package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.NPCCommands;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;

public class PlaceNPCCommand extends AdvancedCommand {
   public PlaceNPCCommand() {
      super(List.of("place"));
      this.addLiteral("npc");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> NPCsConfig.npcEntities.keySet().stream().toList(), "<filename>"));
      this.setUsage("/em place npc <filename>");
      this.setPermission("elitemobs.place.npc");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Permanently adds an npc to the location the user is standing on.");
   }

   @Override
   public void execute(CommandData commandData) {
      NPCCommands.set(commandData.getPlayerSender(), commandData.getStringArgument("filename"));
   }
}
