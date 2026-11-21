package net.coreprotect.command.lookup;

import java.sql.Connection;
import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.lookup.ChestTransactionLookup;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ChestTransactionLookupThread implements Runnable {
   private final CommandSender player;
   private final Command command;
   private final Location location;
   private final int page;
   private final int limit;

   public ChestTransactionLookupThread(CommandSender player, Command command, Location location, int page, int limit) {
      this.player = player;
      this.command = command;
      this.location = location;
      this.page = page;
      this.limit = limit;
   }

   @Override
   public void run() {
      try (Connection connection = Database.getConnection(true)) {
         ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{true, System.currentTimeMillis()});
         if (connection == null) {
            Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
         } else {
            Statement statement = connection.createStatement();

            for (String data : ChestTransactionLookup.performLookup(this.command.getName(), statement, this.location, this.player, this.page, this.limit, false)) {
               Chat.sendComponent(this.player, data);
            }

            statement.close();
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{false, System.currentTimeMillis()});
   }
}
