package github.nighter.smartspawner.spawner.gui.synchronization.services;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuHolder;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuUI;
import github.nighter.smartspawner.spawner.gui.synchronization.managers.ViewerTrackingManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiUpdateService {
   public static final int UPDATE_CHEST = 1;
   public static final int UPDATE_INFO = 2;
   public static final int UPDATE_EXP = 4;
   public static final int UPDATE_ALL = 7;
   private final SmartSpawner plugin;
   private final SpawnerMenuUI spawnerMenuUI;
   private final Set<UUID> pendingUpdates = ConcurrentHashMap.newKeySet();
   private final Map<UUID, Integer> updateFlags = new ConcurrentHashMap<>();

   public GuiUpdateService(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerMenuUI = plugin.getSpawnerMenuUI();
   }

   public void scheduleUpdate(UUID playerId, int flags) {
      this.pendingUpdates.add(playerId);
      this.updateFlags.put(playerId, flags);
   }

   public void processPendingUpdates(Function<UUID, ?> viewerInfoGetter, Consumer<UUID> untrackViewer) {
      if (!this.pendingUpdates.isEmpty()) {
         Set<UUID> currentUpdates = new HashSet<>(this.pendingUpdates);
         this.pendingUpdates.clear();

         for (UUID playerId : currentUpdates) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
               Object info = viewerInfoGetter.apply(playerId);
               if (info == null) {
                  this.updateFlags.remove(playerId);
               } else {
                  int flags = this.updateFlags.getOrDefault(playerId, 7);
                  this.updateFlags.remove(playerId);
                  Location loc = player.getLocation();
                  SpawnerData spawner = this.extractSpawnerData(info);
                  if (spawner != null) {
                     Scheduler.runLocationTask(loc, () -> {
                        if (player.isOnline()) {
                           Inventory openInv = player.getOpenInventory().getTopInventory();
                           if (openInv.getHolder(false) instanceof SpawnerMenuHolder) {
                              this.processInventoryUpdate(player, openInv, spawner, flags);
                           }
                        }
                     });
                  }
               }
            } else {
               untrackViewer.accept(playerId);
               this.updateFlags.remove(playerId);
            }
         }
      }
   }

   private void processInventoryUpdate(Player player, Inventory inventory, SpawnerData spawner, int flags) {
      boolean needsUpdate = false;
      SpawnerMenuHolder holder = (SpawnerMenuHolder)inventory.getHolder(false);
      if ((flags & 1) != 0) {
         GuiButton storageButton = holder.getStorageButton();
         if (storageButton != null) {
            needsUpdate |= this.updateChestItem(inventory, spawner, storageButton);
         }
      }

      if ((flags & 2) != 0) {
         GuiButton infoButton = holder.getInfoButton();
         if (infoButton != null) {
            needsUpdate |= this.updateSpawnerInfoItem(inventory, spawner, player, infoButton);
         }
      }

      if ((flags & 4) != 0) {
         GuiButton expButton = holder.getExpButton();
         if (expButton != null) {
            needsUpdate |= this.updateExpItem(inventory, spawner, expButton);
         }
      }

      if (needsUpdate) {
         player.updateInventory();
      }
   }

   private boolean updateChestItem(Inventory inventory, SpawnerData spawner, GuiButton button) {
      int storageSlot = button.getSlot();
      if (storageSlot < 0) {
         return false;
      } else {
         ItemStack currentChestItem = inventory.getItem(storageSlot);
         ItemStack newChestItem = this.spawnerMenuUI.createLootStorageItem(spawner, button);
         if (areItemsEqual(currentChestItem, newChestItem)) {
            return false;
         } else {
            inventory.setItem(storageSlot, newChestItem);
            return true;
         }
      }
   }

   private boolean updateExpItem(Inventory inventory, SpawnerData spawner, GuiButton button) {
      int expSlot = button.getSlot();
      if (expSlot < 0) {
         return false;
      } else {
         ItemStack currentExpItem = inventory.getItem(expSlot);
         ItemStack newExpItem = this.spawnerMenuUI.createExpItem(spawner, button);
         if (areItemsEqual(currentExpItem, newExpItem)) {
            return false;
         } else {
            inventory.setItem(expSlot, newExpItem);
            return true;
         }
      }
   }

   private boolean updateSpawnerInfoItem(Inventory inventory, SpawnerData spawner, Player player, GuiButton button) {
      int spawnerInfoSlot = button.getSlot();
      if (spawnerInfoSlot < 0) {
         return false;
      } else {
         ItemStack currentSpawnerItem = inventory.getItem(spawnerInfoSlot);
         ItemStack newSpawnerItem = this.spawnerMenuUI.createSpawnerInfoItem(player, spawner, button);
         if (areItemsEqual(currentSpawnerItem, newSpawnerItem)) {
            return false;
         } else {
            inventory.setItem(spawnerInfoSlot, newSpawnerItem);
            return true;
         }
      }
   }

   public static boolean areItemsEqual(ItemStack item1, ItemStack item2) {
      if (item1 == item2) {
         return true;
      } else {
         return item1 != null && item2 != null ? item1.getAmount() == item2.getAmount() && item1.isSimilar(item2) : false;
      }
   }

   private SpawnerData extractSpawnerData(Object info) {
      return info instanceof ViewerTrackingManager.ViewerInfo viewerInfo ? viewerInfo.getSpawnerData() : null;
   }

   public void clearAllPendingUpdates() {
      this.pendingUpdates.clear();
      this.updateFlags.clear();
   }

   public void clearPlayerUpdates(UUID playerId) {
      this.pendingUpdates.remove(playerId);
      this.updateFlags.remove(playerId);
   }
}
