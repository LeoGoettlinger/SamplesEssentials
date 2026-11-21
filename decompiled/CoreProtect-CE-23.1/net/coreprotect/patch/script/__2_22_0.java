package net.coreprotect.patch.script;

import java.sql.Statement;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.patch.Patch;
import net.coreprotect.utility.Chat;

public class __2_22_0 {
   protected static boolean patch(Statement statement) {
      try {
         if (Config.getGlobal().MYSQL) {
            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_5 VARCHAR(100);");
            } catch (Exception var15) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_6 VARCHAR(100);");
            } catch (Exception var14) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_7 VARCHAR(100);");
            } catch (Exception var13) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_8 VARCHAR(100);");
            } catch (Exception var12) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN color_secondary INT;");
            } catch (Exception var11) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN waxed TINYINT DEFAULT 0;");
            } catch (Exception var10) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN face TINYINT DEFAULT 0;");
            } catch (Exception var9) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }
         } else {
            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_5 TEXT;");
            } catch (Exception var8) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_6 TEXT;");
            } catch (Exception var7) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_7 TEXT;");
            } catch (Exception var6) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN line_8 TEXT;");
            } catch (Exception var5) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN color_secondary INTEGER;");
            } catch (Exception var4) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN waxed TINYINT DEFAULT 0;");
            } catch (Exception var3) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }

            if (!Patch.continuePatch()) {
               return false;
            }

            try {
               statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN face TINYINT DEFAULT 0;");
            } catch (Exception var2) {
               Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", "{1}", "{1}"));
            }
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return true;
   }
}
