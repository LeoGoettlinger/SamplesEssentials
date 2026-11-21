package org.lushplugins.lushrewards.storage.type;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushrewards.LushRewards;

public class SQLiteStorage extends MySQLStorage {
   private static final String DATABASE_PATH = new File(LushRewards.getInstance().getDataFolder(), "data.db").getAbsolutePath();

   @Override
   protected String getInsertOrUpdateStatement(String table, String column) {
      return MessageFormat.format("INSERT INTO `{0}`(uuid, `{1}`) VALUES(?, ?) ON CONFLICT (uuid) DO UPDATE SET `{1}` = EXCLUDED.`{1}`;", table, column);
   }

   @Override
   protected Connection conn() {
      try {
         return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
      } catch (SQLException var2) {
         LushRewards.getInstance().log(Level.SEVERE, "An error occurred whilst getting a connection: ", var2);
         return null;
      }
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
         if (var18.getMessage().contains("no such column")) {
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
      return null;
   }
}
