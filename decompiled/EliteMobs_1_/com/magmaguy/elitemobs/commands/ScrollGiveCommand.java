package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ScrollGiveCommand extends AdvancedCommand {
   public ScrollGiveCommand() {
      super(List.of("scrollGive"));
      this.addArgument("playerName", new PlayerCommandArgument());
      this.setUsage("/em scrollGive <player>");
      this.setPermission("elitemobs.scroll.give");
      this.setDescription("Gives the target player an Elite Scroll scaled to their combat level");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollsNotEnabledMessage());
      } else {
         Player player = Bukkit.getPlayer(commandData.getStringArgument("playerName"));
         if (player == null) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getInvalidPlayerForItemMessage());
         } else {
            int level = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
            ItemStack scroll = EliteScroll.generateScroll(level, player);

            for (ItemStack overflow : player.getInventory().addItem(new ItemStack[]{scroll}).values()) {
               player.getWorld().dropItem(player.getLocation(), overflow);
            }

            Logger.sendMessage(
               commandData.getCommandSender(),
               CommandMessagesConfig.getScrollGaveMessage()
                  .replace("$amount", "1")
                  .replace("$level", String.valueOf(level))
                  .replace("$player", player.getName())
            );
         }
      }
   }
}
