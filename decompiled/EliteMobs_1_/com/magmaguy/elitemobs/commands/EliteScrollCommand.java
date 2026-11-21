package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.EliteScrollMenu;
import java.util.List;

public class EliteScrollCommand extends AdvancedCommand {
   public EliteScrollCommand() {
      super(List.of("scroll"));
      this.setUsage("/em scroll");
      this.setPermission("elitemobs.scroll.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the elite scroll menu or teleports the player to the Adventurer's Guild Hub");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getEliteScrollConfigMessage());
      } else {
         if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
            new EliteScrollMenu(commandData.getPlayerSender());
         }
      }
   }
}
