package com.Sw3ys.bigenderchest.gui;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

public class SelectorGUI {
   private static final int PLAYERS_PER_PAGE = 45;
   private final BiggerEnderChest plugin;
   private final EnderChestManager manager;
   private final NamespacedKey targetKey;

   public SelectorGUI(BiggerEnderChest plugin, EnderChestManager manager) {
      this.plugin = plugin;
      this.manager = manager;
      this.targetKey = new NamespacedKey(plugin, "target");
   }

   public void open(Player player, int page) {
      List<OfflinePlayer> players = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));
      if (this.plugin.getConfig().getBoolean("selector.sort-alphabetically", true)) {
         players.sort(Comparator.comparing(OfflinePlayer::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
      }

      SelectorHolder holder = new SelectorHolder(page);
      String rawTitle = this.plugin.getConfig().getString("gui.selector-title", "&dSelect Player");
      Inventory inv = Bukkit.createInventory(holder, 54, LegacyComponentSerializer.legacyAmpersand().deserialize(rawTitle));
      holder.setInventory(inv);
      int start = page * 45;
      int end = Math.min(start + 45, players.size());
      int slot = 0;

      for (int i = start; i < end; i++) {
         OfflinePlayer target = players.get(i);
         ItemStack head = new ItemStack(Material.PLAYER_HEAD);
         SkullMeta meta = (SkullMeta)head.getItemMeta();
         if (meta != null) {
            meta.setOwningPlayer(target);
            String name = target.getName() == null ? target.getUniqueId().toString() : target.getName();
            meta.displayName(((TextComponent)Component.text(name).color(NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(this.targetKey, PersistentDataType.STRING, target.getUniqueId().toString());
            head.setItemMeta(meta);
            inv.setItem(slot++, head);
         }
      }

      inv.setItem(47, this.named(Material.ARROW, "&cPrevious Page"));
      inv.setItem(49, this.named(Material.OAK_SIGN, "&aSearch Player"));
      inv.setItem(51, this.named(Material.ARROW, "&aNext Page"));
      player.openInventory(inv);
   }

   private ItemStack named(Material material, String legacyName) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(legacyName).decoration(TextDecoration.ITALIC, false));
         item.setItemMeta(meta);
      }

      return item;
   }

   public boolean hasNextPage(int page) {
      int total = Bukkit.getOfflinePlayers().length;
      return (page + 1) * 45 < total;
   }

   public UUID readTarget(ItemStack head) {
      if (head == null) {
         return null;
      } else {
         ItemMeta meta = head.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String stored = (String)meta.getPersistentDataContainer().get(this.targetKey, PersistentDataType.STRING);
            if (stored == null) {
               return null;
            } else {
               try {
                  return UUID.fromString(stored);
               } catch (IllegalArgumentException var5) {
                  return null;
               }
            }
         }
      }
   }
}
