package org.lushplugins.lushrewards.storage.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.storage.Storage;

public abstract class AbstractSQLStorage extends Storage {
   protected static final String TABLE_NAME = "lushrewards_users";
   protected static final String MODULES_TABLE_NAME = "lushrewards_users_modules";
   private DataSource dataSource;

   @Override
   public void enable(ConfigurationSection config) {
      this.dataSource = this.setupDataSource(config);
      this.testDataSourceConnection();
   }

   @Override
   public JsonObject loadModuleUserDataJson(UUID uuid, String moduleId) {
      String table;
      String column;
      if (moduleId != null) {
         table = "lushrewards_users_modules";
         column = moduleId + "_data";
      } else {
         table = "lushrewards_users";
         column = "data";
      }

      column = this.formatHeader(column);
      this.assertJsonColumn(table, column);

      try {
         JsonObject var16;
         try (
            Connection conn = this.conn();
            PreparedStatement stmt = conn.prepareStatement(String.format("SELECT `%s` FROM `%s` WHERE uuid = ?;", column, table));
         ) {
            this.setUUIDToStatement(stmt, 1, uuid);
            ResultSet resultSet = stmt.executeQuery();
            JsonObject json;
            if (resultSet.next()) {
               String jsonRaw = resultSet.getString(column);
               json = jsonRaw != null ? JsonParser.parseString(jsonRaw).getAsJsonObject() : null;
            } else {
               json = null;
            }

            var16 = json;
         }

         return var16;
      } catch (SQLException var14) {
         LushRewards.getInstance().getLogger().log(Level.SEVERE, "Failed to load user data: ", (Throwable)var14);
         return null;
      }
   }

   @Override
   public void saveModuleUserData(UserDataModule.UserData userData) {
      UUID uuid = userData.getUniqueId();
      String moduleId = userData.getModuleId();
      JsonObject json = userData.asJson();
      if (json == null) {
         throw new NullPointerException("JsonObject cannot be null when saving");
      } else {
         String table;
         String column;
         if (moduleId != null) {
            table = "lushrewards_users_modules";
            column = moduleId + "_data";
         } else {
            table = "lushrewards_users";
            column = "data";
         }

         column = this.formatHeader(column);
         this.assertJsonColumn(table, column);

         try (
            Connection conn = this.conn();
            PreparedStatement stmt = conn.prepareStatement(this.getInsertOrUpdateStatement(table, column));
         ) {
            this.setUUIDToStatement(stmt, 1, uuid);
            this.setJsonToStatement(stmt, 2, json);
            stmt.executeUpdate();
         } catch (SQLException var15) {
            LushRewards.getInstance().getLogger().log(Level.SEVERE, "Failed to save user data: ", (Throwable)var15);
         }
      }
   }

   protected void assertTable(String table) {
      try (
         Connection conn = this.conn();
         PreparedStatement stmt = conn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s`(uuid CHAR(36) NOT NULL, PRIMARY KEY (uuid));", table));
      ) {
         stmt.execute();
      } catch (SQLException var10) {
         LushRewards.getInstance().getLogger().log(Level.SEVERE, "Failed to assert table: ", (Throwable)var10);
      }
   }

   protected abstract String getInsertOrUpdateStatement(String var1, String var2);

   protected abstract void setUUIDToStatement(PreparedStatement var1, int var2, UUID var3) throws SQLException;

   protected abstract void setJsonToStatement(PreparedStatement var1, int var2, JsonObject var3) throws SQLException;

   protected void assertJsonColumn(String table, String column) {
      this.assertColumn(table, column, "JSON");
   }

   protected abstract void assertColumn(String var1, String var2, String var3);

   protected String formatHeader(String string) {
      return string;
   }

   protected Connection conn() {
      try {
         return this.getDataSource().getConnection();
      } catch (SQLException var2) {
         LushRewards.getInstance().log(Level.SEVERE, "An error occurred whilst getting a connection: ", var2);
         return null;
      }
   }

   protected DataSource getDataSource() {
      return this.dataSource;
   }

   protected abstract DataSource setupDataSource(ConfigurationSection var1);

   protected void testDataSourceConnection() {
      try (Connection conn = this.conn()) {
         if (!conn.isValid(1000)) {
            throw new SQLException("Could not establish database connection.");
         }
      } catch (SQLException var6) {
         LushRewards.getInstance().log(Level.SEVERE, "An error occurred while testing the data source ", var6);
      }
   }
}
