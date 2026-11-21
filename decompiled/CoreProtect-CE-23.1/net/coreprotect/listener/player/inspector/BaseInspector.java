package net.coreprotect.listener.player.inspector;

import java.sql.Connection;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Color;
import org.bukkit.entity.Player;

public abstract class BaseInspector {
   protected void checkPreconditions(Player player) throws BaseInspector.InspectionException {
      if (ConfigHandler.converterRunning) {
         throw new BaseInspector.InspectionException(Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
      } else if (ConfigHandler.purgeRunning) {
         throw new BaseInspector.InspectionException(Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
      } else {
         if (ConfigHandler.lookupThrottle.get(player.getName()) != null) {
            Object[] lookupThrottle = ConfigHandler.lookupThrottle.get(player.getName());
            if ((Boolean)lookupThrottle[0] || System.currentTimeMillis() - (Long)lookupThrottle[1] < 100L) {
               throw new BaseInspector.InspectionException(Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
            }
         }
      }
   }

   protected Connection getDatabaseConnection(Player player) throws Exception {
      ConfigHandler.lookupThrottle.put(player.getName(), new Object[]{true, System.currentTimeMillis()});
      Connection connection = Database.getConnection(true);
      if (connection == null) {
         throw new BaseInspector.InspectionException(Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
      } else {
         return connection;
      }
   }

   protected void finishInspection(Player player) {
      ConfigHandler.lookupThrottle.put(player.getName(), new Object[]{false, System.currentTimeMillis()});
   }

   public static class InspectionException extends Exception {
      private static final long serialVersionUID = 1L;

      public InspectionException(String message) {
         super(message);
      }
   }
}
