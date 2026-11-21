package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.instance.InstanceProtector;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public class ProtectionBypassCommand extends AdvancedCommand {
   public ProtectionBypassCommand() {
      super(List.of("protection"));
      this.addLiteral("bypass");
      this.setPermission("elitemobs.protection.bypass");
      this.setDescription("Allows admins to toggle bypassing protections");
      this.setUsage("/em protection bypass");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      boolean outcome = InstanceProtector.toggleBypass(commandData.getPlayerSender().getUniqueId());
      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getProtectionBypassMessage().replace("$status", String.valueOf(outcome)));
   }
}
