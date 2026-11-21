package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public final class AdvancedCombatDismissSuggestionCommand extends AdvancedCommand {
   public AdvancedCombatDismissSuggestionCommand() {
      super(List.of("advancedcombat"));
      this.addLiteral("dismiss");
      this.setUsage("/em advancedcombat dismiss");
      this.setDescription("Disables the combat developer message for all administrators on this server.");
      this.setPermission("elitemobs.advancedcombat.admin");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      if (AdvancedCombatSystemConfig.dismissDeveloperMessage()) {
         Logger.sendMessage(
            commandData.getCommandSender(),
            "&7Developer message dismissed for this server. &fshowDeveloperMessage &7is now false in &fAdvancedCombatSystem.yml&7."
         );
      } else {
         Logger.sendMessage(commandData.getCommandSender(), "&cCould not save the developer message dismissal. Check the server log.");
      }
   }
}
