package com.oheers.fish.fishing;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.events.EMFFishHuntEvent;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;

public class EMFFishListener implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEMFFishCatch(EMFFishCaughtEvent event) {
      this.handleFishEvent(event.getPlayer(), event.getFish(), event.getCatchTime());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEMFFishHunt(EMFFishHuntEvent event) {
      this.handleFishEvent(event.getPlayer(), event.getFish(), event.getHuntTime());
   }

   private void handleFishEvent(Player player, IFish fish, LocalDateTime catchTime) {
      if (DatabaseUtil.isDatabaseOnline()) {
         if (!fish.isTrackInDatabase()) {
            Logging.debug("Not tracking fish " + RarityKey.of(fish) + " in the database.");
         } else {
            UUID uuid = player.getUniqueId();
            Competition active = CompetitionManager.getInstance().getActiveCompetition();
            String competitionId = active == null ? null : active.getCompetitionName();
            EvenMoreFish.getInstance().getPluginDataManager().getDatabaseWorker().execute(() -> {
               int userId = EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(uuid);
               if (userId == 0) {
                  EvenMoreFish.getInstance().getLogger().warning("Skipping fish database update because user id could not be resolved for " + uuid);
               } else {
                  this.handleFishLog(userId, fish, catchTime, competitionId);
                  this.handleUserFishStats(userId, fish);
                  this.handleFishStats(fish);
                  this.handleUserReport(uuid, fish);
               }
            });
         }
      }
   }

   private void handleUserReport(final UUID uuid, IFish fish) {
      DataManager<UserReport> userReportDataManager = EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager();
      UserReport userReport = userReportDataManager.get(
         String.valueOf(uuid), key -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getUserReport(uuid)
      );
      if (userReport.getShortestLength() == -1.0F || userReport.getShortestLength() > fish.getLength()) {
         userReport.setShortestLengthAndFish(fish);
      }

      if (userReport.getLargestLength() < fish.getLength()) {
         userReport.setLongestLengthAndFish(fish);
      }

      if (userReport.getFirstFish().toString().equals(".")) {
         userReport.setFirstFish(FishRarityKey.of(fish));
      }

      userReport.setRecentFish(FishRarityKey.of(fish));
      userReport.incrementFishCaught(1);
      userReport.incrementTotalLength(fish.getLength());
      userReportDataManager.update(String.valueOf(uuid), userReport);
      EvenMoreFish.getInstance().debug("Saving user report %s".formatted(userReport.toString()));
   }

   private void handleFishStats(@NonNull final IFish fish) {
      DataManager<FishStats> fishStatsDataManager = EvenMoreFish.getInstance().getPluginDataManager().getFishStatsDataManager();
      FishRarityKey fishRarityKey = FishRarityKey.of(fish);
      String key = fishRarityKey.toString();
      FishStats stats = fishStatsDataManager.peek(key);
      if (stats == null) {
         if (EvenMoreFish.getInstance().getPluginDataManager().isFishStatsPreloaded()) {
            stats = FishStats.empty(fish, LocalDateTime.now());
         } else {
            Database.LoadResult<FishStats> loadResult = EvenMoreFish.getInstance()
               .getPluginDataManager()
               .getDatabase()
               .loadFishStats(fish.getId(), fish.getRarity().getId());
            if (loadResult.isUnreadable()) {
               EvenMoreFish.getInstance().getLogger().warning("Skipping fish stats update for " + key + " because the stored row could not be read.");
               return;
            }

            if (loadResult.isFound()) {
               stats = loadResult.getValue();
               fishStatsDataManager.cacheLoadedValue(key, stats);
            } else {
               stats = FishStats.empty(fish, LocalDateTime.now());
            }
         }
      }

      UUID fishermanUuid = fish.getFishermanUUID();
      float length = fish.getLength();
      if (fishermanUuid != null) {
         if (stats.getLongestLength() < length) {
            stats.setLongestLength(length);
            stats.setLongestFisher(fishermanUuid);
         }

         if (stats.getShortestLength() > length) {
            stats.setShortestLength(length);
            stats.setShortestFisher(fishermanUuid);
         }
      }

      stats.incrementQuantity();
      fishStatsDataManager.update(key, stats);
      EvenMoreFish.getInstance().debug("Fish Stats: %s".formatted(stats.toString()));
   }

   private void handleFishLog(final int userId, final IFish fish, final LocalDateTime catchTime, final String competitionId) {
      DataManager<Collection<FishLog>> fishLogDataManager = EvenMoreFish.getInstance().getPluginDataManager().getFishLogDataManager();
      FishLog log = new FishLog(userId, fish, catchTime, competitionId);
      String key = UserFishRarityKey.of(userId, fish).toString();
      fishLogDataManager.update(key, Collections.singletonList(log));
   }

   private void handleUserFishStats(final int userId, @NonNull final IFish fish) {
      DataManager<UserFishStats> userFishStatsDataManager = EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager();
      String key = UserFishRarityKey.of(userId, fish).toString();
      UserFishStats stats = userFishStatsDataManager.peek(key);
      if (stats == null) {
         if (EvenMoreFish.getInstance().getPluginDataManager().isUserFishStatsPreloaded(userId)) {
            stats = new UserFishStats(userId, fish, LocalDateTime.now());
         } else {
            Database.LoadResult<UserFishStats> loadResult = EvenMoreFish.getInstance()
               .getPluginDataManager()
               .getDatabase()
               .loadUserFishStats(userId, fish.getId(), fish.getRarity().getId());
            if (loadResult.isUnreadable()) {
               EvenMoreFish.getInstance().getLogger().warning("Skipping user fish stats update for " + key + " because the stored row could not be read.");
               return;
            }

            if (loadResult.isFound()) {
               stats = loadResult.getValue();
               userFishStatsDataManager.cacheLoadedValue(key, stats);
            } else {
               stats = new UserFishStats(userId, fish, LocalDateTime.now());
            }
         }
      }

      if (stats.getLongestLength() < fish.getLength()) {
         stats.setLongestLength(fish.getLength());
      }

      if (stats.getShortestLength() > fish.getLength()) {
         stats.setShortestLength(fish.getLength());
      }

      stats.incrementQuantity();
      userFishStatsDataManager.update(key, stats);
   }
}
