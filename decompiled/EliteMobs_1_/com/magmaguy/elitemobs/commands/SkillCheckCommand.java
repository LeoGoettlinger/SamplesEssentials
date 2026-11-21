package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkillCheckCommand extends AdvancedCommand {
   public SkillCheckCommand() {
      super(List.of("skill"));
      this.addLiteral("check");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em skill check <player>");
      this.setPermission("elitemobs.skill.check");
      this.setDescription("Displays a player's skill levels.");
   }

   @Override
   public void execute(CommandData commandData) {
      String playerName = commandData.getStringArgument("player");
      Player targetPlayer = Bukkit.getPlayer(playerName);
      if (targetPlayer == null) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillPlayerNotFoundMessage().replace("$player", playerName));
      } else {
         Logger.sendSimpleMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillCheckHeaderMessage().replace("$player", targetPlayer.getName()));

         for (SkillType skillType : SkillType.values()) {
            long xp = PlayerData.getSkillXP(targetPlayer.getUniqueId(), skillType);
            int level = SkillXPCalculator.levelFromTotalXP(xp);
            long xpProgress = SkillXPCalculator.xpProgressInCurrentLevel(xp);
            long xpNeeded = SkillXPCalculator.xpToNextLevel(level);
            double progress = SkillXPCalculator.levelProgress(xp) * 100.0;
            Logger.sendSimpleMessage(
               commandData.getCommandSender(),
               CommandMessagesConfig.getSkillCheckEntryFormat()
                  .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                  .replace("$level", String.valueOf(level))
                  .replace("$xpProgress", this.formatNumber(xpProgress))
                  .replace("$xpNeeded", this.formatNumber(xpNeeded))
                  .replace("$progress", String.format("%.1f", progress))
            );
         }
      }
   }

   private String formatNumber(long number) {
      if (number >= 1000000L) {
         return String.format("%.1fM", number / 1000000.0);
      } else {
         return number >= 1000L ? String.format("%.1fK", number / 1000.0) : String.valueOf(number);
      }
   }
}
