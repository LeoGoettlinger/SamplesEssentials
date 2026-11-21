package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassProgressionForgetResult;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class AdvancedClassTestForgetCommand extends AdvancedCommand {
   public AdvancedClassTestForgetCommand() {
      super(List.of("class"));
      this.addLiteral("test");
      this.addLiteral("forget");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument(
         "class",
         new DynamicListStringCommandArgument(() -> AdvancedClassCommandSupport.catalog().forms().stream().map(ClassFormDefinition::id).toList(), "<class>")
      );
      this.setUsage("/em class test forget <player> <class>");
      this.setDescription("Resets a class and its descendants, removing their unlocks and class XP.");
      this.setPermission("elitemobs.advancedcombat.admin");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!AdvancedCombatModule.isInitialized()) {
         Logger.sendMessage(commandData.getCommandSender(), "&c[Alpha] Advanced Combat System is disabled on this server.");
      } else {
         Player player = Bukkit.getPlayer(commandData.getStringArgument("player"));
         if (player == null) {
            Logger.sendMessage(commandData.getCommandSender(), "&cThat player is not online.");
         } else {
            String formId = commandData.getStringArgument("class");
            ClassProgressionForgetResult result = AdvancedCombatModule.get().forgetClassForAdministration(player, formId);
            switch (result.status()) {
               case APPLIED:
                  String message = "&aForgot &f"
                     + formId
                     + " &aand its descendants for &f"
                     + player.getName()
                     + "&a. Reset unlocks and class XP for &f"
                     + result.formsReset()
                     + " &aforms."
                     + (result.selectionCleared() ? " The affected active class was deactivated." : "");
                  Logger.sendMessage(commandData.getCommandSender(), message);
                  if (!commandData.getCommandSender().equals(player)) {
                     Logger.sendMessage(player, "&dClass debug: &f" + formId + " &7and its descendants were forgotten. Their trials must be completed again.");
                  }
                  break;
               case NOT_READY:
                  Logger.sendMessage(commandData.getCommandSender(), "&eThat player's class profile is still loading.");
                  break;
               case UNKNOWN_FORM:
                  Logger.sendMessage(commandData.getCommandSender(), "&cUnknown class: &f" + formId);
                  break;
               case RUN_LOCKED:
                  Logger.sendMessage(commandData.getCommandSender(), "&cThat player must leave their current run before forgetting a class.");
            }
         }
      }
   }
}
