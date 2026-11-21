package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.Treasure;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MigrationService {
   private final Logger logger;

   public MigrationService(Logger logger) {
      this.logger = logger;
   }

   public CompletableFuture<Void> migrate(DataRepository source, DataRepository target) {
      return this.migrate(source, target, null);
   }

   public CompletableFuture<Void> migrate(DataRepository source, DataRepository target, Consumer<MigrationService.MigrationProgress> progressCallback) {
      this.logger.info("Starting migration...");
      if (progressCallback != null) {
         progressCallback.accept(new MigrationService.MigrationProgress(0, "loading", 0, 0));
      }

      CompletableFuture<List<Collection>> collectionsFuture = source.loadCollections();
      CompletableFuture<List<UUID>> treasureIdsFuture = source.getAllTreasureUUIDs();
      CompletableFuture<List<UUID>> playerUUIDsFuture = source.getAllPlayerUUIDs();
      CompletableFuture<List<RewardPreset>> treasurePresetsFuture = source.loadRewardPresets(RewardPresetType.TREASURE);
      CompletableFuture<List<RewardPreset>> collectionPresetsFuture = source.loadRewardPresets(RewardPresetType.COLLECTION);
      if (progressCallback != null) {
         collectionsFuture.thenRun(() -> progressCallback.accept(new MigrationService.MigrationProgress(0, "loaded_collections", 0, 0)));
         treasureIdsFuture.thenRun(() -> progressCallback.accept(new MigrationService.MigrationProgress(0, "loaded_treasure_ids", 0, 0)));
         playerUUIDsFuture.thenRun(() -> progressCallback.accept(new MigrationService.MigrationProgress(0, "loaded_player_index", 0, 0)));
         treasurePresetsFuture.thenRun(() -> progressCallback.accept(new MigrationService.MigrationProgress(0, "loaded_treasure_presets", 0, 0)));
         collectionPresetsFuture.thenRun(() -> progressCallback.accept(new MigrationService.MigrationProgress(0, "loaded_collection_presets", 0, 0)));
      }

      return CompletableFuture.allOf(collectionsFuture, treasureIdsFuture, playerUUIDsFuture, treasurePresetsFuture, collectionPresetsFuture)
         .thenCompose(
            v -> {
               List<Collection> collections = collectionsFuture.join();
               List<UUID> treasureIds = treasureIdsFuture.join();
               List<UUID> playerUUIDs = playerUUIDsFuture.join();
               List<RewardPreset> treasurePresets = treasurePresetsFuture.join();
               List<RewardPreset> collectionPresets = collectionPresetsFuture.join();
               int totalWork = collections.size() + treasureIds.size() + playerUUIDs.size() + treasurePresets.size() + collectionPresets.size();
               MigrationService.ProgressTracker tracker = new MigrationService.ProgressTracker(totalWork, progressCallback);
               tracker.report(0, "starting", 0);
               this.logger
                  .info(
                     "Migrating "
                        + collections.size()
                        + " collections, "
                        + treasureIds.size()
                        + " treasures, and "
                        + playerUUIDs.size()
                        + " player data entries, plus "
                        + treasurePresets.size()
                        + " treasure reward presets and "
                        + collectionPresets.size()
                        + " collection reward presets."
                  );
               return this.migrateRewardPresetsInBatches(target, treasurePresets, tracker, "reward_presets")
                  .thenCompose(v2 -> this.migrateRewardPresetsInBatches(target, collectionPresets, tracker, "reward_presets"))
                  .thenCompose(v2 -> this.migrateCollectionsInBatches(target, collections, tracker))
                  .thenCompose(v2 -> this.migrateTreasuresInChunks(source, target, treasureIds, tracker))
                  .thenCompose(v2 -> this.migratePlayersInChunks(source, target, playerUUIDs, tracker))
                  .thenRun(() -> tracker.report(100, "done", tracker.current()));
            }
         )
         .thenRun(() -> this.logger.info("Migration completed successfully!"));
   }

   private CompletableFuture<Void> migrateCollectionsInBatches(DataRepository target, List<Collection> collections, MigrationService.ProgressTracker tracker) {
      if (collections != null && !collections.isEmpty()) {
         int batchSize = 25;
         CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

         for (int i = 0; i < collections.size(); i += 25) {
            int end = Math.min(i + 25, collections.size());
            List<Collection> batch = collections.subList(i, end);
            chain = chain.thenCompose(v -> {
               CompletableFuture<?>[] futures = batch.stream().map(target::saveCollection).toArray(CompletableFuture[]::new);
               return CompletableFuture.allOf(futures).thenRun(() -> tracker.increment("collections", batch.size()));
            });
         }

         return chain;
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   private CompletableFuture<Void> migrateRewardPresetsInBatches(
      DataRepository target, List<RewardPreset> presets, MigrationService.ProgressTracker tracker, String stage
   ) {
      return presets != null && !presets.isEmpty()
         ? target.saveRewardPresetsBatch(presets).thenRun(() -> tracker.increment(stage, presets.size()))
         : CompletableFuture.completedFuture(null);
   }

   private CompletableFuture<Void> migrateTreasuresInChunks(
      DataRepository source, DataRepository target, List<UUID> treasureIds, MigrationService.ProgressTracker tracker
   ) {
      if (treasureIds != null && !treasureIds.isEmpty()) {
         int chunkSize = 50;
         CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
         int totalChunks = (treasureIds.size() + 50 - 1) / 50;

         for (int start = 0; start < treasureIds.size(); start += 50) {
            int end = Math.min(start + 50, treasureIds.size());
            int currentChunk = start / 50 + 1;
            List<UUID> chunk = treasureIds.subList(start, end);
            future = future.thenCompose(v -> {
               List<CompletableFuture<Treasure>> loadFutures = new ArrayList<>();

               for (UUID id : chunk) {
                  loadFutures.add(source.loadTreasure(id));
               }

               return CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[0])).thenCompose(v2 -> {
                  List<Treasure> loadedTreasures = new ArrayList<>(chunk.size());
                  int missing = 0;

                  for (CompletableFuture<Treasure> f : loadFutures) {
                     Treasure t = f.join();
                     if (t != null) {
                        loadedTreasures.add(t);
                     } else {
                        missing++;
                     }
                  }

                  this.logger.info("Migrating treasure chunk " + currentChunk + "/" + totalChunks + (missing > 0 ? " (missing: " + missing + ")" : ""));
                  if (loadedTreasures.isEmpty()) {
                     tracker.increment("treasures", chunk.size());
                     return CompletableFuture.completedFuture(null);
                  } else {
                     return target.saveTreasuresBatch(loadedTreasures).thenRun(() -> tracker.increment("treasures", chunk.size()));
                  }
               });
            });
         }

         return future;
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   private CompletableFuture<Void> migratePlayersInChunks(
      DataRepository source, DataRepository target, List<UUID> playerUUIDs, MigrationService.ProgressTracker tracker
   ) {
      int chunkSize = 100;
      CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
      int totalChunks = (playerUUIDs.size() + chunkSize - 1) / chunkSize;

      for (int i = 0; i < playerUUIDs.size(); i += chunkSize) {
         int end = Math.min(i + chunkSize, playerUUIDs.size());
         int currentChunk = i / chunkSize + 1;
         List<UUID> chunk = playerUUIDs.subList(i, end);
         future = future.thenCompose(v -> {
            List<CompletableFuture<PlayerData>> loadFutures = new ArrayList<>();

            for (UUID uuid : chunk) {
               loadFutures.add(source.loadPlayerData(uuid));
            }

            return CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[0])).thenCompose(v2 -> {
               List<PlayerData> loadedData = new ArrayList<>(chunk.size());
               int missing = 0;

               for (CompletableFuture<PlayerData> f : loadFutures) {
                  PlayerData data = f.join();
                  if (data != null) {
                     loadedData.add(data);
                  } else {
                     missing++;
                  }
               }

               this.logger.info("Migrating player chunk " + currentChunk + "/" + totalChunks + (missing > 0 ? " (missing: " + missing + ")" : ""));
               if (loadedData.isEmpty()) {
                  tracker.increment("players", chunk.size());
                  return CompletableFuture.completedFuture(null);
               } else {
                  return target.savePlayerDataBatch(loadedData).thenRun(() -> tracker.increment("players", chunk.size()));
               }
            });
         });
      }

      return future;
   }

   public static final class MigrationProgress {
      private final int percent;
      private final String stage;
      private final int current;
      private final int total;

      public MigrationProgress(int percent, String stage, int current, int total) {
         this.percent = percent;
         this.stage = stage;
         this.current = current;
         this.total = total;
      }

      public int percent() {
         return this.percent;
      }

      public String stage() {
         return this.stage;
      }

      public int current() {
         return this.current;
      }

      public int total() {
         return this.total;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof MigrationService.MigrationProgress)) {
            return false;
         } else {
            MigrationService.MigrationProgress that = (MigrationService.MigrationProgress)o;
            return this.percent == that.percent && this.current == that.current && this.total == that.total && Objects.equals(this.stage, that.stage);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.percent, this.stage, this.current, this.total);
      }

      @Override
      public String toString() {
         return "MigrationProgress{percent=" + this.percent + ", stage='" + this.stage + '\'' + ", current=" + this.current + ", total=" + this.total + '}';
      }
   }

   private static final class ProgressTracker {
      private final int total;
      private final Consumer<MigrationService.MigrationProgress> callback;
      private final AtomicInteger current = new AtomicInteger(0);
      private final AtomicInteger nextPercentToReport = new AtomicInteger(1);

      private ProgressTracker(int total, Consumer<MigrationService.MigrationProgress> callback) {
         this.total = Math.max(0, total);
         this.callback = callback;
      }

      private int current() {
         return this.current.get();
      }

      private void increment(String stage, int delta) {
         if (delta > 0) {
            int updated = this.current.addAndGet(delta);
            this.reportIfNeeded(stage, updated);
         }
      }

      private void report(int percent, String stage, int currentValue) {
         if (this.callback != null) {
            this.callback.accept(new MigrationService.MigrationProgress(clamp(percent, 0, 100), stage, currentValue, this.total));
         }
      }

      private void reportIfNeeded(String stage, int currentValue) {
         if (this.callback != null) {
            if (this.total > 0) {
               int percent = (int)Math.floor(currentValue * 100.0 / this.total);
               int next = this.nextPercentToReport.get();
               if (percent >= next) {
                  while (percent >= next && next < 100) {
                     if (this.nextPercentToReport.compareAndSet(next, next + 1)) {
                        this.report(next, stage, currentValue);
                        next++;
                     } else {
                        next = this.nextPercentToReport.get();
                     }
                  }
               }
            }
         }
      }

      private static int clamp(int value, int min, int max) {
         return Math.max(min, Math.min(max, value));
      }
   }
}
