package com.oheers.fish.database;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;

public class DatabaseUtil {
   private DatabaseUtil() {
      throw new UnsupportedOperationException();
   }

   public static void writeDbVerbose(final String message) {
      if (MainConfig.getInstance().doDBVerbose()) {
         EvenMoreFish.getInstance().getLogger().info(() -> message);
      }
   }

   public static boolean isDatabaseOffline() {
      EvenMoreFish plugin = EvenMoreFish.getInstance();
      if (!MainConfig.getInstance().databaseEnabled()) {
         plugin.debug("Database is disabled in config.");
         return true;
      } else {
         Database database = plugin.getPluginDataManager().getDatabase();
         if (database == null) {
            plugin.debug("Database instance is null.");
            return true;
         } else if (database.isLegacyDatabase()) {
            plugin.debug("Database migration manager reports version 2.");
            return true;
         } else {
            plugin.debug("Database is online and usable.");
            return false;
         }
      }
   }

   public static boolean isDatabaseOnline() {
      return !isDatabaseOffline();
   }

   public static void debugDatabaseTypeVersion(@NonNull ConnectionFactory connectionFactory) {
      try (Connection connection = connectionFactory.getConnection()) {
         String version = connection.getMetaData().getDatabaseProductVersion();
         String type = connection.getMetaData().getDatabaseProductName();
         EvenMoreFish.getInstance().debug("%s version %s".formatted(type, version));
      } catch (SQLException var6) {
      }
   }
}
