package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DatabaseConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

final class PlayerDataRepository {
   private static final Object JDBC_MONITOR = new Object();
   private static final Object PLAYER_STATE_MONITOR = new Object();
   private static final Object QUEUE_MONITOR = new Object();
   private static final Object SCORE_RANKING_MONITOR = new Object();
   private static final Deque<PlayerDataRepository.DatabaseUpdate> pendingUpdates = new ArrayDeque<>();
   private static final Map<UUID, Integer> cachedScores = new ConcurrentHashMap<>();
   private static final Map<UUID, Integer> scoreUpdatesBeforeCacheLoad = new HashMap<>();
   private static Connection connection;
   private static boolean drainScheduled;
   private static long nextDrainAttemptNanos;
   private static volatile long lifecycleGeneration;
   private static volatile boolean closed;
   private static final Map<UUID, Deque<PlayerDataRepository.DatabaseUpdate>> missingPlayerUpdates = new HashMap<>();
   private static volatile boolean scoreRankingCacheLoaded;
   private static long scoreRankingLoadGeneration;

   private PlayerDataRepository() {
   }

   static Object stateMonitor() {
      return PLAYER_STATE_MONITOR;
   }

   static Object jdbcMonitor() {
      return JDBC_MONITOR;
   }

   static void beginInitialization() {
      synchronized (JDBC_MONITOR) {
         closed = false;
         lifecycleGeneration++;
         synchronized (QUEUE_MONITOR) {
            drainScheduled = false;
            nextDrainAttemptNanos = 0L;
         }
      }
   }

   static Connection connection() throws Exception {
      synchronized (JDBC_MONITOR) {
         if (closed) {
            throw new IllegalStateException("Player database is closed");
         } else {
            File databaseFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + PlayerData.getDATABASE_NAME());
            if (connection == null || connection.isClosed()) {
               if (!DatabaseConfig.isUseMySQL()) {
                  Class.forName("org.sqlite.JDBC");
                  connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
               } else {
                  Class.forName("com.mysql.jdbc.Driver");
                  String url = "jdbc:mysql://"
                     + DatabaseConfig.getMysqlHost()
                     + ":"
                     + DatabaseConfig.getMysqlPort()
                     + "/"
                     + DatabaseConfig.mysqlDatabaseName
                     + "?useSSL="
                     + DatabaseConfig.useSSL
                     + "&createDatabaseIfNotExist=true";
                  connection = DriverManager.getConnection(url, DatabaseConfig.getMysqlUsername(), DatabaseConfig.getMysqlPassword());
               }

               connection.setAutoCommit(true);
            }

            return connection;
         }
      }
   }

   static boolean readPlayer(UUID playerId, PlayerDataRepository.ResultSetReader reader) throws Exception {
      synchronized (JDBC_MONITOR) {
         releaseMissingPlayerUpdates(playerId);
         drainUpdatesLocked();
         String sql = "SELECT * FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";

         boolean var14;
         try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, playerId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
               if (!resultSet.next()) {
                  return false;
               }

               reader.read(resultSet);
               var14 = true;
            }
         }

         return var14;
      }
   }

   static void insertNewPlayer(UUID playerId, String playerName) throws Exception {
      synchronized (JDBC_MONITOR) {
         String sql = "INSERT INTO "
            + PlayerData.getPLAYER_DATA_TABLE_NAME()
            + " (PlayerUUID, DisplayName, CurrencyV2, CurrencyCents, Score, Kills, HighestLevelKilled, Deaths, QuestsCompleted, DungeonsCompleted, SkillXP_ARMOR, SkillXP_SWORDS, SkillXP_AXES, SkillXP_BOWS, SkillXP_CROSSBOWS, SkillXP_TRIDENTS, SkillXP_HOES, SkillXP_MACES, SkillXP_SPEARS, SkillXP_STAVES, SkillXP_WANDS, SkillBonusSelections, GamblingDebt, GamblingDebtCents, UseBookMenus, DismissEMStatusScreenMessage) VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '{}', 0, 0, 1, 0)";

         try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
         }

         releaseMissingPlayerUpdates(playerId);
         drainUpdatesLocked();
         updateCachedScore(playerId, 0);
      }
   }

   private static void releaseMissingPlayerUpdates(UUID playerId) {
      synchronized (QUEUE_MONITOR) {
         Deque<PlayerDataRepository.DatabaseUpdate> held = missingPlayerUpdates.remove(playerId);
         if (held != null) {
            while (!held.isEmpty()) {
               pendingUpdates.addFirst(held.removeLast());
            }
         }
      }
   }

   static void enqueueUpdate(UUID playerId, String column, Object value) {
      enqueueUpdate(playerId, Collections.singletonMap(column, value));
   }

   static void enqueueUpdate(UUID playerId, Map<String, Object> values) {
      PlayerDataRepository.DatabaseUpdate update = snapshot(playerId, values);
      long generation;
      synchronized (QUEUE_MONITOR) {
         pendingUpdates.addLast(update);
         if (closed || drainScheduled || System.nanoTime() < nextDrainAttemptNanos) {
            return;
         }

         drainScheduled = true;
         generation = lifecycleGeneration;
      }

      try {
         if (!MetadataHandler.PLUGIN.isEnabled()) {
            drainUpdates(generation);
         } else {
            Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> drainUpdates(generation));
         }
      } catch (RuntimeException var9) {
         synchronized (QUEUE_MONITOR) {
            drainScheduled = false;
         }

         Logger.warn("Player database write submission failed; pending updates were retained: " + var9.getMessage());
      }
   }

   static void updateNow(UUID playerId, String column, Object value) {
      updateNow(playerId, Collections.singletonMap(column, value));
   }

   static void updateNow(UUID playerId, Map<String, Object> values) {
      PlayerDataRepository.DatabaseUpdate update = snapshot(playerId, values);
      synchronized (JDBC_MONITOR) {
         drainUpdatesLocked();
         executeUpdate(update);
      }
   }

   private static PlayerDataRepository.DatabaseUpdate snapshot(UUID playerId, Map<String, Object> values) {
      if (values.isEmpty()) {
         throw new IllegalArgumentException("Empty player update");
      } else {
         values.keySet().forEach(PlayerDataRepository::validateColumn);
         return new PlayerDataRepository.DatabaseUpdate(playerId, Collections.unmodifiableMap(new LinkedHashMap<>(values)));
      }
   }

   static Object getBlob(UUID playerId, String column) {
      return query(playerId, column, ResultSet::getBytes, null, "blob");
   }

   static Boolean getBoolean(UUID playerId, String column) {
      return query(playerId, column, ResultSet::getBoolean, null, "boolean");
   }

   static String getString(UUID playerId, String column) {
      return query(playerId, column, ResultSet::getString, null, "string");
   }

   static Integer getInteger(UUID playerId, String column) {
      return query(playerId, column, ResultSet::getInt, null, "integer");
   }

   static Long getLong(UUID playerId, String column) {
      return query(playerId, column, ResultSet::getLong, 0L, "long");
   }

   static PlayerData.ScoreRank getScoreRank(UUID playerId) {
      if (!scoreRankingCacheLoaded) {
         return PlayerData.ScoreRank.unavailable();
      } else {
         Integer playerScore = cachedScores.get(playerId);
         if (playerScore == null) {
            return PlayerData.ScoreRank.unavailable();
         } else {
            int position = 1;

            for (int score : cachedScores.values()) {
               if (score > playerScore) {
                  position++;
               }
            }

            return new PlayerData.ScoreRank(position, cachedScores.size());
         }
      }
   }

   static void updateCachedScore(UUID playerId, int score) {
      synchronized (SCORE_RANKING_MONITOR) {
         if (scoreRankingCacheLoaded) {
            cachedScores.put(playerId, score);
         } else {
            scoreUpdatesBeforeCacheLoad.put(playerId, score);
         }
      }
   }

   static void loadScoreRankingCacheAsync() {
      long loadGeneration;
      synchronized (SCORE_RANKING_MONITOR) {
         loadGeneration = ++scoreRankingLoadGeneration;
         scoreRankingCacheLoaded = false;
         cachedScores.clear();
      }

      Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> loadScoreRankingCache(loadGeneration));
   }

   private static void loadScoreRankingCache(long loadGeneration) {
      Map<UUID, Integer> loadedScores = new HashMap<>();
      synchronized (JDBC_MONITOR) {
         String sql = "SELECT PlayerUUID, COALESCE(Score, 0) AS ScoreValue FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME();

         try (
            PreparedStatement statement = connection().prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
         ) {
            while (resultSet.next()) {
               try {
                  loadedScores.put(UUID.fromString(resultSet.getString("PlayerUUID")), resultSet.getInt("ScoreValue"));
               } catch (IllegalArgumentException var14) {
                  Logger.warn("Ignored invalid player UUID while loading the score ranking cache.");
               }
            }
         } catch (Exception var17) {
            Logger.warn("Failed to load the player score ranking cache.");
            var17.printStackTrace();
            return;
         }
      }

      synchronized (SCORE_RANKING_MONITOR) {
         if (loadGeneration == scoreRankingLoadGeneration) {
            loadedScores.putAll(scoreUpdatesBeforeCacheLoad);
            scoreUpdatesBeforeCacheLoad.clear();
            cachedScores.putAll(loadedScores);
            scoreRankingCacheLoaded = true;
         }
      }
   }

   static void migrateCurrencyToCents() throws Exception {
      synchronized (JDBC_MONITOR) {
         try (Statement statement = connection().createStatement()) {
            String integralType = DatabaseConfig.isUseMySQL() ? "SIGNED" : "INTEGER";
            int currencyRows = statement.executeUpdate(
               "UPDATE "
                  + PlayerData.getPLAYER_DATA_TABLE_NAME()
                  + " SET CurrencyCents = CAST(ROUND(CurrencyV2 * 100) AS "
                  + integralType
                  + ") WHERE CurrencyCents IS NULL AND CurrencyV2 IS NOT NULL"
            );
            int debtRows = statement.executeUpdate(
               "UPDATE "
                  + PlayerData.getPLAYER_DATA_TABLE_NAME()
                  + " SET GamblingDebtCents = CAST(ROUND(GamblingDebt * 100) AS "
                  + integralType
                  + ") WHERE GamblingDebtCents IS NULL AND GamblingDebt IS NOT NULL"
            );
            if (currencyRows > 0) {
               Logger.info("Migrated " + currencyRows + " player currency rows to cent precision");
            }

            if (debtRows > 0) {
               Logger.info("Migrated " + debtRows + " player gambling debt rows to cent precision");
            }
         }
      }
   }

   static void importLegacy(Collection<PlayerDataRepository.LegacyPlayerData> legacyPlayers) throws Exception {
      synchronized (JDBC_MONITOR) {
         Connection database = connection();
         boolean oldAutoCommit = database.getAutoCommit();
         database.setAutoCommit(false);

         try {
            String existsSql = "SELECT 1 FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";
            String insertSql = "INSERT INTO "
               + PlayerData.getPLAYER_DATA_TABLE_NAME()
               + " (PlayerUUID, DisplayName, CurrencyV2, CurrencyCents) VALUES (?, ?, ?, ?)";

            try (
               PreparedStatement exists = database.prepareStatement(existsSql);
               PreparedStatement insert = database.prepareStatement(insertSql);
            ) {
               for (PlayerDataRepository.LegacyPlayerData legacy : legacyPlayers) {
                  exists.setString(1, legacy.playerId().toString());

                  label135:
                  try (ResultSet resultSet = exists.executeQuery()) {
                     if (!resultSet.next()) {
                        break label135;
                     }
                     continue;
                  }

                  insert.setString(1, legacy.playerId().toString());
                  insert.setString(2, legacy.displayName());
                  insert.setDouble(3, legacy.currency());
                  insert.setLong(4, Math.round(legacy.currency() * 100.0));
                  insert.executeUpdate();
               }
            }

            database.commit();
         } catch (Exception var30) {
            database.rollback();
            throw var30;
         } finally {
            database.setAutoCommit(oldAutoCommit);
         }
      }
   }

   static void close() {
      synchronized (JDBC_MONITOR) {
         try {
            drainUpdatesLocked();
         } catch (RuntimeException var20) {
            Logger.warn("Player database shutdown flush failed; queued intent remains available in this process: " + var20.getMessage());
         }

         closed = true;
         lifecycleGeneration++;
         synchronized (QUEUE_MONITOR) {
            drainScheduled = false;
            if (!pendingUpdates.isEmpty() || !missingPlayerUpdates.isEmpty()) {
               Logger.warn("Unpersisted player updates remain: " + pendingUpdates.size() + " queued; " + missingPlayerUpdates.size() + " missing player rows.");
            }
         }

         try {
            if (connection != null) {
               connection.close();
            }
         } catch (Exception var18) {
            Logger.warn("Could not correctly close database connection.");
         } finally {
            connection = null;
            synchronized (SCORE_RANKING_MONITOR) {
               scoreRankingLoadGeneration++;
               cachedScores.clear();
               scoreUpdatesBeforeCacheLoad.clear();
               scoreRankingCacheLoaded = false;
            }
         }
      }
   }

   private static void drainUpdates(long generation) {
      synchronized (JDBC_MONITOR) {
         if (!closed && generation == lifecycleGeneration) {
            try {
               drainUpdatesLocked();
            } catch (RuntimeException var8) {
               synchronized (QUEUE_MONITOR) {
                  drainScheduled = false;
                  nextDrainAttemptNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
               }

               Logger.warn("Player database update failed; ordered pending writes retained: " + var8.getMessage());
            }
         }
      }
   }

   private static void drainUpdatesLocked() {
      while (true) {
         PlayerDataRepository.DatabaseUpdate update;
         synchronized (QUEUE_MONITOR) {
            update = pendingUpdates.peekFirst();
            if (update == null) {
               drainScheduled = false;
               nextDrainAttemptNanos = 0L;
               return;
            }

            Deque<PlayerDataRepository.DatabaseUpdate> held = missingPlayerUpdates.get(update.playerId());
            if (held != null) {
               held.addLast(pendingUpdates.removeFirst());
               continue;
            }
         }

         try {
            executeUpdate(update);
         } catch (PlayerDataRepository.MissingPlayerRow var8) {
            synchronized (QUEUE_MONITOR) {
               missingPlayerUpdates.computeIfAbsent(update.playerId(), ignored -> new ArrayDeque<>()).addLast(update);
            }

            Logger.warn(var8.getMessage() + "; retained until this player's row is inserted.");
         }

         synchronized (QUEUE_MONITOR) {
            pendingUpdates.removeFirst();
         }
      }
   }

   private static void executeUpdate(PlayerDataRepository.DatabaseUpdate update) {
      String assignments = update.values().keySet().stream().map(column -> column + " = ?").collect(Collectors.joining(", "));
      String sql = "UPDATE " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " SET " + assignments + " WHERE PlayerUUID = ?";

      try {
         try (PreparedStatement statement = connection().prepareStatement(sql)) {
            int index = 1;

            for (Object value : update.values().values()) {
               statement.setObject(index++, value);
            }

            statement.setString(index, update.playerId().toString());
            if (statement.executeUpdate() != 1) {
               throw new PlayerDataRepository.MissingPlayerRow(update.playerId());
            }
         }
      } catch (PlayerDataRepository.MissingPlayerRow var9) {
         throw var9;
      } catch (Exception var10) {
         throw new IllegalStateException("Could not persist " + update.values().keySet() + " for " + update.playerId(), var10);
      }
   }

   private static <T> T query(UUID playerId, String column, PlayerDataRepository.ColumnReader<T> reader, T fallback, String type) {
      validateColumn(column);
      synchronized (JDBC_MONITOR) {
         String sql = "SELECT " + column + " FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";

         Object var9;
         try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, playerId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
               if (!resultSet.next()) {
                  return fallback;
               }

               var9 = reader.read(resultSet, column);
            }
         } catch (Exception var16) {
            Logger.warn("Failed to get " + type + " value from player database: " + column);
            var16.printStackTrace();
            return fallback;
         }

         return (T)var9;
      }
   }

   private static void validateColumn(String column) {
      if (column == null || !column.matches("[A-Za-z0-9_]+")) {
         throw new IllegalArgumentException("Invalid player database column: " + column);
      }
   }

   @FunctionalInterface
   private interface ColumnReader<T> {
      T read(ResultSet var1, String var2) throws Exception;
   }

   private record DatabaseUpdate(UUID playerId, Map<String, Object> values) {
   }

   record LegacyPlayerData(UUID playerId, String displayName, double currency) {
   }

   private static final class MissingPlayerRow extends IllegalStateException {
      private MissingPlayerRow(UUID playerId) {
         super("No player row accepted the update for " + playerId);
      }
   }

   @FunctionalInterface
   interface ResultSetReader {
      void read(ResultSet var1) throws Exception;
   }
}
