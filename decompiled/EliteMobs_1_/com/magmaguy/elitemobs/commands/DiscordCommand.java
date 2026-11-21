package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public class DiscordCommand extends AdvancedCommand {
   public DiscordCommand() {
      super(List.of("discord"));
      this.setUsage("/em discord");
      this.setPermission("elitemobs.discord.link");
      this.setDescription("Links to the EliteMobs discord.");
   }

   @Override
   public void execute(CommandData commandData) {
      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getDiscordMessage() + "https://discord.gg/eSxvPbWYy4");
   }
}
