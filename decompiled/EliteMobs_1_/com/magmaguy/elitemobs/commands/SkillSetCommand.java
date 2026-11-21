package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkillSetCommand extends AdvancedCommand {
   public SkillSetCommand() {
      super(List.of("skill"));
      this.addLiteral("set");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("skillType", new ListStringCommandArgument(Arrays.stream(SkillType.values()).map(Enum::name).toList(), "<skillType>"));
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em skill set <player> <skillType> <level>");
      this.setPermission("elitemobs.skill.admin");
      this.setDescription("Sets a player's skill level for a specific skill type.");
   }

   @Override
   public void execute(CommandData commandData) {
      String playerName = commandData.getStringArgument("player");
      String skillTypeName = commandData.getStringArgument("skillType");
      Integer level = commandData.getIntegerArgument("level");
      if (level != null) {
         Player targetPlayer = Bukkit.getPlayer(playerName);
         if (targetPlayer == null) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillPlayerNotFoundMessage().replace("$player", playerName));
         } else {
            SkillType skillType;
            try {
               skillType = SkillType.valueOf(skillTypeName.toUpperCase());
            } catch (IllegalArgumentException var11) {
               Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillInvalidTypeMessage().replace("$type", skillTypeName));
               Logger.sendMessage(
                  commandData.getCommandSender(),
                  CommandMessagesConfig.getSkillValidTypesMessage()
                     .replace("$types", String.join(", ", Arrays.stream(SkillType.values()).map(Enum::name).toList()))
               );
               return;
            }

            if (level < 1) {
               Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelMinMessage());
            } else {
               if (level > 100) {
                  Logger.sendMessage(
                     commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelWarningMessage().replace("$level", String.valueOf(level))
                  );
               }

               long targetXP;
               try {
                  targetXP = SkillXPCalculator.totalXPForLevel(level);
               } catch (IllegalArgumentException var10) {
                  Logger.sendMessage(commandData.getCommandSender(), "&c" + var10.getMessage());
                  return;
               }

               PlayerData.setSkillXP(targetPlayer.getUniqueId(), skillType, targetXP);
               CombatLevelDisplay.updateDisplay(targetPlayer);
               if (skillType == SkillType.ARMOR) {
                  ArmorSkillHealthBonus.updateHealthBonus(targetPlayer);
               }

               Logger.sendMessage(
                  commandData.getCommandSender(),
                  CommandMessagesConfig.getSkillSetSuccessMessage()
                     .replace("$player", targetPlayer.getName())
                     .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                     .replace("$level", String.valueOf(level))
                     .replace("$xp", String.valueOf(targetXP))
               );
            }
         }
      }
   }
}
