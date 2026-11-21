package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import java.util.List;

public class PlaceWormholeCommand extends AdvancedCommand {
   public PlaceWormholeCommand() {
      super(List.of("place"));
      this.addLiteral("wormhole");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> WormholeConfig.getWormholes().keySet().stream().toList(), "<filename>"));
      this.addArgument("wormholeOption", new IntegerCommandArgument(List.of(1, 2), "<1>/<2>"));
      this.setUsage("/em place wormhole <filename> <1/2>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Place a wormhole teleport at your current location.");
   }

   @Override
   public void execute(CommandData commandData) {
      for (Wormhole wormhole : Wormhole.getWormholes()) {
         if (wormhole.getWormholeConfigFields().getFilename().equals(commandData.getStringArgument("filename"))) {
            String var4 = commandData.getStringArgument("wormholeOption");
            switch (var4) {
               case "1":
                  wormhole.getWormholeEntry1().updateLocation(commandData.getPlayerSender());
                  return;
               case "2":
                  wormhole.getWormholeEntry2().updateLocation(commandData.getPlayerSender());
                  return;
               default:
                  Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getWormholeInvalidOptionMessage());
            }
         }
      }

      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getWormholeSetFailedMessage());
   }
}
