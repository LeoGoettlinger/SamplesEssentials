package net.coreprotect.command.lookup;

import java.sql.Connection;
import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.lookup.BlockLookup;
import net.coreprotect.database.lookup.InteractionLookup;
import net.coreprotect.database.lookup.SignMessageLookup;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BlockLookupThread implements Runnable {
   private final CommandSender player;
   private final Command command;
   private final Block block;
   private final BlockState blockState;
   private final int page;
   private final int limit;
   private final int type;

   public BlockLookupThread(CommandSender player, Command command, Block block, BlockState blockState, int page, int limit, int type) {
      this.player = player;
      this.command = command;
      this.block = block;
      this.blockState = blockState;
      this.page = page;
      this.limit = limit;
      this.type = type;
   }

   @Override
   public void run() {
      try (Connection connection = Database.getConnection(true)) {
         ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{true, System.currentTimeMillis()});
         if (connection == null) {
            Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
         } else {
            Statement statement = connection.createStatement();
            if (this.type == 8) {
               for (String signMessage : SignMessageLookup.performLookup(
                  this.command.getName(), statement, this.blockState.getLocation(), this.player, this.page, this.limit
               )) {
                  String bypass = null;
                  if (signMessage.contains("\n")) {
                     String[] split = signMessage.split("\n");
                     signMessage = split[0];
                     bypass = split[1];
                  }

                  if (signMessage.length() > 0) {
                     Chat.sendComponent(this.player, signMessage, bypass);
                  }
               }
            } else {
               String blockdata = null;
               if (this.type == 7) {
                  blockdata = InteractionLookup.performLookup(this.command.getName(), statement, this.block, this.player, 0, this.page, this.limit);
               } else {
                  blockdata = BlockLookup.performLookup(this.command.getName(), statement, this.blockState, this.player, 0, this.page, this.limit);
               }

               if (blockdata.contains("\n")) {
                  for (String b : blockdata.split("\n")) {
                     Chat.sendComponent(this.player, b);
                  }
               } else if (blockdata.length() > 0) {
                  Chat.sendComponent(this.player, blockdata);
               }
            }

            statement.close();
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

      ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{false, System.currentTimeMillis()});
   }
}
