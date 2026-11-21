package github.nighter.smartspawner.spawner.gui.synchronization.services;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.gui.storage.SpawnerStorageUI;
import github.nighter.smartspawner.spawner.gui.storage.StoragePageHolder;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class StorageUpdateService {
   private static final int ITEMS_PER_PAGE = 45;
   private final SmartSpawner plugin;
   private final SpawnerStorageUI spawnerStorageUI;

   public StorageUpdateService(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerStorageUI = plugin.getSpawnerStorageUI();
   }

   public void processStorageUpdate(Player viewer, SpawnerData spawner, int oldTotalPages, int newTotalPages) {
      Location loc = viewer.getLocation();
      if (loc != null) {
         Scheduler.runLocationTask(loc, () -> {
            if (viewer.isOnline()) {
               Inventory openInv = viewer.getOpenInventory().getTopInventory();
               if (openInv != null && openInv.getHolder(false) instanceof StoragePageHolder) {
                  StoragePageHolder holder = (StoragePageHolder)openInv.getHolder(false);
                  this.processStorageUpdateDirect(viewer, openInv, spawner, holder, oldTotalPages, newTotalPages);
               }
            }
         });
      }
   }

   public void processStorageUpdateDirect(
      Player viewer, Inventory inventory, SpawnerData spawner, StoragePageHolder holder, int oldTotalPages, int newTotalPages
   ) {
      int currentPage = holder.getCurrentPage();
      boolean pagesChanged = oldTotalPages != newTotalPages;
      if (!pagesChanged) {
         this.spawnerStorageUI.updateDisplay(inventory, spawner, currentPage, newTotalPages);
         holder.updateOldUsedSlots();
         viewer.updateInventory();
      } else {
         boolean needsNewInventory = false;
         int targetPage = currentPage;
         if (currentPage > newTotalPages) {
            targetPage = newTotalPages;
            holder.setCurrentPage(newTotalPages);
            needsNewInventory = true;
         } else {
            needsNewInventory = true;
         }

         if (needsNewInventory) {
            try {
               holder.setTotalPages(newTotalPages);
               holder.updateOldUsedSlots();
               String newTitle = this.getStorageTitle(spawner, targetPage, newTotalPages);
               viewer.getOpenInventory().setTitle(newTitle);
               this.spawnerStorageUI.updateDisplay(inventory, spawner, targetPage, newTotalPages);
            } catch (Exception var13) {
               Inventory newInv = this.spawnerStorageUI.createStorageInventory(viewer, spawner, targetPage, newTotalPages);
               viewer.closeInventory();
               viewer.openInventory(newInv);
            }
         } else {
            this.spawnerStorageUI.updateDisplay(inventory, spawner, targetPage, newTotalPages);
            viewer.updateInventory();
         }
      }
   }

   public int calculateTotalPages(int totalItems) {
      return totalItems <= 0 ? 1 : (int)Math.ceil(totalItems / 45.0);
   }

   private String getStorageTitle(SpawnerData spawner, int page, int totalPages) {
      return this.spawnerStorageUI.getStorageTitle(spawner, page, totalPages);
   }
}
