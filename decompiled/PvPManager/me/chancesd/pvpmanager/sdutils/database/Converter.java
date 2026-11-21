package me.chancesd.pvpmanager.sdutils.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public interface Converter {
   default boolean onDatabaseLoad(Database database) {
      try (Connection connection = database.getConnection()) {
         if (this.needsConversion(database)) {
            Table oldTable = this.getOldTable();

            try (
               PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + oldTable.getName());
               ResultSet entries = ps.executeQuery();
            ) {
               this.convertAll(database, entries);
            }

            return true;
         } else {
            return false;
         }
      } catch (SQLException var14) {
         database.getPlugin().getLogger().log(Level.WARNING, "Failed to convert database", (Throwable)var14);
         return false;
      }
   }

   void onComplete();

   default boolean needsConversion(Database database) {
      return database.getPlugin().getConfig().getInt("Database Version") < this.getMinimumDatabaseVersion();
   }

   Table getOldTable();

   Table getNewTable();

   int getMinimumDatabaseVersion();

   void convertAll(Database var1, ResultSet var2) throws SQLException;
}
