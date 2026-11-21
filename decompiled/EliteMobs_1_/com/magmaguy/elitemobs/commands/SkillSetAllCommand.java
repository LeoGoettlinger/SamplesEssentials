package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkillSetAllCommand extends AdvancedCommand {
   public SkillSetAllCommand() {
      super(List.of("skill"));
      this.addLiteral("setAll");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em skill setAll <player> <level>");
      this.setPermission("elitemobs.skill.admin");
      this.setDescription("Sets all of a player's skills to the specified level.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         String playerName = commandData.getStringArgument("player");
         int level = parsedLevel;
         Player targetPlayer = Bukkit.getPlayer(playerName);
         if (targetPlayer == null) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillPlayerNotFoundMessage().replace("$player", playerName));
         } else if (level < 1) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelMinMessage());
         } else {
            if (level > 100) {
               Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelWarningMessage().replace("$level", String.valueOf(level)));
            }

            long targetXP;
            try {
               targetXP = SkillXPCalculator.totalXPForLevel(level);
            } catch (IllegalArgumentException var12) {
               Logger.sendMessage(commandData.getCommandSender(), "&c" + var12.getMessage());
               return;
            }

            for (SkillType skillType : SkillType.values()) {
               PlayerData.setSkillXP(targetPlayer.getUniqueId(), skillType, targetXP);
            }

            CombatLevelDisplay.updateDisplay(targetPlayer);
            ArmorSkillHealthBonus.updateHealthBonus(targetPlayer);
            Logger.sendMessage(
               commandData.getCommandSender(),
               CommandMessagesConfig.getSkillSetAllSuccessMessage()
                  .replace("$player", targetPlayer.getName())
                  .replace("$level", String.valueOf(level))
                  .replace("$xp", String.valueOf(targetXP))
            );
         }
      }
   }
}
