package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import java.util.List;
import org.bukkit.entity.Player;

public class SkillTestCommand extends AdvancedCommand {
   public SkillTestCommand() {
      super(List.of("debug"));
      this.addLiteral("combat");
      this.addLiteral("start");
      this.setUsage("/em debug combat start");
      this.setPermission("elitemobs.admin");
      this.setDescription("Starts the automated combat system test.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (commandData.getCommandSender() instanceof Player player) {
         if (SkillSystemTest.hasActiveSession(player.getUniqueId())) {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestAlreadyRunningMessage());
         } else {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestStartMessage());
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestInfoMessage1());
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestInfoMessage2());
            SkillSystemTest test = new SkillSystemTest(player);
            test.start();
         }
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
      }
   }
}
