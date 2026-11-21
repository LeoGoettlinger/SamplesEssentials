package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import java.util.List;

public class DiscordMessageCommand extends AdvancedCommand {
   public DiscordMessageCommand() {
      super(List.of("discord"));
      this.addVarargsArgument("message", new ListStringCommandArgument("Message to send to discord"));
      this.setUsage("/em discord <message>");
      this.setPermission("elitemobs.discord.message");
      this.setDescription("Sends a message to the Discord via DiscordSRV, for debugging purposes");
   }

   @Override
   public void execute(CommandData commandData) {
      new DiscordSRVAnnouncement(commandData.getStringSequenceArgument("message"));
      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getDiscordMessageSentMessage());
   }
}
