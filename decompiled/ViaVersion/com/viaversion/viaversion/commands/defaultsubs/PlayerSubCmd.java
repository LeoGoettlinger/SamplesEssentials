package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.ArrayList;
import java.util.List;

public class PlayerSubCmd implements ViaSubCommand {
   @Override
   public String name() {
      return "player";
   }

   @Override
   public String description() {
      return "Shows connection information about one or all players.";
   }

   @Override
   public String usage() {
      return "player <name|*>";
   }

   @Override
   public boolean execute(ViaCommandSender sender, String[] args) {
      if (args.length == 0) {
         return false;
      } else {
         boolean all = args[0].equals("*");
         boolean any = false;

         for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            ProtocolInfo info = connection.getProtocolInfo();
            if (args[0].equalsIgnoreCase(info.getUsername()) || all) {
               this.sendMessage(
                  sender,
                  "&7[&6"
                     + info.getUsername()
                     + "&7] UUID: &2"
                     + info.getUuid()
                     + " &7Client protocol: &2"
                     + info.protocolVersion().getName()
                     + " &7Server protocol: &2"
                     + info.serverProtocolVersion().getName()
                     + " &7Client: &2"
                     + connection.isClientSide(),
                  new Object[0]
               );
               any = true;
            }
         }

         if (!any) {
            this.sendMessage(sender, all ? "&cNo players found!" : "&cNo player found with the name: " + args[0], new Object[0]);
         }

         return true;
      }
   }

   @Override
   public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
      if (args.length != 1) {
         return ViaSubCommand.super.onTabComplete(sender, args);
      } else {
         String input = args[0].toLowerCase();
         List<String> matches = new ArrayList<>();

         for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            String name = connection.getProtocolInfo().getUsername();
            if (input.isEmpty() || name.toLowerCase().startsWith(input)) {
               matches.add(name);
            }
         }

         matches.add("*");
         return matches;
      }
   }
}
