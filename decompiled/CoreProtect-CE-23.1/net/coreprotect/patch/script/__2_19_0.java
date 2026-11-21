package net.coreprotect.patch.script;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.language.Phrase;
import net.coreprotect.patch.Patch;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.Tag;

public class __2_19_0 {
   protected static boolean patch(Statement statement) {
      try {
         if (Config.getGlobal().MYSQL) {
            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN action int");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign DROP INDEX wid");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD INDEX(wid,x,z,time), ADD INDEX(user,time), ADD INDEX(time)");
            } catch (Exception var20) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate(
                  "ALTER TABLE "
                     + ConfigHandler.prefix
                     + "chat ADD COLUMN wid int, ADD COLUMN x int, ADD COLUMN y int, ADD COLUMN z int, ADD INDEX(wid,x,z,time)"
               );
            } catch (Exception var19) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "chat", "{1}", "{1}"));
            }

            try {
               statement.executeUpdate(
                  "ALTER TABLE "
                     + ConfigHandler.prefix
                     + "command ADD COLUMN wid int, ADD COLUMN x int, ADD COLUMN y int, ADD COLUMN z int, ADD INDEX(wid,x,z,time)"
               );
            } catch (Exception var18) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "command", "{1}", "{1}"));
            }
         } else {
            try {
               statement.executeUpdate("DROP INDEX IF EXISTS sign_index;");
            } catch (Exception var17) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{2}", "{3}"));
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN action INTEGER;");
            } catch (Exception var16) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            try {
               statement.executeUpdate("CREATE INDEX IF NOT EXISTS sign_index ON " + ConfigHandler.prefix + "sign(wid,x,z,time);");
               statement.executeUpdate("CREATE INDEX IF NOT EXISTS sign_user_index ON " + ConfigHandler.prefix + "sign(user,time);");
               statement.executeUpdate("CREATE INDEX IF NOT EXISTS sign_time_index ON " + ConfigHandler.prefix + "sign(time);");
            } catch (Exception var15) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{2}", "{2}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "chat ADD COLUMN wid INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "chat ADD COLUMN x INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "chat ADD COLUMN y INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "chat ADD COLUMN z INTEGER;");
            } catch (Exception var14) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "chat", "{1}", "{1}"));
            }

            try {
               statement.executeUpdate("CREATE INDEX IF NOT EXISTS chat_wid_index ON " + ConfigHandler.prefix + "chat(wid,x,z,time);");
            } catch (Exception var13) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "chat", "{2}", "{2}"));
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "command ADD COLUMN wid INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "command ADD COLUMN x INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "command ADD COLUMN y INTEGER;");
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "command ADD COLUMN z INTEGER;");
            } catch (Exception var12) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "command", "{1}", "{1}"));
            }

            try {
               statement.executeUpdate("CREATE INDEX IF NOT EXISTS command_wid_index ON " + ConfigHandler.prefix + "command(wid,x,z,time);");
            } catch (Exception var11) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "command", "{2}", "{2}"));
            }
         }

         if (!Patch.continuePatch()) {
            return false;
         }

         List<Integer> signList = new ArrayList<>();

         for (Material material : Tag.SIGNS.getValues()) {
            int id = MaterialUtils.getBlockId(material.name(), false);
            if (id > -1) {
               signList.add(id);
            }
         }

         if (signList.size() == 0) {
            return true;
         }

         StringBuilder signData = new StringBuilder();

         for (Integer id : signList) {
            if (signData.length() == 0) {
               signData = signData.append(id);
            } else {
               signData.append(",").append(id);
            }
         }

         String blockQuery = "SELECT time, user, wid, x, y, z FROM "
            + ConfigHandler.prefix
            + "block WHERE type IN("
            + signData.toString()
            + ") AND action='1' ORDER BY rowid ASC";
         String preparedSignQuery = "SELECT rowid as id FROM "
            + ConfigHandler.prefix
            + "sign WHERE user = ? AND wid = ? AND x = ? AND y = ? AND z = ? AND time >= ? ORDER BY rowid ASC LIMIT 0, 1";
         String preparedQueryUpdate = "UPDATE " + ConfigHandler.prefix + "sign SET action = 1 WHERE rowid = ?";
         PreparedStatement preparedSignStatement = statement.getConnection().prepareStatement(preparedSignQuery);
         PreparedStatement preparedStatementUpdate = statement.getConnection().prepareStatement(preparedQueryUpdate);
         Database.beginTransaction(statement, Config.getGlobal().MYSQL);
         ResultSet resultSet = statement.executeQuery(blockQuery);

         while (resultSet.next()) {
            preparedSignStatement.setInt(1, resultSet.getInt("user"));
            preparedSignStatement.setInt(2, resultSet.getInt("wid"));
            preparedSignStatement.setInt(3, resultSet.getInt("x"));
            preparedSignStatement.setInt(4, resultSet.getInt("y"));
            preparedSignStatement.setInt(5, resultSet.getInt("z"));
            preparedSignStatement.setInt(6, resultSet.getInt("time"));
            ResultSet signResults = preparedSignStatement.executeQuery();

            while (signResults.next()) {
               int idx = signResults.getInt("id");
               preparedStatementUpdate.setInt(1, idx);
               preparedStatementUpdate.executeUpdate();
            }

            signResults.close();
         }

         resultSet.close();
         preparedSignStatement.close();
         preparedStatementUpdate.close();
         Database.commitTransaction(statement, Config.getGlobal().MYSQL);
      } catch (Exception var21) {
         var21.printStackTrace();
      }

      return true;
   }
}
