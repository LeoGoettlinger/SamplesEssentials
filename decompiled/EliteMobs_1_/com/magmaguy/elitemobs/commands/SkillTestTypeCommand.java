package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

public class SkillTestTypeCommand extends AdvancedCommand {
   public SkillTestTypeCommand() {
      super(List.of("debug"));
      this.addLiteral("combat");
      this.addLiteral("test");
      this.addArgument("type", new ListStringCommandArgument(Arrays.stream(SkillType.values()).map(t -> t.name().toLowerCase()).toList(), "<type>"));
      this.setUsage("/em debug combat test <type>");
      this.setPermission("elitemobs.admin");
      this.setDescription("Tests skills for a specific weapon type (e.g., swords, bows).");
   }

   @Override
   public void execute(CommandData commandData) {
      if (commandData.getCommandSender() instanceof Player player) {
         if (SkillSystemTest.hasActiveSession(player.getUniqueId())) {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestAlreadyRunningMessage());
         } else {
            String typeArg = commandData.getStringArgument("type");

            SkillType filterType;
            try {
               filterType = SkillType.valueOf(typeArg.toUpperCase());
            } catch (IllegalArgumentException var6) {
               Logger.sendMessage(player, "§cUnknown weapon type: §e" + typeArg);
               Logger.sendMessage(
                  player, "§7Valid types: §f" + String.join(", ", Arrays.stream(SkillType.values()).map(t -> t.name().toLowerCase()).toArray(String[]::new))
               );
               return;
            }

            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestStartMessage());
            Logger.sendMessage(player, "§7Testing §e" + filterType.getDisplayName() + "§7 skills only.");
            SkillSystemTest test = new SkillSystemTest(player, filterType);
            test.start();
         }
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
      }
   }
}
