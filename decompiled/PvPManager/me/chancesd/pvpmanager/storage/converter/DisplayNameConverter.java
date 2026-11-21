package me.chancesd.pvpmanager.storage.converter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import me.chancesd.pvpmanager.sdutils.database.Converter;
import me.chancesd.pvpmanager.sdutils.database.Database;
import me.chancesd.pvpmanager.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.pvpmanager.sdutils.database.Table;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.ChatColor;

public class DisplayNameConverter implements Converter {
   @Override
   public void onComplete() {
      Log.infoColor(ChatColor.GOLD + ">> Finished database conversion to allow saving bigger display names");
   }

   @Override
   public boolean needsConversion(Database database) {
      return database.getDatabaseType() == DatabaseConfigBuilder.DatabaseType.MYSQL && database.getPlugin().getConfig().getInt("Database Version", 0) < 1;
   }

   @Override
   public Table getOldTable() {
      return new Table("pmr_users", "");
   }

   @Override
   public Table getNewTable() {
      return null;
   }

   @Override
   public int getMinimumDatabaseVersion() {
      return 1;
   }

   @Override
   public void convertAll(Database database, ResultSet results) throws SQLException {
      Log.infoColor(ChatColor.GOLD + ">> Starting database conversion to allow saving bigger display names");
      String columnName = "displayname";

      try (
         Connection connection = database.getConnection();
         Statement statement = connection.createStatement();
      ) {
         String alterQuery = "ALTER TABLE " + this.getOldTable().getName() + " MODIFY displayname VARCHAR(255)";
         statement.executeUpdate(alterQuery);
      } catch (SQLException var12) {
         Log.severe("Failed to run database conversion: " + this.getClass().getSimpleName(), var12);
      }
   }
}
