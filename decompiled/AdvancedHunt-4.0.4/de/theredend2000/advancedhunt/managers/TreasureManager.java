package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.bukkit.Location;

public class TreasureManager {
   private final DataRepository repository;
   private volatile Map<Long, List<TreasureCore>> treasureChunkMap = new ConcurrentHashMap<>();
   private volatile Map<UUID, List<TreasureCore>> collectionTreasureMap = new ConcurrentHashMap<>();
   private volatile Map<UUID, UUID> treasureToCollectionIndex = new ConcurrentHashMap<>();
   private volatile Map<UUID, TreasureCore> treasureCoreById = new ConcurrentHashMap<>();
   private final Cache<UUID, Treasure> fullTreasureCache;
   private final Object idGenerationLock = new Object();

   public TreasureManager(DataRepository repository) {
      this.repository = repository;
      this.fullTreasureCache = Caffeine.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).maximumSize(500L).build();
   }

   public UUID generateUniqueTreasureId() {
      synchronized (this.idGenerationLock) {
         UUID id;
         do {
            id = UUID.randomUUID();
         } while (this.treasureCoreById.containsKey(id));

         return id;
      }
   }

   public void loadTreasures() {
      this.repository.loadTreasureCores().thenAccept(cores -> {
         Map<Long, List<TreasureCore>> newChunkMap = new ConcurrentHashMap<>();
         Map<UUID, List<TreasureCore>> newCollectionMap = new ConcurrentHashMap<>();
         Map<UUID, UUID> newTreasureToCollection = new ConcurrentHashMap<>();
         Map<UUID, TreasureCore> newCoreById = new ConcurrentHashMap<>();
         if (cores != null) {
            for (TreasureCore core : cores) {
               long key = this.getChunkKey(core.getLocation());
               newChunkMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(core);
               newCollectionMap.computeIfAbsent(core.getCollectionId(), k -> new CopyOnWriteArrayList<>()).add(core);
               newTreasureToCollection.put(core.getId(), core.getCollectionId());
               newCoreById.put(core.getId(), core);
            }
         }

         this.treasureChunkMap = newChunkMap;
         this.collectionTreasureMap = newCollectionMap;
         this.treasureToCollectionIndex = newTreasureToCollection;
         this.treasureCoreById = newCoreById;
         this.fullTreasureCache.invalidateAll();
      });
   }

   public void addTreasure(Treasure t) {
      this.addTreasureAsync(t);
   }

   public CompletableFuture<Void> addTreasureAsync(Treasure t) {
      return this.repository.saveTreasure(t).thenRun(() -> {
         TreasureCore core = TreasureCore.from(t);
         this.addCoreToCache(core);
         this.fullTreasureCache.put(t.getId(), t);
      });
   }

   public CompletableFuture<Void> addTreasuresBatchAsync(List<Treasure> treasures) {
      return treasures != null && !treasures.isEmpty() ? this.repository.saveTreasuresBatch(treasures).thenRun(() -> {
         for (Treasure t : treasures) {
            TreasureCore core = TreasureCore.from(t);
            this.addCoreToCache(core);
            this.fullTreasureCache.put(t.getId(), t);
         }
      }) : CompletableFuture.completedFuture(null);
   }

   public void deleteTreasure(Treasure t) {
      this.repository.deleteTreasure(t.getId()).thenRun(() -> {
         this.removeCoreFromCache(t.getId(), t.getCollectionId(), t.getLocation());
         this.fullTreasureCache.invalidate(t.getId());
      });
   }

   public void deleteTreasure(TreasureCore core) {
      this.repository.deleteTreasure(core.getId()).thenRun(() -> {
         this.removeCoreFromCache(core.getId(), core.getCollectionId(), core.getLocation());
         this.fullTreasureCache.invalidate(core.getId());
      });
   }

   public void removeCollection(UUID collectionId) {
      List<TreasureCore> cores = this.collectionTreasureMap.remove(collectionId);
      if (cores != null) {
         for (TreasureCore core : cores) {
            long key = this.getChunkKey(core.getLocation());
            List<TreasureCore> chunkList = this.treasureChunkMap.get(key);
            if (chunkList != null) {
               chunkList.removeIf(c -> c.getId().equals(core.getId()));
            }

            this.treasureToCollectionIndex.remove(core.getId());
            this.treasureCoreById.remove(core.getId());
            this.fullTreasureCache.invalidate(core.getId());
         }
      }
   }

   public CompletableFuture<Void> updateTreasure(Treasure oldTreasure, Treasure newTreasure) {
      return this.repository.saveTreasure(newTreasure).thenRun(() -> {
         this.removeCoreFromCache(oldTreasure.getId(), oldTreasure.getCollectionId(), oldTreasure.getLocation());
         TreasureCore core = TreasureCore.from(newTreasure);
         this.addCoreToCache(core);
         this.fullTreasureCache.put(newTreasure.getId(), newTreasure);
         if (!oldTreasure.getId().equals(newTreasure.getId())) {
            this.fullTreasureCache.invalidate(oldTreasure.getId());
         }
      });
   }

   public CompletableFuture<Void> updateTreasureRewards(UUID treasureId, List<Reward> rewards) {
      return this.repository
         .updateTreasureRewards(treasureId, rewards)
         .thenRun(
            () -> {
               Treasure cached = this.fullTreasureCache.getIfPresent(treasureId);
               if (cached != null) {
                  Treasure updated = new Treasure(
                     cached.getId(), cached.getCollectionId(), cached.getLocation(), rewards, cached.getNbtData(), cached.getMaterial(), cached.getBlockState()
                  );
                  this.fullTreasureCache.put(treasureId, updated);
               }
            }
         );
   }

   public CompletableFuture<Integer> overrideTreasureRewardsInCollection(
      UUID collectionId, List<Reward> rewards, int batchSize, BiConsumer<Integer, Integer> progressCallback
   ) {
      List<TreasureCore> cores = this.getTreasureCoresInCollection(collectionId);
      if (cores.isEmpty()) {
         return CompletableFuture.completedFuture(0);
      } else {
         int total = cores.size();
         int effectiveBatchSize = Math.max(1, batchSize);
         int[] completed = new int[]{0};
         int[] nextPercentToReport = new int[]{0};
         CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

         for (int i = 0; i < cores.size(); i += effectiveBatchSize) {
            int start = i;
            int end = Math.min(i + effectiveBatchSize, cores.size());
            chain = chain.thenCompose(
               v -> {
                  int size = end - start;
                  List<UUID> batchIds = new ArrayList<>(size);

                  for (int j = start; j < end; j++) {
                     batchIds.add(cores.get(j).getId());
                  }

                  return this.repository
                     .updateTreasureRewardsBatch(batchIds, rewards)
                     .thenRun(
                        () -> {
                           for (UUID treasureId : batchIds) {
                              Treasure cached = this.fullTreasureCache.getIfPresent(treasureId);
                              if (cached != null) {
                                 Treasure updated = new Treasure(
                                    cached.getId(),
                                    cached.getCollectionId(),
                                    cached.getLocation(),
                                    rewards,
                                    cached.getNbtData(),
                                    cached.getMaterial(),
                                    cached.getBlockState()
                                 );
                                 this.fullTreasureCache.put(treasureId, updated);
                              }
                           }

                           completed[0] += size;
                           if (progressCallback != null && total > 0) {
                              int percent = (int)(completed[0] * 100L / total);

                              while (percent >= nextPercentToReport[0] && nextPercentToReport[0] < 100) {
                                 progressCallback.accept(nextPercentToReport[0], completed[0]);
                                 nextPercentToReport[0]++;
                              }
                           }
                        }
                     );
               }
            );
         }

         return chain.thenApply(v -> total);
      }
   }

   private void addCoreToCache(TreasureCore core) {
      long key = this.getChunkKey(core.getLocation());
      this.treasureChunkMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(core);
      this.collectionTreasureMap.computeIfAbsent(core.getCollectionId(), k -> new CopyOnWriteArrayList<>()).add(core);
      this.treasureToCollectionIndex.put(core.getId(), core.getCollectionId());
      this.treasureCoreById.put(core.getId(), core);
   }

   private void removeCoreFromCache(UUID treasureId, UUID collectionId, Location location) {
      long key = this.getChunkKey(location);
      List<TreasureCore> chunkList = this.treasureChunkMap.get(key);
      if (chunkList != null) {
         chunkList.removeIf(core -> core.getId().equals(treasureId));
      }

      List<TreasureCore> collectionList = this.collectionTreasureMap.get(collectionId);
      if (collectionList != null) {
         collectionList.removeIf(core -> core.getId().equals(treasureId));
      }

      this.treasureToCollectionIndex.remove(treasureId);
      this.treasureCoreById.remove(treasureId);
   }

   public List<TreasureCore> getTreasureCoresInCollection(UUID collectionId) {
      return this.collectionTreasureMap.getOrDefault(collectionId, Collections.emptyList());
   }

   public List<TreasureCore> getTreasureCoresInChunk(int chunkX, int chunkZ) {
      long key = (long)chunkX << 32 | chunkZ & 4294967295L;
      return this.treasureChunkMap.getOrDefault(key, Collections.emptyList());
   }

   public TreasureCore getTreasureCoreAt(Location loc) {
      long key = this.getChunkKey(loc);
      List<TreasureCore> inChunk = this.treasureChunkMap.get(key);
      if (inChunk == null) {
         return null;
      } else {
         for (TreasureCore core : inChunk) {
            if (this.locationsEqual(core.getLocation(), loc)) {
               return core;
            }
         }

         return null;
      }
   }

   public TreasureCore getTreasureCoreById(UUID treasureId) {
      return this.treasureCoreById.get(treasureId);
   }

   @Deprecated
   public Treasure getTreasureAt(Location loc) {
      TreasureCore core = this.getTreasureCoreAt(loc);
      return core == null ? null : this.getFullTreasure(core.getId());
   }

   public Treasure getFullTreasure(UUID treasureId) {
      Treasure cached = this.fullTreasureCache.getIfPresent(treasureId);
      if (cached != null) {
         return cached;
      } else {
         Treasure loaded = this.repository.loadTreasure(treasureId).join();
         if (loaded != null) {
            this.fullTreasureCache.put(treasureId, loaded);
         }

         return loaded;
      }
   }

   public CompletableFuture<Treasure> getFullTreasureAsync(UUID treasureId) {
      Treasure cached = this.fullTreasureCache.getIfPresent(treasureId);
      return cached != null ? CompletableFuture.completedFuture(cached) : this.repository.loadTreasure(treasureId).thenApply(loaded -> {
         if (loaded != null) {
            this.fullTreasureCache.put(treasureId, loaded);
         }

         return (Treasure)loaded;
      });
   }

   @Deprecated
   public List<Treasure> getTreasuresInCollection(UUID collectionId) {
      List<TreasureCore> cores = this.getTreasureCoresInCollection(collectionId);
      List<Treasure> treasures = new ArrayList<>(cores.size());

      for (TreasureCore core : cores) {
         Treasure full = this.getFullTreasure(core.getId());
         if (full != null) {
            treasures.add(full);
         }
      }

      return treasures;
   }

   @Deprecated
   public List<Treasure> getTreasuresInChunk(int chunkX, int chunkZ) {
      List<TreasureCore> cores = this.getTreasureCoresInChunk(chunkX, chunkZ);
      List<Treasure> treasures = new ArrayList<>(cores.size());

      for (TreasureCore core : cores) {
         Treasure full = this.getFullTreasure(core.getId());
         if (full != null) {
            treasures.add(full);
         }
      }

      return treasures;
   }

   private boolean locationsEqual(Location l1, Location l2) {
      return l1 != null && l2 != null && l1.getWorld() != null && l2.getWorld() != null
         ? l1.getWorld().getName().equals(l2.getWorld().getName())
            && l1.getBlockX() == l2.getBlockX()
            && l1.getBlockY() == l2.getBlockY()
            && l1.getBlockZ() == l2.getBlockZ()
         : false;
   }

   private long getChunkKey(Location loc) {
      return (long)(loc.getBlockX() >> 4) << 32 | loc.getBlockZ() >> 4 & 4294967295L;
   }

   public UUID getCollectionIdForTreasure(UUID treasureId) {
      return this.treasureToCollectionIndex.get(treasureId);
   }

   public Set<UUID> filterFoundByCollection(Set<UUID> foundTreasures, UUID collectionId) {
      Set<UUID> result = new HashSet<>();

      for (UUID treasureId : foundTreasures) {
         UUID treasureCollectionId = this.treasureToCollectionIndex.get(treasureId);
         if (collectionId.equals(treasureCollectionId)) {
            result.add(treasureId);
         }
      }

      return result;
   }

   public int countFoundInCollection(Set<UUID> foundTreasures, UUID collectionId) {
      int count = 0;

      for (UUID treasureId : foundTreasures) {
         if (collectionId.equals(this.treasureToCollectionIndex.get(treasureId))) {
            count++;
         }
      }

      return count;
   }

   public Set<UUID> getTreasureIdsInCollection(UUID collectionId) {
      Set<UUID> result = new HashSet<>();

      for (Entry<UUID, UUID> entry : this.treasureToCollectionIndex.entrySet()) {
         if (collectionId.equals(entry.getValue())) {
            result.add(entry.getKey());
         }
      }

      return result;
   }

   public int getTreasureCount() {
      return this.treasureCoreById.size();
   }

   public long getCachedFullTreasureCount() {
      return this.fullTreasureCache.estimatedSize();
   }
}
