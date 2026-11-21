package net.coreprotect.services;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.VersionUtils;
import org.bukkit.Bukkit;

public class VersionCheckService {
   private VersionCheckService() {
      throw new IllegalStateException("Utility class");
   }

   public static boolean performVersionChecks() {
      try {
         String[] bukkitVersion = Bukkit.getServer().getBukkitVersion().split("[-.]");
         if (VersionUtils.newVersion(bukkitVersion[0] + "." + bukkitVersion[1], "1.16")) {
            Chat.console(Phrase.build(Phrase.VERSION_REQUIRED, "Minecraft", "1.16"));
            return false;
         } else if (VersionUtils.newVersion(
               "1.21.11",
               bukkitVersion[0] + "." + bukkitVersion[1] + (bukkitVersion.length > 2 && bukkitVersion[2].matches("\\d+") ? "." + bukkitVersion[2] : "")
            )
            && VersionUtils.isCommunityEdition()) {
            Chat.console(
               Phrase.build(
                  Phrase.VERSION_INCOMPATIBLE,
                  "Minecraft",
                  bukkitVersion[0] + "." + bukkitVersion[1] + (bukkitVersion.length > 2 ? "." + bukkitVersion[2] : "")
               )
            );
            return false;
         } else {
            String[] javaVersion = (System.getProperty("java.version").replaceAll("[^0-9.]", "") + ".0").split("\\.");
            if (VersionUtils.newVersion(javaVersion[0] + "." + javaVersion[1], "11.0")) {
               Chat.console(Phrase.build(Phrase.VERSION_REQUIRED, "Java", "11.0"));
               return false;
            } else if (VersionUtils.newVersion("23.1", VersionUtils.getPluginVersion()) && !VersionUtils.isBranch("dev")) {
               Chat.console(Phrase.build(Phrase.VERSION_INCOMPATIBLE, "CoreProtect", "v" + VersionUtils.getPluginVersion()));
               Chat.sendConsoleMessage(Color.GREY + "[CoreProtect] " + Phrase.build(Phrase.INVALID_BRANCH_2));
               return false;
            } else if (ConfigHandler.EDITION_BRANCH.length() == 0) {
               Chat.sendConsoleMessage(Color.RED + "[CoreProtect] " + Phrase.build(Phrase.INVALID_BRANCH_1));
               Chat.sendConsoleMessage(Color.GREY + "[CoreProtect] " + Phrase.build(Phrase.INVALID_BRANCH_2));
               Chat.sendConsoleMessage(Color.GREY + "[CoreProtect] " + Phrase.build(Phrase.INVALID_BRANCH_3));
               return false;
            } else {
               ConfigHandler.SERVER_VERSION = Integer.parseInt(bukkitVersion[1]);
               return true;
            }
         }
      } catch (Exception var2) {
         var2.printStackTrace();
         return false;
      }
   }
}
