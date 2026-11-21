package github.nighter.smartspawner.spawner.gui.synchronization.managers;

import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ViewerTrackingManager {
   private final Map<UUID, ViewerTrackingManager.ViewerInfo> playerToSpawnerMap = new ConcurrentHashMap<>();
   private final Map<String, Set<UUID>> spawnerToPlayersMap = new ConcurrentHashMap<>();
   private final Map<UUID, ViewerTrackingManager.ViewerInfo> mainMenuViewers = new ConcurrentHashMap<>();
   private final Map<String, Set<UUID>> spawnerToMainMenuViewers = new ConcurrentHashMap<>();
   private final Map<String, Set<UUID>> spawnerToFilterViewersMap = new ConcurrentHashMap<>();

   public void trackViewer(UUID playerId, SpawnerData spawner, ViewerTrackingManager.ViewerType viewerType) {
      ViewerTrackingManager.ViewerInfo info = new ViewerTrackingManager.ViewerInfo(spawner, viewerType);
      this.playerToSpawnerMap.put(playerId, info);
      this.spawnerToPlayersMap.computeIfAbsent(spawner.getSpawnerId(), k -> ConcurrentHashMap.newKeySet()).add(playerId);
      if (viewerType == ViewerTrackingManager.ViewerType.MAIN_MENU) {
         this.mainMenuViewers.put(playerId, info);
         this.spawnerToMainMenuViewers.computeIfAbsent(spawner.getSpawnerId(), k -> ConcurrentHashMap.newKeySet()).add(playerId);
      }

      if (viewerType == ViewerTrackingManager.ViewerType.FILTER) {
         this.spawnerToFilterViewersMap.computeIfAbsent(spawner.getSpawnerId(), k -> ConcurrentHashMap.newKeySet()).add(playerId);
      }
   }

   public void untrackViewer(UUID playerId) {
      ViewerTrackingManager.ViewerInfo info = this.playerToSpawnerMap.remove(playerId);
      if (info != null) {
         SpawnerData spawner = info.spawnerData;
         String spawnerId = spawner.getSpawnerId();
         Set<UUID> viewers = this.spawnerToPlayersMap.get(spawnerId);
         if (viewers != null) {
            viewers.remove(playerId);
            if (viewers.isEmpty()) {
               this.spawnerToPlayersMap.remove(spawnerId);
            }
         }

         Set<UUID> filterViewers = this.spawnerToFilterViewersMap.get(spawnerId);
         if (filterViewers != null) {
            filterViewers.remove(playerId);
            if (filterViewers.isEmpty()) {
               this.spawnerToFilterViewersMap.remove(spawnerId);
            }
         }
      }

      ViewerTrackingManager.ViewerInfo mainMenuInfo = this.mainMenuViewers.remove(playerId);
      if (mainMenuInfo != null) {
         SpawnerData spawnerx = mainMenuInfo.spawnerData;
         Set<UUID> mainMenuViewerSet = this.spawnerToMainMenuViewers.get(spawnerx.getSpawnerId());
         if (mainMenuViewerSet != null) {
            mainMenuViewerSet.remove(playerId);
            if (mainMenuViewerSet.isEmpty()) {
               this.spawnerToMainMenuViewers.remove(spawnerx.getSpawnerId());
            }
         }
      }
   }

   public Set<Player> getViewers(String spawnerId) {
      Set<UUID> viewerIds = this.spawnerToPlayersMap.get(spawnerId);
      if (viewerIds != null && !viewerIds.isEmpty()) {
         Set<Player> onlineViewers = new HashSet<>(viewerIds.size());

         for (UUID id : viewerIds) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
               onlineViewers.add(player);
            }
         }

         return onlineViewers;
      } else {
         return Collections.emptySet();
      }
   }

   public boolean hasViewers(SpawnerData spawner) {
      Set<UUID> viewers = this.spawnerToPlayersMap.get(spawner.getSpawnerId());
      return viewers != null && !viewers.isEmpty();
   }

   public ViewerTrackingManager.ViewerInfo getViewerInfo(UUID playerId) {
      return this.playerToSpawnerMap.get(playerId);
   }

   public Set<UUID> getMainMenuViewersForSpawner(String spawnerId) {
      return this.spawnerToMainMenuViewers.get(spawnerId);
   }

   public Map<UUID, ViewerTrackingManager.ViewerInfo> getAllViewers() {
      return this.playerToSpawnerMap;
   }

   public Set<UUID> getViewerIds(String spawnerId) {
      return this.spawnerToPlayersMap.get(spawnerId);
   }

   public Set<UUID> getFilterViewersForSpawner(String spawnerId) {
      return this.spawnerToFilterViewersMap.get(spawnerId);
   }

   public boolean hasAnyViewers() {
      return !this.playerToSpawnerMap.isEmpty();
   }

   public boolean hasMainMenuViewers() {
      return !this.mainMenuViewers.isEmpty();
   }

   public void clearAll() {
      this.playerToSpawnerMap.clear();
      this.spawnerToPlayersMap.clear();
      this.mainMenuViewers.clear();
      this.spawnerToMainMenuViewers.clear();
      this.spawnerToFilterViewersMap.clear();
   }

   @Generated
   public Map<UUID, ViewerTrackingManager.ViewerInfo> getMainMenuViewers() {
      return this.mainMenuViewers;
   }

   public static class ViewerInfo {
      private final SpawnerData spawnerData;
      private final long lastUpdateTime;
      private final ViewerTrackingManager.ViewerType viewerType;

      public ViewerInfo(SpawnerData spawnerData, ViewerTrackingManager.ViewerType viewerType) {
         this.spawnerData = spawnerData;
         this.lastUpdateTime = System.currentTimeMillis();
         this.viewerType = viewerType;
      }

      public SpawnerData getSpawnerData() {
         return this.spawnerData;
      }

      public long getLastUpdateTime() {
         return this.lastUpdateTime;
      }

      public ViewerTrackingManager.ViewerType getViewerType() {
         return this.viewerType;
      }
   }

   public static enum ViewerType {
      MAIN_MENU,
      STORAGE,
      FILTER;
   }
}
