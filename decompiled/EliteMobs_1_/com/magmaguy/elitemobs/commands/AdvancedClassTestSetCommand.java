package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.progression.ClassProgressionSetResult;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class AdvancedClassTestSetCommand extends AdvancedCommand {
   public AdvancedClassTestSetCommand() {
      super(List.of("class"));
      this.addLiteral("test");
      this.addLiteral("set");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument(
         "class",
         new DynamicListStringCommandArgument(() -> AdvancedClassCommandSupport.catalog().forms().stream().map(ClassFormDefinition::id).toList(), "<class>")
      );
      this.addArgument("level", new IntegerCommandArgument("<effectiveLevel>"));
      this.setUsage("/em class test set <player> <class> <effectiveLevel>");
      this.setDescription("Sets and selects an exact [Alpha] Advanced Combat System branch level for testing.");
      this.setPermission("elitemobs.advancedcombat.admin");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         if (!AdvancedCombatModule.isInitialized()) {
            Logger.sendMessage(commandData.getCommandSender(), "&c[Alpha] Advanced Combat System is disabled on this server.");
         } else {
            String playerName = commandData.getStringArgument("player");
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
               Logger.sendMessage(commandData.getCommandSender(), "&cPlayer &f" + playerName + " &cis not online.");
            } else {
               String formId = commandData.getStringArgument("class");
               int level = parsedLevel;
               ClassProgressionSetResult result = AdvancedCombatModule.get().setClassLevelForAdministration(player, formId, level);
               report(commandData, player, result);
            }
         }
      }
   }

   private static void report(CommandData commandData, Player player, ClassProgressionSetResult result) {
      switch (result.status()) {
         case APPLIED:
            String message = "&aSet and selected &f"
               + result.formId()
               + " "
               + result.requestedEffectiveLevel()
               + " &afor &f"
               + player.getName()
               + "&a. Ancestor bands were completed for this branch.";
            Logger.sendMessage(commandData.getCommandSender(), message);
            if (!commandData.getCommandSender().equals(player)) {
               Logger.sendMessage(player, "&dAdvanced tester setup: &f" + result.formId() + " " + result.requestedEffectiveLevel() + " &7is now selected.");
            }
            break;
         case NOT_READY:
            Logger.sendMessage(commandData.getCommandSender(), "&eThat player's class profile is still loading.");
            break;
         case UNKNOWN_FORM:
            Logger.sendMessage(commandData.getCommandSender(), "&cUnknown class form: &f" + result.formId());
            break;
         case LEVEL_OUTSIDE_FORM_BAND:
            ClassFormDefinition form = AdvancedClassCommandSupport.catalog().require(result.formId());
            Logger.sendMessage(
               commandData.getCommandSender(),
               "&c"
                  + form.displayName()
                  + " accepts effective levels &f"
                  + form.band().effectiveStart()
                  + "-"
                  + (form.band().isTerminal() ? "100+" : form.band().effectiveEnd())
                  + "&c."
            );
            break;
         case FOUNDATION_SKILL_CAP:
            ClassFormDefinition blocker = AdvancedClassCommandSupport.catalog().require(result.blockingFormId());
            String skills = String.join(" and ", result.limitingSkills().stream().map(skill -> skill.getDisplayName()).toList());
            Logger.sendMessage(
               commandData.getCommandSender(),
               "&cCannot prepare that branch: &f"
                  + blocker.displayName()
                  + " &cis capped at &f"
                  + result.effectiveCap()
                  + " &cby &f"
                  + skills
                  + "&c. Raise the foundation skills first; for broad testing use &f/em skill setAll "
                  + player.getName()
                  + " 100&c."
            );
            break;
         case RUN_LOCKED:
            Logger.sendMessage(
               commandData.getCommandSender(), "&cThat player is in a class-locked dungeon run. Have them leave before changing test progression."
            );
      }
   }
}
