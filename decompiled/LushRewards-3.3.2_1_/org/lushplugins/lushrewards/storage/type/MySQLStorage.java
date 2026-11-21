package org.lushplugins.lushrewards.storage.type;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.MysqlDataSource;

public class MySQLStorage extends AbstractSQLStorage {
   @Override
   protected String getInsertOrUpdateStatement(String table, String column) {
      return MessageFormat.format("INSERT INTO `{0}` (uuid, `{1}`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `{1}` = VALUES(`{1}`);", table, column);
   }

   @Override
   protected void setUUIDToStatement(PreparedStatement stmt, int index, UUID uuid) throws SQLException {
      stmt.setString(index, uuid.toString());
   }

   @Override
   protected void setJsonToStatement(PreparedStatement stmt, int index, JsonObject json) throws SQLException {
      stmt.setString(index, json.toString());
   }

   @Override
   protected void assertColumn(String table, String column, String type) {
      this.assertTable(table);

      try (
         Connection conn = this.conn();
         PreparedStatement stmt = conn.prepareStatement(String.format("SELECT `%s` FROM `%s`", column, table));
      ) {
         stmt.executeQuery();
      } catch (SQLException var18) {
         if (var18.getErrorCode() == 1054) {
            try (
               Connection connx = this.conn();
               PreparedStatement stmtx = connx.prepareStatement(String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s;", table, column, type));
            ) {
               stmtx.execute();
            } catch (SQLException var15) {
               LushRewards.getInstance().getLogger().log(Level.SEVERE, "Failed to create column: ", (Throwable)var15);
            }
         } else {
            LushRewards.getInstance().getLogger().log(Level.SEVERE, "Failed to assert column: ", (Throwable)var18);
         }
      }
   }

   @Override
   protected DataSource setupDataSource(ConfigurationSection config) {
      MysqlDataSource dataSource = new MysqlDataSource();
      dataSource.setServerName(config.getString("host"));
      dataSource.setPortNumber(config.getInt("port"));
      if (config.contains("name")) {
         LushRewards.getInstance().getLogger().warning("Deprecated: The 'storage.name' path in the storage.yml has been renamed to 'storage.database'");
         dataSource.setDatabaseName(config.getString("name"));
      } else {
         dataSource.setDatabaseName(config.getString("database"));
      }

      dataSource.setUser(config.getString("user"));
      dataSource.setPassword(config.getString("password"));
      return dataSource;
   }
}
