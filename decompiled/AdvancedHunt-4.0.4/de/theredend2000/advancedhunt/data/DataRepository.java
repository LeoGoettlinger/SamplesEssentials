package de.theredend2000.advancedhunt.data;

import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface DataRepository {
   void init();

   void reload();

   void shutdown();

   int getSchemaVersion();

   void upgradeSchema();

   CompletableFuture<PlayerData> loadPlayerData(UUID var1);

   CompletableFuture<Void> savePlayerData(PlayerData var1);

   CompletableFuture<Void> savePlayerDataBatch(List<PlayerData> var1);

   CompletableFuture<List<PlayerData>> loadAllPlayerData();

   CompletableFuture<List<UUID>> getAllPlayerUUIDs();

   CompletableFuture<List<Treasure>> loadTreasures();

   default CompletableFuture<List<UUID>> getAllTreasureUUIDs() {
      return this.loadTreasureCores()
         .thenApply(cores -> cores != null && !cores.isEmpty() ? cores.stream().map(TreasureCore::getId).collect(Collectors.toList()) : Collections.emptyList());
   }

   CompletableFuture<List<TreasureCore>> loadTreasureCores();

   CompletableFuture<Void> saveTreasure(Treasure var1);

   default CompletableFuture<Void> saveTreasuresBatch(List<Treasure> treasures) {
      if (treasures != null && !treasures.isEmpty()) {
         CompletableFuture<?>[] futures = treasures.stream().map(this::saveTreasure).toArray(CompletableFuture[]::new);
         return CompletableFuture.allOf(futures);
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   CompletableFuture<Void> deleteTreasure(UUID var1);

   CompletableFuture<Integer> deleteTreasuresInCollection(UUID var1);

   CompletableFuture<Treasure> loadTreasure(UUID var1);

   CompletableFuture<Void> updateTreasureRewards(UUID var1, List<Reward> var2);

   default CompletableFuture<Void> updateTreasureRewardsBatch(List<UUID> treasureIds, List<Reward> rewards) {
      if (treasureIds != null && !treasureIds.isEmpty()) {
         CompletableFuture<?>[] futures = new CompletableFuture[treasureIds.size()];

         for (int i = 0; i < treasureIds.size(); i++) {
            futures[i] = this.updateTreasureRewards(treasureIds.get(i), rewards);
         }

         return CompletableFuture.allOf(futures);
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   CompletableFuture<List<Collection>> loadCollections();

   CompletableFuture<Void> saveCollection(Collection var1);

   CompletableFuture<Void> deleteCollection(UUID var1);

   CompletableFuture<Boolean> renameCollection(UUID var1, String var2);

   CompletableFuture<Integer> resetAllProgress();

   CompletableFuture<Integer> resetCollectionProgress(UUID var1);

   CompletableFuture<Integer> resetPlayerProgress(UUID var1);

   CompletableFuture<Integer> resetPlayerCollectionProgress(UUID var1, UUID var2);

   CompletableFuture<Map<UUID, Integer>> getLeaderboard(UUID var1, int var2);

   CompletableFuture<List<UUID>> getPlayersWhoFound(UUID var1);

   default CompletableFuture<Integer> getFoundPlayerCount(UUID treasureId) {
      return this.getPlayersWhoFound(treasureId).thenApply(list -> list == null ? 0 : list.size());
   }

   CompletableFuture<Set<UUID>> getPlayerFoundInCollection(UUID var1, UUID var2);

   CompletableFuture<List<RewardPreset>> loadRewardPresets(RewardPresetType var1);

   CompletableFuture<Void> saveRewardPreset(RewardPreset var1);

   CompletableFuture<Void> deleteRewardPreset(RewardPresetType var1, UUID var2);

   CompletableFuture<List<PlaceItem>> loadPlaceItems();

   CompletableFuture<Void> savePlaceItem(PlaceItem var1);

   CompletableFuture<Void> deletePlaceItem(UUID var1);

   CompletableFuture<Set<String>> loadPlaceItemGroups();

   CompletableFuture<Void> createPlaceItemGroup(String var1);

   CompletableFuture<Void> renamePlaceItemGroup(String var1, String var2);

   CompletableFuture<Void> deletePlaceItemGroup(String var1);

   default CompletableFuture<Void> saveRewardPresetsBatch(List<RewardPreset> presets) {
      if (presets != null && !presets.isEmpty()) {
         CompletableFuture<?>[] futures = presets.stream().map(this::saveRewardPreset).toArray(CompletableFuture[]::new);
         return CompletableFuture.allOf(futures);
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   default void flushIndexes() {
   }

   default void rebuildLeaderboardCache() {
   }
}
