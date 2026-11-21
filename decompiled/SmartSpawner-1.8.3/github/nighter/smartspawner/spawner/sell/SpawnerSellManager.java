package github.nighter.smartspawner.spawner.sell;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerSellEvent;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpawnerSellManager {
   private final SmartSpawner plugin;
   private final MessageService messageService;
   private final SpawnerGuiViewManager spawnerGuiViewManager;

   public SpawnerSellManager(SmartSpawner plugin) {
      this.plugin = plugin;
      this.messageService = plugin.getMessageService();
      this.spawnerGuiViewManager = plugin.getSpawnerGuiViewManager();
   }

   public void sellAllItems(Player player, SpawnerData spawner) {
      this.sellAllItems(player, spawner, null, 0L, 0L);
   }

   public void sellAllItems(Player player, SpawnerData spawner, Runnable onComplete) {
      this.sellAllItems(player, spawner, onComplete, 0L, 0L);
   }

   public void sellAllItems(Player player, SpawnerData spawner, Runnable onComplete, long expCollected, long expMending) {
      if (!spawner.startSelling()) {
         this.messageService.sendMessage(player, "action_in_progress");
         if (onComplete != null) {
            onComplete.run();
         }
      } else {
         VirtualInventory virtualInv = spawner.getVirtualInventory();
         if (virtualInv.getUsedSlots() == 0) {
            spawner.stopSelling();
            this.messageService.sendMessage(player, "spawner_storage_empty");
            if (onComplete != null) {
               onComplete.run();
            }
         } else {
            if (spawner.isSellValueDirty()) {
               spawner.recalculateSellValue();
            }

            this.spawnerGuiViewManager.closeAllViewersInventory(spawner);
            Map<ItemSignature, Long> itemSnapshot = virtualInv.getConsolidatedItems();
            double accumulatedValue = spawner.getAccumulatedSellValue();
            Location spawnerLocation = spawner.getSpawnerLocation();

            SellResult result;
            try {
               result = this.calculateSellValue(itemSnapshot, accumulatedValue);
            } catch (Exception var15) {
               this.plugin.getLogger().warning("Sell calculation error for " + player.getName() + ": " + var15.getMessage());
               Scheduler.runLocationTask(spawnerLocation, () -> {
                  try {
                     if (onComplete != null) {
                        onComplete.run();
                     }
                  } finally {
                     spawner.stopSelling();
                  }

                  this.messageService.sendMessage(player, "action_failed");
               });
               return;
            }

            Scheduler.runLocationTask(spawnerLocation, () -> {
               try {
                  this.applySellResult(player, spawner, result, expCollected, expMending);
               } finally {
                  try {
                     if (onComplete != null) {
                        onComplete.run();
                     }
                  } finally {
                     spawner.stopSelling();
                  }
               }
            });
         }
      }
   }

   private void applySellResult(Player player, SpawnerData spawner, SellResult sellResult, long expCollected, long expMending) {
      if (!sellResult.isSuccessful()) {
         this.messageService.sendMessage(player, "no_sellable_items");
      } else {
         double amount = sellResult.getTotalValue();
         if (SpawnerSellEvent.getHandlerList().getRegisteredListeners().length != 0) {
            SpawnerSellEvent event = new SpawnerSellEvent(
               player, spawner.getSpawnerLocation(), this.toApiItemStacks(sellResult.getItemsToRemove()), amount, spawner.getEntityType()
            );
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }

            if (event.getMoneyAmount() >= 0.0) {
               amount = event.getMoneyAmount();
            }
         }

         boolean depositSuccess = this.plugin.getItemPriceManager().getCurrencyManager().deposit(amount, player);
         if (!depositSuccess) {
            this.messageService.sendMessage(player, "action_failed");
         } else {
            spawner.removeItemsAndUpdateSellValue(sellResult.getItemsToRemove());
            spawner.updateHologramData();
            VirtualInventory virtualInv = spawner.getVirtualInventory();
            if (spawner.getIsAtCapacity() && virtualInv.getUsedSlots() < spawner.getMaxSpawnerLootSlots()) {
               spawner.setIsAtCapacity(false);
            }

            this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
            this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", this.plugin.getLanguageManager().formatNumber(sellResult.getItemsSold()));
            placeholders.put("price", this.plugin.getLanguageManager().formatNumber(amount));
            if (expCollected > 0L) {
               long expGiven = expCollected - expMending;
               placeholders.put("exp", this.plugin.getLanguageManager().formatNumber(expGiven));
               if (expMending > 0L) {
                  placeholders.put("exp_mending", this.plugin.getLanguageManager().formatNumber(expMending));
                  this.messageService.sendMessage(player, "sell_and_exp_success_with_mending", placeholders);
               } else {
                  this.messageService.sendMessage(player, "sell_and_exp_success", placeholders);
               }
            } else {
               this.messageService.sendMessage(player, "sell_success", placeholders);
            }

            spawner.markLastSellAsProcessed();
         }
      }
   }

   private SellResult calculateSellValue(Map<ItemSignature, Long> consolidatedItems, double totalValue) {
      long totalItemsSold = 0L;

      for (Entry<ItemSignature, Long> entry : consolidatedItems.entrySet()) {
         totalItemsSold += entry.getValue();
      }

      return new SellResult(totalValue, totalItemsSold, consolidatedItems);
   }

   private List<ItemStack> toApiItemStacks(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         List<ItemStack> apiItems = new ArrayList<>(items.size());

         for (Entry<ItemSignature, Long> entry : items.entrySet()) {
            ItemSignature signature = entry.getKey();
            Long amount = entry.getValue();
            ItemStack stack = signature.getTemplate();
            stack.setAmount((int)Math.min(amount, 2147483647L));
            apiItems.add(stack);
         }

         return apiItems;
      } else {
         return Collections.emptyList();
      }
   }
}
