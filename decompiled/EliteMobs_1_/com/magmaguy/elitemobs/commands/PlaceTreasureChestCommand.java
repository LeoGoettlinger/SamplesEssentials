package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;

public class PlaceTreasureChestCommand extends AdvancedCommand {
   public PlaceTreasureChestCommand() {
      super(List.of("place"));
      this.addLiteral("treasureChest");
      this.addArgument(
         "filename",
         new DynamicListStringCommandArgument(() -> CustomTreasureChestsConfig.getCustomTreasureChestConfigFields().keySet().stream().toList(), "<filename>")
      );
      this.setUsage("/em place treasureChest <filename>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Permanently adds a treasure chest to the location the user is standing on.");
   }

   @Override
   public void execute(CommandData commandData) {
      CustomTreasureChestsConfig.addTreasureChestEntry(commandData.getPlayerSender(), commandData.getStringArgument("filename"));
   }
}
