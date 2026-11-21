package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class LootRewardCommand extends AdvancedCommand {
   public LootRewardCommand() {
      super(List.of("loot"));
      this.addLiteral("reward");
      this.addArgument("playerName", new PlayerCommandArgument());
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomItemsConfig.getCustomItems().keySet().stream().toList(), "<filename>"));
      this.setUsage("/em loot reward <player> <filename>");
      this.setPermission("elitemobs.loot.admin");
      this.setDescription("Gives the specified loot to a player, scaled to their combat level.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.admin.LootCommand.reward(
         commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getStringArgument("filename")
      );
   }
}
