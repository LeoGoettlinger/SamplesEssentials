package net.coreprotect.patch.script;

import java.sql.Statement;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;

public class __2_23_0 {
   protected static boolean patch(Statement statement) {
      try {
         if (Config.getGlobal().MYSQL) {
            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "skull MODIFY owner VARCHAR(255);");
            } catch (Exception var2) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "skull", "{1}", "{1}"));
            }
         }

         __2_23_1.patch(statement);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return true;
   }
}
