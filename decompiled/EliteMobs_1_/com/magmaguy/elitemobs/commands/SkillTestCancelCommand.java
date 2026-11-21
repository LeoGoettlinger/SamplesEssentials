package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import java.util.List;
import org.bukkit.entity.Player;

public class SkillTestCancelCommand extends AdvancedCommand {
   public SkillTestCancelCommand() {
      super(List.of("debug"));
      this.addLiteral("combat");
      this.addLiteral("cancel");
      this.setUsage("/em debug combat cancel");
      this.setPermission("elitemobs.admin");
      this.setDescription("Cancels an active combat system test.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (commandData.getCommandSender() instanceof Player player) {
         SkillSystemTest session = SkillSystemTest.getSession(player.getUniqueId());
         if (session == null) {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestCancelNoTestMessage());
         } else {
            session.cancel();
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestCancelSuccessMessage());
         }
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
      }
   }
}
