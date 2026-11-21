package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class LootGiveCommand extends AdvancedCommand {
   public LootGiveCommand() {
      super(List.of("loot"));
      this.addLiteral("give");
      this.addArgument("playerName", new PlayerCommandArgument());
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomItemsConfig.getCustomItems().keySet().stream().toList(), "<filename>"));
      this.setUsage("/em loot give <player> <filename>");
      this.setPermission("elitemobs.loot.admin");
      this.setDescription("Gives the specified loot to a specific player.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.admin.LootCommand.give(
         commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getStringArgument("filename")
      );
   }
}
