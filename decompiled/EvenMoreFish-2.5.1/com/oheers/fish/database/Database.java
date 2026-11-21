package com.oheers.fish.database;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.annotations.NeedsTesting;
import com.oheers.fish.api.annotations.TestType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.H2ConnectionFactory;
import com.oheers.fish.database.connection.MigrationManager;
import com.oheers.fish.database.connection.MySqlConnectionFactory;
import com.oheers.fish.database.connection.SqliteConnectionFactory;
import com.oheers.fish.database.mapper.CompetitionReportMapper;
import com.oheers.fish.database.mapper.FishLogMapper;
import com.oheers.fish.database.mapper.FishStatsMapper;
import com.oheers.fish.database.mapper.UserFishStatsMapper;
import com.oheers.fish.database.mapper.UserReportMapper;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.database.sql.DatabaseSqlDialect;
import com.oheers.fish.database.sql.DatabaseSqlDialectFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jspecify.annotations.NonNull;

@NeedsTesting(
   reason = "Requires full testing, unit where possible, integration eventually.",
   testType = {TestType.MANUAL, TestType.UNIT, TestType.INTEGRATION}
)
public class Database implements DatabaseAPI {
   private String version;
   private final ConnectionFactory connectionFactory;
   private final MigrationManager migrationManager;
   private Jdbi jdbi;
   private String competitionsTable;
   private String fishTable;
   private String usersTable;
   private String fishLogTable;
   private String userFishStatsTable;
   private String transactionsTable;
   private String userSalesTable;
   private DatabaseSqlDialect sqlDialect;
   private final boolean legacyDatabase;
   private ThreadLocal<Boolean> lastHandleFailed = ThreadLocal.withInitial(() -> false);

   public Database() {
      this.connectionFactory = this.getConnectionFactory(MainConfig.getInstance().getDatabaseType().toLowerCase());
      this.connectionFactory.init();
      this.migrationManager = new MigrationManager(this.connectionFactory);
      this.legacyDatabase = this.migrationManager.usingV2();
      if (this.legacyDatabase) {
         this.version = "2";
      } else {
         this.version = this.migrationManager.getDatabaseVersion().getVersion();
         this.migrateFromDatabaseVersionToLatest();
         this.initSettings(MainConfig.getInstance().getPrefix(), MainConfig.getInstance().getDatabase());
      }
   }

   public void migrateFromDatabaseVersionToLatest() {
      String var1 = this.version;
      switch (var1) {
         case "5":
            this.migrationManager.migrateFromV5ToLatest();
            break;
         case "6.0":
            this.migrationManager.migrateFromV6ToLatest();
            break;
         default:
            this.migrationManager.migrateFromVersion(this.version, true);
      }

      this.version = this.migrationManager.getDatabaseVersion().getVersion();
   }

   public MigrationManager getMigrationManager() {
      return this.migrationManager;
   }

   public boolean isLegacyDatabase() {
      return this.legacyDatabase;
   }

   @NonNull
   private ConnectionFactory getConnectionFactory(@NonNull final String type) {
      return (ConnectionFactory)(switch (type) {
         case "mysql" -> new MySqlConnectionFactory();
         case "sqlite" -> new SqliteConnectionFactory();
         default -> new H2ConnectionFactory();
      });
   }

   public void initSettings(final String tablePrefix, final String dbName) {
      this.competitionsTable = tablePrefix + "competitions";
      this.fishTable = tablePrefix + "fish";
      this.usersTable = tablePrefix + "users";
      this.fishLogTable = tablePrefix + "fish_log";
      this.userFishStatsTable = tablePrefix + "user_fish_stats";
      this.transactionsTable = tablePrefix + "transactions";
      this.userSalesTable = tablePrefix + "users_sales";
      this.sqlDialect = DatabaseSqlDialectFactory.create(this.connectionFactory.getType());
      this.jdbi = this.createJdbi();
   }

   public Jdbi getJdbi() {
      return this.jdbi;
   }

   private Jdbi createJdbi() {
      Jdbi database = Jdbi.create(this.connectionFactory::getConnection);
      boolean normalizeLegacyText = this.supportsLegacyTimestampNormalization();
      database.installPlugin(new SqlObjectPlugin());
      database.registerRowMapper(UserReport.class, new UserReportMapper());
      database.registerRowMapper(FishLog.class, new FishLogMapper(this.fishLogTable, normalizeLegacyText));
      database.registerRowMapper(FishStats.class, new FishStatsMapper(this.fishTable, normalizeLegacyText));
      database.registerRowMapper(UserFishStats.class, new UserFishStatsMapper(this.userFishStatsTable, normalizeLegacyText));
      database.registerRowMapper(CompetitionReport.class, new CompetitionReportMapper(this.competitionsTable, normalizeLegacyText));
      return database;
   }

   @Override
   public boolean hasUser(@NonNull UUID uuid) {
      return this.withHandle(
         handle -> handle.createQuery("select 1 from " + this.usersTable + " where uuid = :uuid limit 1")
            .bind("uuid", uuid.toString())
            .mapTo(Integer.class)
            .findOne()
            .isPresent(),
         false
      );
   }

   public boolean hasFishLog(int userId) {
      return userId == 0
         ? false
         : this.withHandle(
            handle -> handle.createQuery("select 1 from " + this.fishLogTable + " where user_id = :user_id limit 1")
               .bind("user_id", userId)
               .mapTo(Integer.class)
               .findOne()
               .isPresent(),
            false
         );
   }

   @Override
   public int getUserId(@NonNull UUID uuid) {
      return this.withHandle(
         handle -> handle.createQuery("select id from " + this.usersTable + " where uuid = :uuid order by id asc limit 1")
            .bind("uuid", uuid.toString())
            .mapTo(Integer.class)
            .findOne()
            .orElse(0),
         0
      );
   }

   @Override
   public UserReport getUserReport(@NonNull UUID uuid) {
      UserReport report = this.withHandle(
         handle -> handle.createQuery(
               "select id, uuid, first_fish, last_fish, largest_fish, shortest_fish, largest_length, shortest_length, num_fish_caught, total_fish_length, competitions_won, competitions_joined, fish_sold, money_earned from "
                  + this.usersTable
                  + " where uuid = :uuid order by id asc limit 1"
            )
            .bind("uuid", uuid.toString())
            .mapTo(UserReport.class)
            .findOne()
            .orElse(null),
         null
      );
      if (report == null) {
         DatabaseUtil.writeDbVerbose("User report for (%s) does not exist in the database.".formatted(uuid));
      } else {
         DatabaseUtil.writeDbVerbose("Read user report for user (%s)".formatted(uuid));
      }

      return report;
   }

   @Override
   public boolean hasFishStats(@NonNull IFish fish) {
      return this.withHandle(
         handle -> handle.createQuery("select 1 from " + this.fishTable + " where fish_name = :fish_name and fish_rarity = :fish_rarity limit 1")
            .bind("fish_name", fish.getId())
            .bind("fish_rarity", fish.getRarity().getId())
            .mapTo(Integer.class)
            .findOne()
            .isPresent(),
         false
      );
   }

   @Override
   public void incrementFish(@NonNull IFish fish) {
      this.useHandle(
         handle -> handle.createUpdate(
               "update " + this.fishTable + " set total_caught = total_caught + 1 where fish_rarity = :fish_rarity and fish_name = :fish_name"
            )
            .bind("fish_rarity", fish.getRarity().getId())
            .bind("fish_name", fish.getId())
            .execute()
      );
   }

   @Override
   public void createCompetitionReport(@NonNull Competition competition) {
      Leaderboard leaderboard = competition.getLeaderboard();
      String winnerUuid;
      String winnerFish;
      float winnerScore;
      String contestants;
      if (leaderboard.getSize() > 0) {
         CompetitionEntry topEntry = leaderboard.getTopEntry();
         winnerUuid = topEntry.getPlayer().toString();
         winnerFish = this.prepareRarityFishString(topEntry.getFish());
         winnerScore = topEntry.getValue();
         contestants = this.prepareContestantsString(leaderboard.getEntries());
      } else {
         winnerUuid = "None";
         winnerFish = "None";
         winnerScore = 0.0F;
         contestants = "None";
      }

      Timestamp endTime = Timestamp.valueOf(LocalDateTime.now());
      LocalDateTime start = competition.getStartTime();
      Timestamp startTime = start == null ? endTime : Timestamp.valueOf(start);
      this.useHandle(
         handle -> handle.createUpdate(
               "insert into "
                  + this.competitionsTable
                  + " (competition_name, winner_uuid, winner_fish, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_uuid, :winner_fish, :winner_score, :contestants, :start_time, :end_time)"
            )
            .bind("competition_name", competition.getCompetitionName())
            .bind("winner_uuid", winnerUuid)
            .bind("winner_fish", winnerFish)
            .bind("winner_score", winnerScore)
            .bind("contestants", contestants)
            .bind("start_time", startTime == null ? endTime : startTime)
            .bind("end_time", endTime)
            .execute()
      );
   }

   private String prepareContestantsString(@NonNull List<CompetitionEntry> entries) {
      return entries.isEmpty() ? "None" : entries.stream().map(CompetitionEntry::getPlayer).map(UUID::toString).collect(Collectors.joining(","));
   }

   @NonNull
   private String prepareRarityFishString(@NonNull final IFish fish) {
      return fish.getRarity().getId() + ":" + fish.getId();
   }

   @Override
   public void createSale(
      @NonNull String transactionId, @NonNull String fishName, @NonNull String fishRarity, int fishAmount, double fishLength, double priceSold
   ) {
      this.useHandle(
         handle -> handle.createUpdate(
               "insert into "
                  + this.userSalesTable
                  + " (transaction_id, fish_name, fish_rarity, fish_amount, fish_length, price_sold) values (:transaction_id, :fish_name, :fish_rarity, :fish_amount, :fish_length, :price_sold)"
            )
            .bind("transaction_id", transactionId)
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .bind("fish_amount", fishAmount)
            .bind("fish_length", fishLength)
            .bind("price_sold", priceSold)
            .execute()
      );
   }

   @Override
   public void createTransaction(@NonNull String transactionId, int userId, @NonNull Timestamp timestamp) {
      this.useHandle(
         handle -> handle.createUpdate("insert into " + this.transactionsTable + " (id, user_id, timestamp) values (:id, :user_id, :timestamp)")
            .bind("id", transactionId)
            .bind("user_id", userId)
            .bind("timestamp", timestamp.toLocalDateTime())
            .execute()
      );
   }

   @Override
   public FishLog getFishLog(int userId, String fishName, String fishRarity, LocalDateTime time) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select id, user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id from "
                  + this.fishLogTable
                  + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity and catch_time = :catch_time"
            )
            .bind("user_id", userId)
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .bind("catch_time", time)
            .mapTo(FishLog.class)
            .findOne()
            .orElse(null),
         null
      );
   }

   public void shutdown() {
      try {
         this.connectionFactory.shutdown();
      } catch (Exception var2) {
         EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, var2.getMessage(), (Throwable)var2);
      }
   }

   public String getDatabaseVersion() {
      if (!MainConfig.getInstance().databaseEnabled()) {
         return "Disabled";
      } else {
         return !DatabaseUtil.isDatabaseOnline() ? "Offline" : "V" + this.version;
      }
   }

   public String getType() {
      if (!MainConfig.getInstance().databaseEnabled()) {
         return "Disabled";
      } else {
         return !DatabaseUtil.isDatabaseOnline() ? "Offline" : this.connectionFactory.getType();
      }
   }

   public int resetPluginData() {
      return this.withHandle(handle -> this.resetPluginData(handle.getConnection()), -1);
   }

   @Override
   public UserFishStats getUserFishStats(int userId, String fishName, String fishRarity) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity from "
                  + this.userFishStatsTable
                  + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity"
            )
            .bind("user_id", userId)
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .mapTo(UserFishStats.class)
            .findOne()
            .orElse(null),
         null
      );
   }

   public Database.LoadResult<UserFishStats> loadUserFishStats(int userId, String fishName, String fishRarity) {
      UserFishStats stats = this.withHandle(
         handle -> handle.createQuery(
               "select user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity from "
                  + this.userFishStatsTable
                  + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity"
            )
            .bind("user_id", userId)
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .mapTo(UserFishStats.class)
            .findOne()
            .orElse(null),
         null
      );
      if (stats != null) {
         return Database.LoadResult.found(stats);
      } else {
         return this.didLastHandleFail() ? Database.LoadResult.unreadable() : Database.LoadResult.notFound();
      }
   }

   public void upsertUserFishStats(UserFishStats userFishStats) {
      this.useHandle(handle -> this.bindUserFishStatsUpdate(handle, this.userFishStatsUpsertSql(), userFishStats).execute());
   }

   public List<UserFishStats> loadAllUserFishStats(int userId) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity from "
                  + this.userFishStatsTable
                  + " where user_id = :user_id"
            )
            .bind("user_id", userId)
            .mapTo(UserFishStats.class)
            .list(),
         null
      );
   }

   @Override
   public Set<FishLog> getFishLogEntries(int userId, String fishName, String fishRarity) {
      return this.withHandle(
         handle -> new LinkedHashSet<>(
            handle.createQuery(
                  "select id, user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id from "
                     + this.fishLogTable
                     + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity"
               )
               .bind("user_id", userId)
               .bind("fish_name", fishName)
               .bind("fish_rarity", fishRarity)
               .mapTo(FishLog.class)
               .list()
         ),
         Set.of()
      );
   }

   @Override
   public void setFishLogEntry(FishLog fishLogEntry) {
      this.useHandle(handle -> this.bindFishLogInsert(handle, fishLogEntry).execute());
   }

   @Override
   public FishStats getFishStats(String fishName, String fishRarity) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select fish_name, fish_rarity, first_catch_time, discoverer, shortest_length, shortest_fisher, largest_fish, largest_fisher, total_caught from "
                  + this.fishTable
                  + " where fish_name = :fish_name and fish_rarity = :fish_rarity"
            )
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .mapTo(FishStats.class)
            .findOne()
            .orElse(null),
         null
      );
   }

   public Database.LoadResult<FishStats> loadFishStats(String fishName, String fishRarity) {
      FishStats stats = this.withHandle(
         handle -> handle.createQuery(
               "select fish_name, fish_rarity, first_catch_time, discoverer, shortest_length, shortest_fisher, largest_fish, largest_fisher, total_caught from "
                  + this.fishTable
                  + " where fish_name = :fish_name and fish_rarity = :fish_rarity"
            )
            .bind("fish_name", fishName)
            .bind("fish_rarity", fishRarity)
            .mapTo(FishStats.class)
            .findOne()
            .orElse(null),
         null
      );
      if (stats != null) {
         return Database.LoadResult.found(stats);
      } else {
         return this.didLastHandleFail() ? Database.LoadResult.unreadable() : Database.LoadResult.notFound();
      }
   }

   @Override
   public void upsertFishStats(@NonNull FishStats fishStats) {
      this.useTransaction(handle -> this.bindFishStatsUpdate(handle, fishStats).execute());
   }

   public List<FishStats> loadAllFishStats() {
      return this.withHandle(
         handle -> handle.createQuery(
               "select fish_name, fish_rarity, first_catch_time, discoverer, shortest_length, shortest_fisher, largest_fish, largest_fisher, total_caught from "
                  + this.fishTable
            )
            .mapTo(FishStats.class)
            .list(),
         null
      );
   }

   @Override
   public boolean userHasFish(@NonNull String rarity, @NonNull String fish, int id) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select 1 from " + this.userFishStatsTable + " where user_id = :user_id and fish_rarity = :fish_rarity and fish_name = :fish_name limit 1"
            )
            .bind("user_id", id)
            .bind("fish_rarity", rarity)
            .bind("fish_name", fish)
            .mapTo(Integer.class)
            .findOne()
            .isPresent(),
         false
      );
   }

   @Override
   public boolean userHasRarity(@NonNull String rarity, int id) {
      return this.withHandle(
         handle -> handle.createQuery("select 1 from " + this.userFishStatsTable + " where user_id = :user_id and fish_rarity = :fish_rarity limit 1")
            .bind("user_id", id)
            .bind("fish_rarity", rarity)
            .mapTo(Integer.class)
            .findOne()
            .isPresent(),
         false
      );
   }

   @Override
   public void batchInsertFishLogs(Collection<FishLog> logs) {
      this.useHandle(
         handle -> {
            PreparedBatch batch = handle.prepareBatch(
               "insert into "
                  + this.fishLogTable
                  + " (user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id) values (:user_id, :fish_name, :fish_rarity, :fish_length, :catch_time, :competition_id)"
            );
            int count = 0;

            for (FishLog log : logs) {
               if (log != null) {
                  this.bindFishLogBatch(batch, log);
                  batch.add();
                  count++;
               }
            }

            if (count > 0) {
               batch.execute();
            }
         }
      );
   }

   @Override
   public Integer upsertUserReport(UserReport report) {
      return this.withHandle(
         handle -> {
            String userUuid = report.getUuid().toString();
            Integer id = handle.createQuery("select id from " + this.usersTable + " where uuid = :uuid order by id asc limit 1")
               .bind("uuid", userUuid)
               .mapTo(Integer.class)
               .findOne()
               .orElse(null);
            if (id != null) {
               this.bindUserReportUpdate(handle, report, id).execute();
               return id;
            } else {
               this.bindUserReportInsert(handle, report).execute();
               return handle.createQuery("select id from " + this.usersTable + " where uuid = :uuid order by id asc limit 1")
                  .bind("uuid", userUuid)
                  .mapTo(Integer.class)
                  .findOne()
                  .orElse(0);
            }
         },
         0
      );
   }

   @Override
   public void batchUpdateUserFishStats(Collection<UserFishStats> userFishStats) {
      this.useHandle(handle -> {
         PreparedBatch batch = handle.prepareBatch(this.userFishStatsUpsertSql());
         int count = 0;

         for (UserFishStats stats : userFishStats) {
            if (stats != null) {
               this.bindUserFishStatsBatch(batch, stats);
               batch.add();
               count++;
            }
         }

         if (count > 0) {
            batch.execute();
         }
      });
   }

   @Override
   public void updateCompetition(CompetitionReport competition) {
      this.useHandle(handle -> this.bindCompetitionInsert(handle, competition).execute());
   }

   @Override
   public CompetitionReport getCompetitionReport(int id) {
      return this.withHandle(
         handle -> handle.createQuery(
               "select id, competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time from "
                  + this.competitionsTable
                  + " where id = :id"
            )
            .bind("id", id)
            .mapTo(CompetitionReport.class)
            .findOne()
            .orElse(null),
         null
      );
   }

   @Override
   public void batchUpdateCompetitions(Collection<CompetitionReport> competitions) {
      this.useHandle(
         handle -> {
            PreparedBatch batch = handle.prepareBatch(
               "insert into "
                  + this.competitionsTable
                  + " (competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_fish, :winner_uuid, :winner_score, :contestants, :start_time, :end_time)"
            );
            int count = 0;

            for (CompetitionReport competition : competitions) {
               if (competition != null) {
                  this.bindCompetitionBatch(batch, competition);
                  batch.add();
                  count++;
               }
            }

            if (count > 0) {
               batch.execute();
            }
         }
      );
   }

   private String userFishStatsUpsertSql() {
      return this.sqlDialect.userFishStatsUpsert(this.userFishStatsTable);
   }

   private String fishStatsUpsertSql() {
      return this.sqlDialect.fishStatsUpsert(this.fishTable);
   }

   private int resetPluginData(@NonNull Connection connection) throws SQLException {
      List<String> tables = this.discoverManagedTables(connection);
      if (tables.isEmpty()) {
         return 0;
      } else {
         String type = this.connectionFactory.getType().toUpperCase(Locale.ROOT);
         switch (type) {
            case "SQLITE":
               this.resetSqliteTables(connection, tables);
               break;
            case "MYSQL":
            case "MARIADB":
               this.resetMysqlTables(connection, tables);
               break;
            case "H2":
               this.resetH2Tables(connection, tables);
               break;
            case "POSTGRES":
            case "POSTGRESQL":
               this.resetPostgresTables(connection, tables);
               break;
            default:
               throw new SQLException("Unsupported database type for reset: " + type);
         }

         EvenMoreFish.getInstance().getLogger().info("Reset EMF database tables: " + String.join(", ", tables));
         return tables.size();
      }
   }

   @NonNull
   private List<String> discoverManagedTables(@NonNull Connection connection) throws SQLException {
      String prefix = MainConfig.getInstance().getPrefix();
      String flywayTable = prefix + "flyway_schema_history";
      DatabaseMetaData metaData = connection.getMetaData();
      Map<String, String> tablesByLowerName = new LinkedHashMap<>();
      this.collectManagedTables(metaData, connection.getCatalog(), null, prefix, flywayTable, tablesByLowerName);
      this.collectManagedTables(metaData, null, null, prefix, flywayTable, tablesByLowerName);
      return new ArrayList<>(tablesByLowerName.values());
   }

   private void collectManagedTables(
      @NonNull DatabaseMetaData metaData,
      String catalog,
      String schema,
      @NonNull String prefix,
      @NonNull String flywayTable,
      @NonNull Map<String, String> tablesByLowerName
   ) throws SQLException {
      try (ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
         while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            if (tableName != null) {
               String normalizedName = tableName.toLowerCase(Locale.ROOT);
               if (normalizedName.startsWith(prefix.toLowerCase(Locale.ROOT)) && !normalizedName.equals(flywayTable.toLowerCase(Locale.ROOT))) {
                  tablesByLowerName.putIfAbsent(normalizedName, tableName);
               }
            }
         }
      }
   }

   private void resetSqliteTables(@NonNull Connection connection, @NonNull List<String> tables) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.execute("PRAGMA foreign_keys = OFF");

         try {
            for (String table : tables) {
               statement.executeUpdate("DELETE FROM " + this.quotedIdentifier(connection, table));
            }

            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name IN (" + this.quotedStringList(tables) + ")");
         } finally {
            statement.execute("PRAGMA foreign_keys = ON");
         }
      }
   }

   private void resetMysqlTables(@NonNull Connection connection, @NonNull List<String> tables) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.execute("SET FOREIGN_KEY_CHECKS = 0");

         try {
            for (String table : tables) {
               statement.executeUpdate("TRUNCATE TABLE " + this.quotedIdentifier(connection, table));
            }
         } finally {
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
         }
      }
   }

   private void resetH2Tables(@NonNull Connection connection, @NonNull List<String> tables) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.execute("SET REFERENTIAL_INTEGRITY FALSE");

         try {
            for (String table : tables) {
               statement.executeUpdate("TRUNCATE TABLE " + this.quotedIdentifier(connection, table) + " RESTART IDENTITY");
            }
         } finally {
            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
         }
      }
   }

   private void resetPostgresTables(@NonNull Connection connection, @NonNull List<String> tables) throws SQLException {
      List<String> quotedTables = new ArrayList<>(tables.size());

      for (String table : tables) {
         quotedTables.add(this.quotedIdentifier(connection, table));
      }

      try (Statement statement = connection.createStatement()) {
         statement.executeUpdate("TRUNCATE TABLE " + quotedTables.stream().collect(Collectors.joining(", ")) + " RESTART IDENTITY");
      }
   }

   @NonNull
   private String quotedIdentifier(@NonNull Connection connection, @NonNull String identifier) throws SQLException {
      String quote = connection.getMetaData().getIdentifierQuoteString();
      return quote != null && !quote.isBlank() ? quote + identifier.replace(quote, quote + quote) + quote : identifier;
   }

   @NonNull
   private String quotedStringList(@NonNull List<String> values) {
      return values.stream().map(value -> "'" + value.replace("'", "''") + "'").collect(Collectors.joining(", "));
   }

   private <T> T withHandle(Database.HandleCallback<T> callback, T fallback) {
      try {
         this.handleFailureState().set(false);
         return this.jdbi == null ? fallback : this.jdbi.withHandle(callback::apply);
      } catch (Exception var4) {
         this.handleFailureState().set(true);
         this.logQueryFailure(var4);
         return fallback;
      }
   }

   private void useHandle(Database.HandleConsumer consumer) {
      try {
         if (this.jdbi == null) {
            return;
         }

         this.jdbi.useHandle(consumer::accept);
      } catch (Exception var3) {
         EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Update execution failed", (Throwable)var3);
      }
   }

   private void useTransaction(Database.HandleConsumer consumer) {
      try {
         if (this.jdbi == null) {
            return;
         }

         this.jdbi.useTransaction(consumer::accept);
      } catch (Exception var3) {
         EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Transactional update execution failed", (Throwable)var3);
      }
   }

   private Update bindUserFishStatsUpdate(Handle handle, String sql, UserFishStats stats) {
      return handle.createUpdate(sql)
         .bind("user_id", stats.getUserId())
         .bind("fish_name", stats.getFishName())
         .bind("fish_rarity", stats.getFishRarity())
         .bind("first_catch_time", stats.getFirstCatchTimestamp())
         .bind("shortest_length", stats.getShortestLength())
         .bind("longest_length", stats.getLongestLength())
         .bind("quantity", stats.getQuantity());
   }

   private void bindUserFishStatsBatch(PreparedBatch batch, UserFishStats stats) {
      batch.bind("user_id", stats.getUserId())
         .bind("fish_name", stats.getFishName())
         .bind("fish_rarity", stats.getFishRarity())
         .bind("first_catch_time", stats.getFirstCatchTimestamp())
         .bind("shortest_length", stats.getShortestLength())
         .bind("longest_length", stats.getLongestLength())
         .bind("quantity", stats.getQuantity());
   }

   private Update bindFishLogInsert(Handle handle, FishLog fishLogEntry) {
      Update update = handle.createUpdate(
            "insert into "
               + this.fishLogTable
               + " (user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id) values (:user_id, :fish_name, :fish_rarity, :fish_length, :catch_time, :competition_id)"
         )
         .bind("user_id", fishLogEntry.getUserId())
         .bind("fish_name", fishLogEntry.getFishName())
         .bind("fish_rarity", fishLogEntry.getFishRarity())
         .bind("fish_length", fishLogEntry.getLength())
         .bind("catch_time", fishLogEntry.getCatchTimestamp());
      if (fishLogEntry.getCompetitionId() == null) {
         update.bindNull("competition_id", 12);
      } else {
         update.bind("competition_id", fishLogEntry.getCompetitionId());
      }

      return update;
   }

   private void bindFishLogBatch(PreparedBatch batch, FishLog fishLogEntry) {
      batch.bind("user_id", fishLogEntry.getUserId())
         .bind("fish_name", fishLogEntry.getFishName())
         .bind("fish_rarity", fishLogEntry.getFishRarity())
         .bind("fish_length", fishLogEntry.getLength())
         .bind("catch_time", fishLogEntry.getCatchTimestamp());
      if (fishLogEntry.getCompetitionId() == null) {
         batch.bindNull("competition_id", 12);
      } else {
         batch.bind("competition_id", fishLogEntry.getCompetitionId());
      }
   }

   private Update bindFishStatsUpdate(Handle handle, FishStats fishStats) {
      return handle.createUpdate(this.fishStatsUpsertSql())
         .bind("fish_name", fishStats.getFishName())
         .bind("fish_rarity", fishStats.getFishRarity())
         .bind("first_fisher", fishStats.getDiscoverer().toString())
         .bind("discoverer", fishStats.getDiscoverer().toString())
         .bind("total_caught", fishStats.getQuantity())
         .bind("largest_fish", fishStats.getLongestLength())
         .bind("largest_fisher", fishStats.getLongestFisher().toString())
         .bind("shortest_length", fishStats.getShortestLength())
         .bind("shortest_fisher", fishStats.getShortestFisher().toString())
         .bind("first_catch_time", fishStats.getFirstCatchTimestamp());
   }

   private Update bindUserReportInsert(Handle handle, UserReport report) {
      return handle.createUpdate(
            "insert into "
               + this.usersTable
               + " (uuid, competitions_joined, competitions_won, total_fish_length, first_fish, money_earned, fish_sold, num_fish_caught, largest_fish, largest_length, last_fish, shortest_fish, shortest_length) values (:uuid, :competitions_joined, :competitions_won, :total_fish_length, :first_fish, :money_earned, :fish_sold, :num_fish_caught, :largest_fish, :largest_length, :last_fish, :shortest_fish, :shortest_length)"
         )
         .bind("uuid", report.getUuid().toString())
         .bind("competitions_joined", report.getCompetitionsJoined())
         .bind("competitions_won", report.getCompetitionsWon())
         .bind("total_fish_length", report.getTotalFishLength())
         .bind("first_fish", report.getFirstFish().toString())
         .bind("money_earned", report.getMoneyEarned())
         .bind("fish_sold", report.getFishSold())
         .bind("num_fish_caught", report.getNumFishCaught())
         .bind("largest_fish", report.getLargestFish().toString())
         .bind("largest_length", report.getLargestLength())
         .bind("last_fish", report.getRecentFish().toString())
         .bind("shortest_fish", report.getShortestFish().toString())
         .bind("shortest_length", report.getShortestLength());
   }

   private Update bindUserReportUpdate(Handle handle, UserReport report, int id) {
      return handle.createUpdate(
            "update "
               + this.usersTable
               + " set competitions_joined = :competitions_joined, competitions_won = :competitions_won, total_fish_length = :total_fish_length, first_fish = :first_fish, money_earned = :money_earned, fish_sold = :fish_sold, num_fish_caught = :num_fish_caught, largest_fish = :largest_fish, largest_length = :largest_length, last_fish = :last_fish, shortest_fish = :shortest_fish, shortest_length = :shortest_length where id = :id"
         )
         .bind("id", id)
         .bind("competitions_joined", report.getCompetitionsJoined())
         .bind("competitions_won", report.getCompetitionsWon())
         .bind("total_fish_length", report.getTotalFishLength())
         .bind("first_fish", report.getFirstFish().toString())
         .bind("money_earned", report.getMoneyEarned())
         .bind("fish_sold", report.getFishSold())
         .bind("num_fish_caught", report.getNumFishCaught())
         .bind("largest_fish", report.getLargestFish().toString())
         .bind("largest_length", report.getLargestLength())
         .bind("last_fish", report.getRecentFish().toString())
         .bind("shortest_fish", report.getShortestFish().toString())
         .bind("shortest_length", report.getShortestLength());
   }

   private Update bindCompetitionInsert(Handle handle, CompetitionReport competition) {
      return handle.createUpdate(
            "insert into "
               + this.competitionsTable
               + " (competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_fish, :winner_uuid, :winner_score, :contestants, :start_time, :end_time)"
         )
         .bind("competition_name", competition.getCompetitionConfigId())
         .bind("winner_fish", competition.getWinnerFish())
         .bind("winner_uuid", competition.getWinnerUuid() == null ? "None" : competition.getWinnerUuid().toString())
         .bind("winner_score", competition.getWinnerScore())
         .bind(
            "contestants",
            competition.getContestants().isEmpty() ? "None" : competition.getContestants().stream().map(UUID::toString).collect(Collectors.joining(","))
         )
         .bind("start_time", competition.getStartTimestamp())
         .bind("end_time", competition.getEndTimestamp());
   }

   private void bindCompetitionBatch(PreparedBatch batch, CompetitionReport competition) {
      batch.bind("competition_name", competition.getCompetitionConfigId())
         .bind("winner_fish", competition.getWinnerFish())
         .bind("winner_uuid", competition.getWinnerUuid() == null ? "None" : competition.getWinnerUuid().toString())
         .bind("winner_score", competition.getWinnerScore())
         .bind(
            "contestants",
            competition.getContestants().isEmpty() ? "None" : competition.getContestants().stream().map(UUID::toString).collect(Collectors.joining(","))
         )
         .bind("start_time", competition.getStartTimestamp())
         .bind("end_time", competition.getEndTimestamp());
   }

   private void logQueryFailure(Exception exception) {
      UnreadableTimestampException unreadableTimestampException = this.findCause(exception, UnreadableTimestampException.class);
      if (unreadableTimestampException != null) {
         EvenMoreFish.getInstance().getLogger().warning("Query execution failed: " + unreadableTimestampException.getMessage());
         EvenMoreFish.getInstance().debug("Query execution failed due to unreadable timestamp.", exception);
      } else {
         EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Query execution failed", (Throwable)exception);
      }
   }

   private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
      for (Throwable current = throwable; current != null; current = current.getCause()) {
         if (type.isInstance(current)) {
            return type.cast(current);
         }
      }

      return null;
   }

   private boolean supportsLegacyTimestampNormalization() {
      String type = this.connectionFactory.getType();
      return "SQLITE".equalsIgnoreCase(type) || "H2".equalsIgnoreCase(type);
   }

   private boolean didLastHandleFail() {
      return Boolean.TRUE.equals(this.handleFailureState().get());
   }

   private ThreadLocal<Boolean> handleFailureState() {
      if (this.lastHandleFailed == null) {
         this.lastHandleFailed = ThreadLocal.withInitial(() -> false);
      }

      return this.lastHandleFailed;
   }

   @FunctionalInterface
   private interface HandleCallback<T> {
      T apply(Handle handle) throws Exception;
   }

   @FunctionalInterface
   private interface HandleConsumer {
      void accept(Handle handle) throws Exception;
   }

   public static final class LoadResult<T> {
      private final T value;
      private final Database.LoadResult.State state;

      private LoadResult(T value, Database.LoadResult.State state) {
         this.value = value;
         this.state = state;
      }

      public static <T> Database.LoadResult<T> found(T value) {
         return new Database.LoadResult<>(value, Database.LoadResult.State.FOUND);
      }

      public static <T> Database.LoadResult<T> notFound() {
         return new Database.LoadResult<>(null, Database.LoadResult.State.NOT_FOUND);
      }

      public static <T> Database.LoadResult<T> unreadable() {
         return new Database.LoadResult<>(null, Database.LoadResult.State.UNREADABLE);
      }

      public T getValue() {
         return this.value;
      }

      public boolean isFound() {
         return this.state == Database.LoadResult.State.FOUND;
      }

      public boolean isUnreadable() {
         return this.state == Database.LoadResult.State.UNREADABLE;
      }

      private static enum State {
         FOUND,
         NOT_FOUND,
         UNREADABLE;
      }
   }
}
