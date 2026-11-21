package com.Sw3ys.bigenderchest.listeners;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.gui.BiggerEnderHolder;
import com.Sw3ys.bigenderchest.gui.SelectorGUI;
import com.Sw3ys.bigenderchest.gui.SelectorHolder;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import com.Sw3ys.bigenderchest.manager.LidAnimator;
import com.Sw3ys.bigenderchest.util.Sched;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
   private final BiggerEnderChest plugin;
   private final EnderChestManager manager;
   private final SelectorGUI selector;
   private final LidAnimator animator;

   public InventoryListener(BiggerEnderChest plugin, EnderChestManager manager, SelectorGUI selector, LidAnimator animator) {
      this.plugin = plugin;
      this.manager = manager;
      this.selector = selector;
      this.animator = animator;
   }

   @EventHandler
   public void onClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player p) {
         InventoryHolder holder = e.getInventory().getHolder();
         if (holder instanceof SelectorHolder sel) {
            this.handleSelectorClick(e, p, sel);
         } else if (holder instanceof BiggerEnderHolder) {
            this.handleEnderChestClick(e, p);
         }
      }
   }

   private void handleSelectorClick(InventoryClickEvent e, Player p, SelectorHolder sel) {
      e.setCancelled(true);
      ItemStack item = e.getCurrentItem();
      if (item != null && !item.getType().isAir()) {
         Material type = item.getType();
         if (type == Material.ARROW && this.isNamed(item, "Previous Page")) {
            if (sel.getPage() > 0) {
               this.selector.open(p, sel.getPage() - 1);
            }
         } else if (type == Material.ARROW && this.isNamed(item, "Next Page")) {
            if (this.selector.hasNextPage(sel.getPage())) {
               this.selector.open(p, sel.getPage() + 1);
            }
         } else if (type == Material.PLAYER_HEAD) {
            UUID targetUUID = this.selector.readTarget(item);
            if (targetUUID != null) {
               p.closeInventory();
               Sched.entityLater(this.plugin, p, () -> this.manager.open(p, targetUUID), 1L);
            }
         } else {
            if (type == Material.OAK_SIGN) {
               p.closeInventory();
               p.sendMessage(ChatColor.GREEN + "Type a player name or " + ChatColor.RED + "cancel");
               this.plugin.getSearch().startSearch(p);
            }
         }
      }
   }

   private void handleEnderChestClick(InventoryClickEvent e, Player p) {
      if (!p.hasPermission("enderchest.bypass")) {
         Inventory top = e.getView().getTopInventory();
         List<String> blacklist = this.plugin.getConfig().getStringList("blacklisted-items");
         if (e.getClickedInventory() == top) {
            ItemStack cursor = e.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && blacklist.contains(cursor.getType().name())) {
               this.denyBlacklisted(e, p);
               return;
            }
         }

         if (e.isShiftClick() && e.getClickedInventory() != null && e.getClickedInventory() != top) {
            ItemStack item = e.getCurrentItem();
            if (item != null && blacklist.contains(item.getType().name())) {
               this.denyBlacklisted(e, p);
               return;
            }
         }

         if (e.getClick() == ClickType.NUMBER_KEY && e.getClickedInventory() == top) {
            ItemStack hotbarItem = p.getInventory().getItem(e.getHotbarButton());
            if (hotbarItem != null && blacklist.contains(hotbarItem.getType().name())) {
               this.denyBlacklisted(e, p);
               return;
            }
         }
      }

      Sched.entityLater(this.plugin, p, () -> {
         if (this.plugin.getConfig().getBoolean("storage.save-on-click", true)) {
            this.manager.save(p);
         }

         p.updateInventory();
      }, 1L);
   }

   private void denyBlacklisted(InventoryClickEvent e, Player p) {
      e.setCancelled(true);
      p.sendMessage(ChatColor.RED + "That item cannot be stored in an Ender Chest.");
   }

   @EventHandler
   public void onDrag(InventoryDragEvent e) {
      if (e.getWhoClicked() instanceof Player p) {
         if (e.getInventory().getHolder() instanceof BiggerEnderHolder) {
            if (!p.hasPermission("enderchest.bypass")) {
               ItemStack item = e.getOldCursor();
               if (item != null && item.getType() != Material.AIR) {
                  List<String> blacklist = this.plugin.getConfig().getStringList("blacklisted-items");
                  if (blacklist.contains(item.getType().name())) {
                     int topSize = e.getView().getTopInventory().getSize();

                     for (int slot : e.getRawSlots()) {
                        if (slot < topSize) {
                           e.setCancelled(true);
                           p.sendMessage(ChatColor.RED + "That item cannot be stored in an Ender Chest.");
                           Sched.entityLater(this.plugin, p, p::updateInventory, 1L);
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player p) {
         if (e.getInventory().getHolder() instanceof BiggerEnderHolder) {
            if (this.plugin.getConfig().getBoolean("storage.save-on-close", true)) {
               this.manager.save(p);
            }

            this.manager.close(p);
            this.animator.close(p);
            Sched.entityLater(this.plugin, p, p::updateInventory, 1L);
            if (this.plugin.getConfig().getBoolean("sounds.enabled", true)) {
               p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 1.0F, 1.0F);
            }
         }
      }
   }

   private boolean isNamed(ItemStack item, String plainName) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null && meta.hasDisplayName()) {
            String actual = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
            return actual.equalsIgnoreCase(plainName);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
