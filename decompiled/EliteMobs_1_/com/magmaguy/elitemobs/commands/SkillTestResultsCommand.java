package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.testing.TestReport;
import java.util.List;
import org.bukkit.entity.Player;

public class SkillTestResultsCommand extends AdvancedCommand {
   public SkillTestResultsCommand() {
      super(List.of("debug"));
      this.addLiteral("combat");
      this.addLiteral("results");
      this.setUsage("/em debug combat results");
      this.setPermission("elitemobs.admin");
      this.setDescription("Shows the results of the last combat system test.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!(commandData.getCommandSender() instanceof Player player)) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
      } else {
         TestReport report = TestReport.getLastReport(player.getUniqueId());
         if (report == null) {
            Logger.sendMessage(player, "§cNo test results found. Run §e/em debug combat start §cfirst.");
         } else {
            for (String line : report.generateSummary()) {
               player.sendMessage(line.replace("&", "§"));
            }

            for (String line : report.generateDetailedReport()) {
               player.sendMessage(line.replace("&", "§"));
            }
         }
      }
   }
}
