package com.oheers.fish.database.connection;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.utils.ManifestUtil;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.migrate.LegacyToV3DatabaseMigration;
import com.oheers.fish.database.strategies.DatabaseStrategyFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
   private final String latestBaselineVersion;
   private final Logger logger = LoggerFactory.getLogger(MigrationManager.class.getName());
   private final FluentConfiguration baseFlywayConfiguration;
   private final Flyway defaultFlyway;
   private final ConnectionFactory connectionFactory;

   public MigrationManager(ConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      this.baseFlywayConfiguration = this.getBaseFlywayConfiguration(connectionFactory);
      this.defaultFlyway = this.baseFlywayConfiguration.load();
      this.latestBaselineVersion = ManifestUtil.getAttributeFromManifest("Database-Baseline-Version", "8.1");
      DatabaseUtil.debugDatabaseTypeVersion(this.connectionFactory);
   }

   public void migrateFromVersion(String currentDbVersion, boolean baseline) {
      Flyway migrate = this.baseFlywayConfiguration.baselineVersion(this.latestBaselineVersion).load();

      try {
         if (this.shouldBaseline(baseline, this.latestBaselineVersion, currentDbVersion)) {
            migrate.baseline();
         }

         migrate.migrate();
      } catch (FlywayException var5) {
         this.logger.error("Database migration failed for version: {}", currentDbVersion, var5);
      }
   }

   private boolean shouldBaseline(final boolean baseline, final String baselineVersion, final String currentDbVersion) {
      if (!baseline) {
         return false;
      } else {
         ComparableVersion version1 = new ComparableVersion(baselineVersion);
         ComparableVersion version2 = new ComparableVersion(currentDbVersion);
         int compare = version1.compareTo(version2);
         return compare == 0;
      }
   }

   public boolean usingV2() {
      boolean dataFolder = Files.isDirectory(Paths.get(EvenMoreFish.getInstance().getDataFolder() + "/data/"));
      return dataFolder || this.queryTableExistence("Fish2");
   }

   public boolean queryTableExistence(final String tableName) {
      try {
         boolean var3;
         try (Connection connection = this.connectionFactory.getConnection()) {
            var3 = this.tableExists(connection, this.resolveLegacyTableName(tableName));
         }

         return var3;
      } catch (SQLException var7) {
         return false;
      }
   }

   private boolean tableExists(@NonNull Connection connection, @NonNull String tableName) throws SQLException {
      DatabaseMetaData metaData = connection.getMetaData();
      String[] tableTypes = new String[]{"TABLE"};

      for (String candidate : new String[]{tableName, tableName.toLowerCase(), tableName.toUpperCase()}) {
         try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, candidate, tableTypes)) {
            if (tables.next()) {
               return true;
            }
         }

         try (ResultSet tablesx = metaData.getTables(null, null, candidate, tableTypes)) {
            if (tablesx.next()) {
               return true;
            }
         }
      }

      return false;
   }

   @NonNull
   private String resolveLegacyTableName(@NonNull String tableName) {
      return tableName.contains("${table.prefix}") ? tableName.replace("${table.prefix}", MainConfig.getInstance().getPrefix()) : tableName;
   }

   public void dropFlywaySchemaHistory() {
      try (
         Connection connection = this.defaultFlyway.getConfiguration().getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS flyway_schema_history");
      ) {
         statement.execute();
         this.logger.info("Dropped flyway schema history table.");
      } catch (SQLException var9) {
         this.logger.error("Failed to drop Flyway schema history table", (Throwable)var9);
      }
   }

   public void repairFlyway() {
      this.baseFlywayConfiguration.load().repair();
   }

   public void cleanFlyway() {
      this.baseFlywayConfiguration.load().clean();
   }

   public MigrationVersion getDatabaseVersion() {
      MigrationVersion latestBaseVersion = MigrationVersion.fromVersion(this.latestBaselineVersion);

      try (Connection ignored = this.connectionFactory.getConnection()) {
         EvenMoreFish.getInstance().debug("Attempting first connection to database...");
      } catch (SQLException var7) {
         return latestBaseVersion;
      }

      MigrationInfoService infoService = this.baseFlywayConfiguration.load().info();
      return infoService.current() == null ? latestBaseVersion : this.baseFlywayConfiguration.load().info().current().getVersion();
   }

   public void migrateFromV6ToLatest() {
      Flyway flyway = this.baseFlywayConfiguration.baselineVersion("6").load();

      try {
         flyway.migrate();
      } catch (FlywayException var3) {
         this.logger.error("There was a problem migrating to the latest database version. You may experience issues.", (Throwable)var3);
      }
   }

   public void legacyFlywayBaseline() {
      Flyway flyway = this.baseFlywayConfiguration.target("3.1").load();
      flyway.migrate();
   }

   public void legacyInitVersion() {
      Flyway flyway = this.baseFlywayConfiguration.target("3.0").load();
      flyway.migrate();
   }

   public void migrateFromV5ToLatest() {
      Flyway flyway = this.baseFlywayConfiguration.baselineVersion("5").load();

      try {
         this.dropFlywaySchemaHistory();
         flyway.migrate();
      } catch (FlywayException var3) {
         this.logger.error("There was a problem migrating to the latest database version. You may experience issues.", (Throwable)var3);
      }
   }

   @Contract(
      pure = true
   )
   @NonNull
   private String getMigrationLocation(final String dialect) {
      return "db/migrations/" + dialect;
   }

   public void migrateLegacy(CommandSender initiator) {
      LegacyToV3DatabaseMigration legacy = new LegacyToV3DatabaseMigration(
         EvenMoreFish.getInstance().getPluginDataManager().getDatabase(), this, this.connectionFactory
      );
      legacy.migrate(initiator);
   }

   private FluentConfiguration getBaseFlywayConfiguration(@NonNull ConnectionFactory connectionFactory) {
      FluentConfiguration baseConfig = Flyway.configure(this.getClass().getClassLoader())
         .dataSource(connectionFactory.dataSource)
         .placeholders(Map.of("table.prefix", MainConfig.getInstance().getPrefix(), "db.name", MainConfig.getInstance().getDatabase()))
         .createSchemas(true)
         .locations(new String[]{this.getMigrationLocation(MainConfig.getInstance().getDatabaseType())})
         .validateMigrationNaming(true)
         .baselineOnMigrate(true)
         .table(MainConfig.getInstance().getPrefix() + "flyway_schema_history");
      return DatabaseStrategyFactory.getStrategy(connectionFactory).configureFlyway(baseConfig);
   }
}
