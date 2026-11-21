package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.ParticleUtils;
import de.theredend2000.advancedhunt.util.PlayerSnapshot;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ParticleManager {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final PlayerManager playerManager;
   private final CollectionManager collectionManager;
   private BukkitTask task;
   private final Map<UUID, Boolean> globallyClaimedCache = new ConcurrentHashMap<>();
   private int staggerTick = 0;
   private static final int STAGGER_CYCLE = 3;
   private final ArrayDeque<ParticleManager.ParticleAction> particleActionPool = new ArrayDeque<>(200);
   private final ArrayDeque<ParticleManager.GlobalParticleAction> globalParticleActionPool = new ArrayDeque<>(200);
   private final ArrayDeque<HashSet<UUID>> hashSetPool = new ArrayDeque<>(50);
   private static final int MAX_POOL_SIZE = 500;
   private static final int MAX_PARTICLES_PER_PLAYER_PER_TICK = 75;
   private ParticleManager.ParticleConfig notFoundConfig;
   private ParticleManager.ParticleConfig foundByPlayerConfig;
   private ParticleManager.ParticleConfig foundByOthersConfig;
   private int viewDistanceSq;
   private final Location reusableLocation = new Location(null, 0.0, 0.0, 0.0);

   public ParticleManager(Main plugin, TreasureManager treasureManager, PlayerManager playerManager, CollectionManager collectionManager) {
      this.plugin = plugin;
      this.treasureManager = treasureManager;
      this.playerManager = playerManager;
      this.collectionManager = collectionManager;
   }

   public void markTreasureAsGloballyClaimed(UUID treasureId) {
      this.globallyClaimedCache.put(treasureId, Boolean.TRUE);
   }

   public boolean isTreasureGloballyClaimed(UUID treasureId) {
      return this.globallyClaimedCache.getOrDefault(treasureId, Boolean.FALSE);
   }

   public void clearGlobalCache(UUID collectionId) {
      for (TreasureCore treasure : this.treasureManager.getTreasureCoresInCollection(collectionId)) {
         this.globallyClaimedCache.remove(treasure.getId());
      }
   }

   public void clearAllGlobalCache() {
      this.globallyClaimedCache.clear();
   }

   private CompletableFuture<Void> preloadGloballyClaimedTreasures() {
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (Collection collection : this.collectionManager.getAllCollections()) {
         if (collection.isSinglePlayerFind()) {
            futures.add(this.refreshGlobalCache(collection.getId()));
         }
      }

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
   }

   private void refreshSinglePlayerFindVisibility() {
      TreasureVisibilityManager vis = this.plugin.getTreasureVisibilityManager();
      if (vis != null) {
         for (Collection collection : this.collectionManager.getAllCollections()) {
            if (collection.isSinglePlayerFind() && collection.isHideAfterFound()) {
               vis.refreshHideAfterFound(collection);
            }
         }
      }
   }

   public CompletableFuture<Void> refreshGlobalCache(UUID collectionId) {
      List<TreasureCore> treasures = this.treasureManager.getTreasureCoresInCollection(collectionId);
      if (treasures.isEmpty()) {
         return CompletableFuture.completedFuture(null);
      } else {
         List<CompletableFuture<Void>> futures = new ArrayList<>(treasures.size());

         for (TreasureCore treasure : treasures) {
            UUID treasureId = treasure.getId();
            futures.add(this.plugin.getDataRepository().getPlayersWhoFound(treasureId).thenAccept(claimers -> {
               if (claimers != null && !claimers.isEmpty()) {
                  this.globallyClaimedCache.put(treasureId, Boolean.TRUE);
               } else {
                  this.globallyClaimedCache.remove(treasureId);
               }
            }));
         }

         return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
      }
   }

   public void reapplyGlobalClaimVisibility() {
      List<Collection> collections = this.collectionManager.getAllCollections();
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (Collection collection : collections) {
         if (collection.isSinglePlayerFind()) {
            futures.add(this.refreshGlobalCache(collection.getId()));
         }
      }

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         TreasureVisibilityManager vis = this.plugin.getTreasureVisibilityManager();
         if (vis != null) {
            for (Collection collectionx : collections) {
               if (collectionx.isSinglePlayerFind() && collectionx.isHideAfterFound()) {
                  vis.refreshHideAfterFound(collectionx);
               }
            }
         }
      }));
   }

   public void start() {
      this.stop();
      FileConfiguration config = this.plugin.getConfig();
      if (config.getBoolean("particle-settings.enabled", false)) {
         int viewDistance = config.getInt("particle-settings.view-distance", 32);
         this.viewDistanceSq = viewDistance * viewDistance;
         this.notFoundConfig = this.loadParticleConfig(config, "particle-settings.not-found", true, "VILLAGER_HAPPY", 2, 0.03, 0.5, 0.5, 0.5);
         this.foundByPlayerConfig = this.loadParticleConfig(config, "particle-settings.found-by-player", true, "END_ROD", 1, 0.01, 0.2, 0.2, 0.2);
         this.foundByOthersConfig = this.loadParticleConfig(config, "particle-settings.found-by-others", false, "SMOKE_NORMAL", 1, 0.02, 0.3, 0.3, 0.3);
         this.preloadGloballyClaimedTreasures().thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, this::refreshSinglePlayerFindVisibility));
      }
   }

   private ParticleManager.ParticleConfig loadParticleConfig(
      FileConfiguration config,
      String path,
      boolean defaultEnabled,
      String defaultParticle,
      int defaultCount,
      double defaultSpeed,
      double defaultOffX,
      double defaultOffY,
      double defaultOffZ
   ) {
      return new ParticleManager.ParticleConfig(
         config.getBoolean(path + ".enabled", defaultEnabled),
         config.getString(path + ".particle", defaultParticle),
         config.getInt(path + ".count", defaultCount),
         config.getDouble(path + ".speed", defaultSpeed),
         config.getDouble(path + ".offset.x", defaultOffX),
         config.getDouble(path + ".offset.y", defaultOffY),
         config.getDouble(path + ".offset.z", defaultOffZ)
      );
   }

   public void processTick(List<PlayerSnapshot> snapshots, Set<UUID> availableCollections, Set<UUID> singlePlayerFindCollections) {
      FileConfiguration config = this.plugin.getConfig();
      if (config.getBoolean("particle-settings.enabled", false)) {
         if (ParticleUtils.isLegacy() && !ParticleUtils.supportsPerPlayerParticles()) {
            this.tickParticlesLegacy(snapshots, availableCollections);
         } else {
            this.staggerTick = (this.staggerTick + 1) % 3;
            List<ParticleManager.ParticleAction> actions = new ArrayList<>();
            int chunkRadius = (int)Math.ceil(Math.sqrt(this.viewDistanceSq) / 16.0);

            for (PlayerSnapshot snapshot : snapshots) {
               HashSet<UUID> processedTreasures = this.borrowHashSet();
               Location playerLoc = snapshot.location();
               int playerChunkX = playerLoc.getBlockX() >> 4;
               int playerChunkZ = playerLoc.getBlockZ() >> 4;
               PlayerData playerData = this.playerManager.getPlayerData(snapshot.player().getUniqueId());
               List<ParticleManager.TreasureDistance> nearbyTreasures = new ArrayList<>();

               for (int x = -chunkRadius; x <= chunkRadius; x++) {
                  for (int z = -chunkRadius; z <= chunkRadius; z++) {
                     int chunkX = playerChunkX + x;
                     int chunkZ = playerChunkZ + z;
                     List<TreasureCore> treasures = this.treasureManager.getTreasureCoresInChunk(chunkX, chunkZ);
                     if (treasures != null && !treasures.isEmpty()) {
                        for (TreasureCore treasure : treasures) {
                           if (!processedTreasures.contains(treasure.getId())) {
                              int treasureStagger = Math.abs(treasure.getId().hashCode() % 3);
                              if (treasureStagger == this.staggerTick) {
                                 Location treasureLoc = treasure.getLocation();
                                 if (treasureLoc != null
                                    && treasureLoc.getWorld() != null
                                    && treasureLoc.getWorld().getName().equals(playerLoc.getWorld().getName())) {
                                    double dx = playerLoc.getX() - treasureLoc.getX();
                                    double dy = playerLoc.getY() - treasureLoc.getY();
                                    double dz = playerLoc.getZ() - treasureLoc.getZ();
                                    double distSq = dx * dx + dy * dy + dz * dz;
                                    if (distSq <= this.viewDistanceSq) {
                                       ParticleManager.ParticleConfig particleConfig = this.determineParticleConfig(
                                          treasure, playerData, availableCollections, singlePlayerFindCollections, this.globallyClaimedCache
                                       );
                                       if (particleConfig != null && particleConfig.enabled) {
                                          ParticleManager.TreasureDistance td = new ParticleManager.TreasureDistance();
                                          td.set(treasure, treasureLoc, distSq, particleConfig);
                                          nearbyTreasures.add(td);
                                       }

                                       processedTreasures.add(treasure.getId());
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (nearbyTreasures.size() > 75) {
                  nearbyTreasures.sort(Comparator.comparingDouble(tdx -> tdx.distanceSq));
                  nearbyTreasures = nearbyTreasures.subList(0, 75);
               }

               for (ParticleManager.TreasureDistance td : nearbyTreasures) {
                  actions.add(this.borrowParticleAction(snapshot.player(), td.treasureLoc, td.config));
               }

               this.returnHashSet(processedTreasures);
            }

            if (!actions.isEmpty()) {
               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        for (ParticleManager.ParticleAction action : actions) {
                           Location loc = action.location;
                           Location particleLoc = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5);
                           ParticleUtils.spawnParticleForPlayer(
                              action.player,
                              particleLoc,
                              action.config.particleName,
                              action.config.count,
                              action.config.offX,
                              action.config.offY,
                              action.config.offZ,
                              action.config.speed
                           );
                           this.returnParticleAction(action);
                        }
                     }
                  );
            }
         }
      }
   }

   private void tickParticlesLegacy(List<PlayerSnapshot> snapshots, Set<UUID> availableCollections) {
      HashSet<UUID> processedTreasures = this.borrowHashSet();
      List<ParticleManager.GlobalParticleAction> actions = new ArrayList<>();
      int chunkRadius = (int)Math.ceil(Math.sqrt(this.viewDistanceSq) / 16.0);
      if (this.notFoundConfig != null && this.notFoundConfig.enabled) {
         for (PlayerSnapshot snapshot : snapshots) {
            Location playerLoc = snapshot.location();
            int playerChunkX = playerLoc.getBlockX() >> 4;
            int playerChunkZ = playerLoc.getBlockZ() >> 4;

            for (int x = -chunkRadius; x <= chunkRadius; x++) {
               for (int z = -chunkRadius; z <= chunkRadius; z++) {
                  int chunkX = playerChunkX + x;
                  int chunkZ = playerChunkZ + z;
                  List<TreasureCore> treasures = this.treasureManager.getTreasureCoresInChunk(chunkX, chunkZ);
                  if (treasures != null && !treasures.isEmpty()) {
                     for (TreasureCore treasure : treasures) {
                        if (!processedTreasures.contains(treasure.getId()) && availableCollections.contains(treasure.getCollectionId())) {
                           Location treasureLoc = treasure.getLocation();
                           if (treasureLoc != null && treasureLoc.getWorld() != null && treasureLoc.getWorld().getName().equals(playerLoc.getWorld().getName())
                              )
                            {
                              double dx = playerLoc.getX() - treasureLoc.getX();
                              double dy = playerLoc.getY() - treasureLoc.getY();
                              double dz = playerLoc.getZ() - treasureLoc.getZ();
                              double distSq = dx * dx + dy * dy + dz * dz;
                              if (distSq <= this.viewDistanceSq) {
                                 actions.add(this.borrowGlobalParticleAction(treasureLoc, this.notFoundConfig));
                                 processedTreasures.add(treasure.getId());
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         this.returnHashSet(processedTreasures);
         if (!actions.isEmpty()) {
            Bukkit.getScheduler()
               .runTask(
                  this.plugin,
                  () -> {
                     for (ParticleManager.GlobalParticleAction action : actions) {
                        Location loc = action.location;
                        Location particleLoc = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5);
                        ParticleUtils.spawnParticle(
                           particleLoc,
                           action.config.particleName,
                           action.config.count,
                           action.config.offX,
                           action.config.offY,
                           action.config.offZ,
                           action.config.speed
                        );
                        this.returnGlobalParticleAction(action);
                     }
                  }
               );
         }
      } else {
         this.returnHashSet(processedTreasures);
      }
   }

   private void tickParticles() {
   }

   private ParticleManager.ParticleConfig determineParticleConfig(
      TreasureCore treasure,
      PlayerData playerData,
      Set<UUID> availableCollections,
      Set<UUID> singlePlayerFindCollections,
      Map<UUID, Boolean> globallyClaimedCache
   ) {
      if (!availableCollections.contains(treasure.getCollectionId())) {
         return null;
      } else if (playerData.hasFound(treasure.getId())) {
         Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(treasure.getCollectionId());
         return collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound() ? null : this.foundByPlayerConfig;
      } else if (singlePlayerFindCollections.contains(treasure.getCollectionId()) && globallyClaimedCache.getOrDefault(treasure.getId(), Boolean.FALSE)) {
         Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(treasure.getCollectionId());
         return collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound() ? null : this.foundByOthersConfig;
      } else {
         return this.notFoundConfig;
      }
   }

   private ParticleManager.ParticleAction borrowParticleAction(Player player, Location location, ParticleManager.ParticleConfig config) {
      ParticleManager.ParticleAction action = this.particleActionPool.pollLast();
      if (action == null) {
         return new ParticleManager.ParticleAction(player, location, config);
      } else {
         action.player = player;
         action.location = location;
         action.config = config;
         return action;
      }
   }

   private void returnParticleAction(ParticleManager.ParticleAction action) {
      if (this.particleActionPool.size() < 500) {
         action.player = null;
         action.location = null;
         action.config = null;
         this.particleActionPool.addLast(action);
      }
   }

   private ParticleManager.GlobalParticleAction borrowGlobalParticleAction(Location location, ParticleManager.ParticleConfig config) {
      ParticleManager.GlobalParticleAction action = this.globalParticleActionPool.pollLast();
      if (action == null) {
         return new ParticleManager.GlobalParticleAction(location, config);
      } else {
         action.location = location;
         action.config = config;
         return action;
      }
   }

   private void returnGlobalParticleAction(ParticleManager.GlobalParticleAction action) {
      if (this.globalParticleActionPool.size() < 500) {
         action.location = null;
         action.config = null;
         this.globalParticleActionPool.addLast(action);
      }
   }

   private HashSet<UUID> borrowHashSet() {
      HashSet<UUID> set = this.hashSetPool.pollLast();
      if (set == null) {
         return new HashSet<>();
      } else {
         set.clear();
         return set;
      }
   }

   private void returnHashSet(HashSet<UUID> set) {
      if (this.hashSetPool.size() < 500) {
         set.clear();
         this.hashSetPool.addLast(set);
      }
   }

   public void stop() {
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }
   }

   public void reload() {
      this.stop();
      ParticleUtils.resetWarnings();
      this.clearAllGlobalCache();
      this.start();
   }

   private static class GlobalParticleAction {
      Location location;
      ParticleManager.ParticleConfig config;

      GlobalParticleAction(Location location, ParticleManager.ParticleConfig config) {
         this.location = location;
         this.config = config;
      }
   }

   private static class ParticleAction {
      Player player;
      Location location;
      ParticleManager.ParticleConfig config;

      ParticleAction(Player player, Location location, ParticleManager.ParticleConfig config) {
         this.player = player;
         this.location = location;
         this.config = config;
      }
   }

   private static class ParticleConfig {
      boolean enabled;
      String particleName;
      int count;
      double speed;
      double offX;
      double offY;
      double offZ;

      ParticleConfig(boolean enabled, String particleName, int count, double speed, double offX, double offY, double offZ) {
         this.enabled = enabled;
         this.particleName = particleName;
         this.count = count;
         this.speed = speed;
         this.offX = offX;
         this.offY = offY;
         this.offZ = offZ;
      }
   }

   private static class TreasureDistance {
      TreasureCore treasure;
      Location treasureLoc;
      double distanceSq;
      ParticleManager.ParticleConfig config;

      private TreasureDistance() {
      }

      void set(TreasureCore treasure, Location treasureLoc, double distanceSq, ParticleManager.ParticleConfig config) {
         this.treasure = treasure;
         this.treasureLoc = treasureLoc;
         this.distanceSq = distanceSq;
         this.config = config;
      }
   }
}
