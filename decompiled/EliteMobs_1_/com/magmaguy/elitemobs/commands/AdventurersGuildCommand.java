package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.ArrayList;

public class AdventurersGuildCommand extends AdvancedCommand {
   public AdventurersGuildCommand() {
      super(new ArrayList<>());
      this.setUsage("/em adventurersguild");
      this.setPermission("elitemobs.adventurersguild.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Teleports players to the Adventurer's Guild Hub or opens the Adventurer's Guild menu.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildCommand(commandData.getPlayerSender());
   }
}
