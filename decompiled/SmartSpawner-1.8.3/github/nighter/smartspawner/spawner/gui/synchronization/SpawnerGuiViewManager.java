package github.nighter.smartspawner.spawner.gui.synchronization;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.gui.storage.StoragePageHolder;
import github.nighter.smartspawner.spawner.gui.storage.filter.FilterConfigHolder;
import github.nighter.smartspawner.spawner.gui.synchronization.listeners.InventoryEventListener;
import github.nighter.smartspawner.spawner.gui.synchronization.listeners.PlayerEventListener;
import github.nighter.smartspawner.spawner.gui.synchronization.managers.UpdateTaskManager;
import github.nighter.smartspawner.spawner.gui.synchronization.managers.ViewerTrackingManager;
import github.nighter.smartspawner.spawner.gui.synchronization.services.GuiUpdateService;
import github.nighter.smartspawner.spawner.gui.synchronization.services.StorageUpdateService;
import github.nighter.smartspawner.spawner.gui.synchronization.services.TimerUpdateService;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class SpawnerGuiViewManager {
   private final SmartSpawner plugin;
   private final ViewerTrackingManager viewerTrackingManager;
   private final UpdateTaskManager updateTaskManager;
   private final TimerUpdateService timerUpdateService;
   private final GuiUpdateService guiUpdateService;
   private final StorageUpdateService storageUpdateService;
   private final InventoryEventListener inventoryEventListener;
   private final PlayerEventListener playerEventListener;

   public SpawnerGuiViewManager(SmartSpawner plugin) {
      this.plugin = plugin;
      this.viewerTrackingManager = new ViewerTrackingManager();
      this.updateTaskManager = new UpdateTaskManager();
      this.timerUpdateService = new TimerUpdateService(plugin, this.viewerTrackingManager);
      this.guiUpdateService = new GuiUpdateService(plugin);
      this.storageUpdateService = new StorageUpdateService(plugin);
      this.inventoryEventListener = new InventoryEventListener(this.viewerTrackingManager, this::onViewerAdded);
      this.playerEventListener = new PlayerEventListener(this.viewerTrackingManager);
      Bukkit.getPluginManager().registerEvents(this.inventoryEventListener, plugin);
      Bukkit.getPluginManager().registerEvents(this.playerEventListener, plugin);
   }

   private void onViewerAdded() {
      if (!this.updateTaskManager.isRunning() && this.viewerTrackingManager.hasAnyViewers()) {
         this.updateTaskManager.startTask(this::processPeriodicUpdates);
      }
   }

   private void processPeriodicUpdates() {
      this.guiUpdateService.processPendingUpdates(this.viewerTrackingManager::getViewerInfo, this::cleanupViewer);
      if (this.timerUpdateService.shouldProcessTimerUpdates()) {
         this.timerUpdateService.processTimerUpdates();
      }

      if (!this.viewerTrackingManager.hasAnyViewers()) {
         this.updateTaskManager.stopTask();
      }
   }

   private void cleanupViewer(UUID playerId) {
      this.viewerTrackingManager.untrackViewer(playerId);
      this.guiUpdateService.clearPlayerUpdates(playerId);
      this.timerUpdateService.clearPlayerTracking(playerId);
   }

   public boolean isTimerPlaceholdersEnabled() {
      return this.timerUpdateService.isTimerPlaceholdersEnabled();
   }

   public String calculateTimerDisplay(SpawnerData spawner) {
      return this.timerUpdateService.calculateTimerDisplay(spawner, null);
   }

   public String calculateTimerDisplay(SpawnerData spawner, Player player) {
      return this.timerUpdateService.calculateTimerDisplay(spawner, player);
   }

   public void recheckTimerPlaceholders() {
      this.timerUpdateService.recheckTimerPlaceholders();
   }

   public Set<Player> getViewers(String spawnerId) {
      return this.viewerTrackingManager.getViewers(spawnerId);
   }

   public boolean hasViewers(SpawnerData spawner) {
      return this.viewerTrackingManager.hasViewers(spawner);
   }

   public void clearAllTrackedGuis() {
      this.viewerTrackingManager.clearAll();
      this.guiUpdateService.clearAllPendingUpdates();
      this.timerUpdateService.clearAllTracking();
   }

   public void forceStateChangeUpdate(SpawnerData spawner) {
      this.timerUpdateService.forceStateChangeUpdate(spawner);
   }

   public void forceTimerUpdateInactive(Player player, SpawnerData spawner) {
      this.timerUpdateService.forceTimerUpdateInactive(player, spawner);
   }

   public void updateSpawnerMenuViewers(SpawnerData spawner) {
      Set<UUID> viewers = this.viewerTrackingManager.getViewerIds(spawner.getSpawnerId());
      if (viewers != null && !viewers.isEmpty()) {
         if (this.plugin.getSpawnerMenuUI() != null) {
            this.plugin.getSpawnerMenuUI().invalidateSpawnerCache(spawner.getSpawnerId());
         }

         for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
               this.guiUpdateService.scheduleUpdate(viewerId, 7);
               Inventory openInv = viewer.getOpenInventory().getTopInventory();
               if (openInv.getHolder(false) instanceof StoragePageHolder holder) {
                  Location loc = viewer.getLocation();
                  if (holder.getSpawnerData().getSpawnerId().equals(spawner.getSpawnerId())) {
                     Scheduler.runLocationTask(loc, () -> {
                        if (viewer.isOnline()) {
                           Inventory inv = viewer.getOpenInventory().getTopInventory();
                           if (inv.getHolder(false) instanceof StoragePageHolder spHolder) {
                              if (!spHolder.getSpawnerData().getSpawnerId().equals(spawner.getSpawnerId())) {
                                 return;
                              }

                              int oldPages = this.storageUpdateService.calculateTotalPages(holder.getOldUsedSlots());
                              int newPages = this.storageUpdateService.calculateTotalPages(spawner.getVirtualInventory().getUsedSlots());
                              this.storageUpdateService.processStorageUpdateDirect(viewer, inv, spawner, spHolder, oldPages, newPages);
                           }
                        }
                     });
                  }
               }
            } else {
               this.cleanupViewer(viewerId);
            }
         }
      }
   }

   public void updateSpawnerMenuGui(Player player, SpawnerData spawner, boolean forceUpdate) {
      this.guiUpdateService.scheduleUpdate(player.getUniqueId(), 7);
   }

   public void closeAllViewersInventory(SpawnerData spawner) {
      String spawnerId = spawner.getSpawnerId();
      Set<Player> viewers = this.getViewers(spawnerId);
      if (!viewers.isEmpty()) {
         for (Player viewer : viewers) {
            if (viewer != null && viewer.isOnline()) {
               Scheduler.runEntityTask(viewer, () -> {
                  if (viewer.isOnline()) {
                     viewer.closeInventory();
                  }
               });
            }
         }
      }

      Set<UUID> filterViewers = this.viewerTrackingManager.getFilterViewersForSpawner(spawnerId);
      if (filterViewers != null && !filterViewers.isEmpty()) {
         for (UUID viewerId : new HashSet<>(filterViewers)) {
            Player viewerx = Bukkit.getPlayer(viewerId);
            if (viewerx != null && viewerx.isOnline()) {
               Scheduler.runEntityTask(
                  viewerx,
                  () -> {
                     if (viewer.isOnline()) {
                        Inventory openInventory = viewer.getOpenInventory().getTopInventory();
                        if (openInventory != null
                           && openInventory.getHolder(false) instanceof FilterConfigHolder filterHolder
                           && filterHolder.getSpawnerData().getSpawnerId().equals(spawnerId)) {
                           viewer.closeInventory();
                        }
                     }
                  }
               );
            }
         }
      }
   }

   public void cleanup() {
      this.updateTaskManager.stopTask();
      this.clearAllTrackedGuis();
      HandlerList.unregisterAll(this.inventoryEventListener);
      HandlerList.unregisterAll(this.playerEventListener);
   }
}
