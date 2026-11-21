package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class UnbindForceCommand extends AdvancedCommand {
   public UnbindForceCommand() {
      super(List.of("unbind"));
      this.addLiteral("force");
      this.setUsage("/em unbind force");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.unbind.force");
      this.setDescription("Forcefully unbinds a held item.");
   }

   @Override
   public void execute(CommandData commandData) {
      ItemStack itemStack = commandData.getPlayerSender().getInventory().getItemInMainHand();
      if (itemStack != null && itemStack.hasItemMeta() && SoulbindEnchantment.itemHasSoulbindEnchantment(itemStack.getItemMeta())) {
         commandData.getPlayerSender().getInventory().setItemInMainHand(ItemConsumables.unbind(itemStack));
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getUnbindForceSuccessMessage());
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getUnbindForceNotSoulboundMessage());
      }
   }
}
