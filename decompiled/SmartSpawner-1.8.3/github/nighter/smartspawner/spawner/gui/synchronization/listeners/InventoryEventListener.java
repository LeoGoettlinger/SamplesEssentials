package github.nighter.smartspawner.spawner.gui.synchronization.listeners;

import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuHolder;
import github.nighter.smartspawner.spawner.gui.storage.StoragePageHolder;
import github.nighter.smartspawner.spawner.gui.storage.filter.FilterConfigHolder;
import github.nighter.smartspawner.spawner.gui.synchronization.managers.ViewerTrackingManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryEventListener implements Listener {
   private final ViewerTrackingManager viewerTrackingManager;
   private final Runnable onViewerAdded;
   private final Set<Class<? extends InventoryHolder>> validHolderTypes;

   public InventoryEventListener(ViewerTrackingManager viewerTrackingManager, Runnable onViewerAdded) {
      this.viewerTrackingManager = viewerTrackingManager;
      this.onViewerAdded = onViewerAdded;
      this.validHolderTypes = Set.of(SpawnerMenuHolder.class, StoragePageHolder.class, FilterConfigHolder.class);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player player) {
         InventoryHolder var10 = event.getInventory().getHolder(false);
         if (this.isValidHolder(var10)) {
            UUID playerId = player.getUniqueId();
            SpawnerData spawnerData = null;
            ViewerTrackingManager.ViewerType viewerType = null;
            if (var10 instanceof SpawnerMenuHolder spawnerHolder) {
               spawnerData = spawnerHolder.getSpawnerData();
               viewerType = ViewerTrackingManager.ViewerType.MAIN_MENU;
            } else if (var10 instanceof StoragePageHolder storageHolder) {
               spawnerData = storageHolder.getSpawnerData();
               viewerType = ViewerTrackingManager.ViewerType.STORAGE;
            } else if (var10 instanceof FilterConfigHolder filterHolder) {
               spawnerData = filterHolder.getSpawnerData();
               viewerType = ViewerTrackingManager.ViewerType.FILTER;
            }

            if (spawnerData != null && viewerType != null) {
               spawnerData.updateLastInteractedPlayer(player.getName());
               this.viewerTrackingManager.trackViewer(playerId, spawnerData, viewerType);
               this.onViewerAdded.run();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         this.viewerTrackingManager.untrackViewer(player.getUniqueId());
      }
   }

   private boolean isValidHolder(InventoryHolder holder) {
      return holder != null && this.validHolderTypes.contains(holder.getClass());
   }
}
