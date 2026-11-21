package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.List;

public class AltCommand extends AdvancedCommand {
   public AltCommand() {
      super(List.of("alt"));
      this.setUsage("/em alt");
      this.setDescription("Changes the style of the /em menu.");
      this.setPermission("elitemobs.alt");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PlayerData.setUseBookMenus(commandData.getPlayerSender(), !PlayerData.getUseBookMenus(commandData.getPlayerSender().getUniqueId()));
      Logger.sendMessage(commandData.getCommandSender(), DefaultConfig.getSwitchEMStyleMessage());
   }
}
