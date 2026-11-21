package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ScrollGetCommand extends AdvancedCommand {
   public ScrollGetCommand() {
      super(List.of("scrollGet"));
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addArgument("amount", new IntegerCommandArgument("<amount>"));
      this.setUsage("/em scrollGet <level> <amount>");
      this.setPermission("elitemobs.scroll.get");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Gives the player a specified number of elite scrolls at a given level");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollsNotEnabledMessage());
      } else {
         Integer level = commandData.getIntegerArgument("level");
         Integer amount = commandData.getIntegerArgument("amount");
         if (level != null && amount != null) {
            if (level <= 0) {
               Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollLevelZeroMessage());
            } else if (amount <= 0) {
               Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollAmountZeroMessage());
            } else {
               Player player = commandData.getPlayerSender();

               for (int i = 0; i < amount; i++) {
                  for (ItemStack overflow : player.getInventory().addItem(new ItemStack[]{EliteScroll.generateScroll(level, player)}).values()) {
                     player.getWorld().dropItem(player.getLocation(), overflow);
                  }
               }

               Logger.sendMessage(
                  commandData.getCommandSender(),
                  CommandMessagesConfig.getScrollGaveMessage()
                     .replace("$amount", String.valueOf(amount))
                     .replace("$level", String.valueOf(level))
                     .replace("$player", player.getName())
               );
            }
         }
      }
   }
}
