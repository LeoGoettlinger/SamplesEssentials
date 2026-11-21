package github.nighter.smartspawner.commands.editloot;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.MessageService;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootEditorHandler implements Listener {
   private final MessageService messageService;
   private final LootEditorService service;
   private final LootEditorUI ui;
   private final LootEditorDialogs dialogs;

   public LootEditorHandler(SmartSpawner plugin, LootEditorService service, LootEditorUI ui, LootEditorDialogs dialogs) {
      this.messageService = plugin.getMessageService();
      this.service = service;
      this.ui = ui;
      this.dialogs = dialogs;
   }

   @EventHandler
   public void onLootListClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof LootListHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getInventory()) {
               LootEditorTarget target = holder.getTarget();
               String entryKey = holder.getEntryKey();
               if (!this.service.hasEntry(target, entryKey)) {
                  this.messageService.sendMessage(player, "editloot.entry_missing");
                  player.closeInventory();
               } else {
                  int slot = event.getSlot();
                  if (slot >= 0 && slot <= 26) {
                     List<String> lootKeys = this.service.listLootKeys(target, entryKey);
                     int index = slot - 0;
                     if (index >= lootKeys.size()) {
                        if (index == lootKeys.size() && lootKeys.size() < 27) {
                           this.click(player);
                           this.ui.openItemCapture(player, new LootCaptureHolder(target, LootCaptureHolder.Purpose.ADD_LOOT, entryKey, null));
                        }
                     } else {
                        String lootKey = lootKeys.get(index);
                        if (event.getClick() == ClickType.RIGHT) {
                           this.click(player);
                           this.ui.openItemCapture(player, new LootCaptureHolder(target, LootCaptureHolder.Purpose.REPLACE_LOOT, entryKey, lootKey));
                        } else {
                           this.click(player);
                           this.dialogs.openEditForm(player, target, entryKey, lootKey);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onCaptureClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof LootCaptureHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            boolean ownInventory = event.getClickedInventory() != event.getInventory();
            if (event.isShiftClick()) {
               event.setCancelled(true);
            } else if (!ownInventory) {
               int slot = event.getSlot();
               if (slot != 13) {
                  event.setCancelled(true);
                  if (slot == 18) {
                     this.click(player);
                     this.returnCapturedItem(player, event.getInventory());
                     this.ui.openLootList(player, holder.getTarget(), holder.getEntryKey());
                  } else {
                     if (slot == 26) {
                        ItemStack captured = event.getInventory().getItem(13);
                        if (captured == null || captured.getType() == Material.AIR) {
                           this.messageService.sendMessage(player, "editloot.capture_empty");
                           return;
                        }

                        event.getInventory().setItem(13, null);
                        this.applyCapture(player, holder, captured);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onCaptureClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder(false) instanceof LootCaptureHolder) {
         if (event.getPlayer() instanceof Player player) {
            this.returnCapturedItem(player, event.getInventory());
         }
      }
   }

   private void returnCapturedItem(Player player, Inventory inventory) {
      ItemStack captured = inventory.getItem(13);
      if (captured != null && captured.getType() != Material.AIR) {
         inventory.setItem(13, null);

         for (ItemStack leftover : player.getInventory().addItem(new ItemStack[]{captured}).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
         }
      }
   }

   private void applyCapture(Player player, LootCaptureHolder holder, ItemStack captured) {
      LootEditorTarget target = holder.getTarget();
      String entryKey = holder.getEntryKey();
      if (!this.service.hasEntry(target, entryKey)) {
         this.messageService.sendMessage(player, "editloot.entry_missing");
         this.returnItem(player, captured);
         player.closeInventory();
      } else {
         switch (holder.getPurpose()) {
            case ADD_LOOT:
               this.dialogs.openAddForm(player, target, entryKey, captured);
               break;
            case REPLACE_LOOT:
               this.service.setLootItem(target, entryKey, holder.getLootKey(), captured);
               this.returnItem(player, captured);
               this.messageService.sendMessage(player, "editloot.loot_replaced", Map.of("label", holder.getLootKey()));
               this.ui.openLootList(player, target, entryKey);
         }
      }
   }

   private void returnItem(Player player, ItemStack item) {
      for (ItemStack leftover : player.getInventory().addItem(new ItemStack[]{item}).values()) {
         player.getWorld().dropItemNaturally(player.getLocation(), leftover);
      }
   }

   private void click(Player player) {
      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
   }
}
