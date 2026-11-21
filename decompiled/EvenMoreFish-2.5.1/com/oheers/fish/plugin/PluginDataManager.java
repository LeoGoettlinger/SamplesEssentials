package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.FishLogKey;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.data.manager.UserManager;
import com.oheers.fish.database.data.strategy.impl.CompetitionSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.FishLogSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.FishStatsSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.UserFishStatsSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.UserReportsSavingStrategy;
import com.oheers.fish.database.execute.DatabaseWorker;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PluginDataManager {
   private final EvenMoreFish plugin;
   private Database database;
   private UserManager userManager;
   private DatabaseWorker databaseWorker;
   private final Set<Integer> preloadedUserFishStats = ConcurrentHashMap.newKeySet();
   private volatile boolean fishStatsPreloaded;
   private DataManager<Collection<FishLog>> fishLogDataManager;
   private DataManager<FishStats> fishStatsDataManager;
   private DataManager<UserFishStats> userFishStatsDataManager;
   private DataManager<UserReport> userReportDataManager;
   private DataManager<CompetitionReport> competitionDataManager;

   public PluginDataManager(EvenMoreFish plugin) {
      this.plugin = plugin;
      this.init();
   }

   public void init() {
      if (!MainConfig.getInstance().databaseEnabled()) {
         this.plugin.getLogger().info("Database is disabled in config");
      } else {
         this.database = new Database();
         this.databaseWorker = new DatabaseWorker();
         this.initDataManagers();
         this.databaseWorker.execute(this::preloadFishStats);
      }
   }

   public void initDataManagers() {
      this.userManager = new UserManager(this.database, this.databaseWorker, this::preloadUserData);
      this.fishLogDataManager = new DataManager<>(new FishLogSavingStrategy(this.databaseWorker), key -> {
         FishLogKey logKey = FishLogKey.from(key);
         return Collections.singleton(this.database.getFishLog(logKey.getUserId(), logKey.getFishName(), logKey.getFishRarity(), logKey.getDateTime()));
      });
      this.fishStatsDataManager = new DataManager<>(new FishStatsSavingStrategy(this.databaseWorker), key -> {
         FishRarityKey fishRarityKey = FishRarityKey.from(key);
         return this.database.getFishStats(fishRarityKey.fishName(), fishRarityKey.fishRarity());
      });
      this.userFishStatsDataManager = new DataManager<>(new UserFishStatsSavingStrategy(this.databaseWorker), key -> {
         UserFishRarityKey userFishRarityKey = UserFishRarityKey.from(key);
         return this.database.getUserFishStats(userFishRarityKey.userId(), userFishRarityKey.fishName(), userFishRarityKey.fishRarity());
      }, (long)MainConfig.getInstance().getUserFishStatsSaveInterval(), TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit()));
      this.userReportDataManager = new DataManager<>(
         new UserReportsSavingStrategy(this.databaseWorker), uuid -> this.database.getUserReport(UUID.fromString(uuid))
      );
      this.competitionDataManager = new DataManager<>(
         new CompetitionSavingStrategy(this.databaseWorker),
         key -> this.database.getCompetitionReport(Integer.parseInt(key)),
         (long)MainConfig.getInstance().getCompetitionSaveInterval(),
         TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
      );
   }

   public void shutdown() {
      if (this.database != null && DatabaseUtil.isDatabaseOnline()) {
         try {
            this.userReportDataManager.shutdown();
            this.userFishStatsDataManager.shutdown();
            this.fishStatsDataManager.shutdown();
            this.fishLogDataManager.shutdown();
            this.competitionDataManager.shutdown();
            if (this.databaseWorker != null) {
               this.databaseWorker.shutdown(60L, TimeUnit.SECONDS);
            }

            this.database.shutdown();
            this.plugin.getLogger().info("Database connections closed successfully");
         } catch (Exception var2) {
            this.plugin.getLogger().log(Level.WARNING, "Error while shutting down database", (Throwable)var2);
         }
      }
   }

   public UserManager getUserManager() {
      return this.userManager;
   }

   public DataManager<Collection<FishLog>> getFishLogDataManager() {
      return this.fishLogDataManager;
   }

   public DataManager<FishStats> getFishStatsDataManager() {
      return this.fishStatsDataManager;
   }

   public DataManager<UserFishStats> getUserFishStatsDataManager() {
      return this.userFishStatsDataManager;
   }

   public DataManager<UserReport> getUserReportDataManager() {
      return this.userReportDataManager;
   }

   public DataManager<CompetitionReport> getCompetitionDataManager() {
      return this.competitionDataManager;
   }

   public Database getDatabase() {
      return this.database;
   }

   public DatabaseWorker getDatabaseWorker() {
      return this.databaseWorker;
   }

   public boolean isFishStatsPreloaded() {
      return this.fishStatsPreloaded;
   }

   public boolean isUserFishStatsPreloaded(int userId) {
      return this.preloadedUserFishStats.contains(userId);
   }

   public CompletableFuture<Integer> preloadUserDataAsync(UUID uuid) {
      if (this.databaseWorker == null) {
         return CompletableFuture.completedFuture(0);
      } else {
         this.plugin.debug("Queueing user data preload for %s.".formatted(uuid));
         return this.databaseWorker.query(() -> {
            this.plugin.debug("Preloading user data for %s.".formatted(uuid));
            int userId = this.userManager.getUserId(uuid);
            if (userId != 0 && !this.isUserFishStatsPreloaded(userId)) {
               this.preloadUserData(uuid, userId);
            }

            this.plugin.debug("Finished user data preload for %s with user id %d.".formatted(uuid, userId));
            return userId;
         });
      }
   }

   private void preloadUserData(UUID uuid, int userId) {
      UserReport report = this.database.getUserReport(uuid);
      if (report != null) {
         this.userReportDataManager.cacheLoadedValue(uuid.toString(), report);
      }

      List<UserFishStats> statsRows = this.database.loadAllUserFishStats(userId);
      if (statsRows != null) {
         for (UserFishStats stats : statsRows) {
            this.userFishStatsDataManager.cacheLoadedValue(UserFishRarityKey.of(userId, stats.getFishName(), stats.getFishRarity()).toString(), stats);
         }

         this.preloadedUserFishStats.add(userId);
      }
   }

   private void preloadFishStats() {
      this.plugin.debug("Preloading fish stats.");
      List<FishStats> rows = this.database.loadAllFishStats();
      if (rows == null) {
         this.plugin.getLogger().warning("Could not preload fish stats; falling back to on-demand loads.");
      } else {
         for (FishStats stats : rows) {
            this.fishStatsDataManager.cacheLoadedValue(FishRarityKey.of(stats.getFishName(), stats.getFishRarity()).toString(), stats);
         }

         this.fishStatsPreloaded = true;
         this.plugin.getLogger().info("Preloaded %d fish stats entries.".formatted(rows.size()));
      }
   }

   public int resetDatabaseData() {
      if (this.database == null) {
         return 0;
      } else {
         this.clearRuntimeCaches();
         int resetTableCount = this.database.resetPluginData();
         if (resetTableCount >= 0) {
            this.preloadFishStats();
         }

         return resetTableCount;
      }
   }

   private void clearRuntimeCaches() {
      if (this.userManager != null) {
         this.userManager.clearCache();
      }

      if (this.fishLogDataManager != null) {
         this.fishLogDataManager.clear();
      }

      if (this.fishStatsDataManager != null) {
         this.fishStatsDataManager.clear();
      }

      if (this.userFishStatsDataManager != null) {
         this.userFishStatsDataManager.clear();
      }

      if (this.userReportDataManager != null) {
         this.userReportDataManager.clear();
      }

      if (this.competitionDataManager != null) {
         this.competitionDataManager.clear();
      }

      this.preloadedUserFishStats.clear();
      this.fishStatsPreloaded = false;
   }
}
