package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.ScrapperMenu;
import java.util.List;

public class ScrapCommand extends AdvancedCommand {
   public ScrapCommand() {
      super(List.of("scrap"));
      this.setUsage("/em scrap");
      this.setPermission("elitemobs.scrap.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the scrap menu or teleports the player to the Adventurer's Guild Hub.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         ScrapperMenu scrapperMenu = new ScrapperMenu();
         scrapperMenu.constructScrapMenu(commandData.getPlayerSender());
      }
   }
}
