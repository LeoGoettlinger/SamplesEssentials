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
import org.bukkit.inventory.PlayerInventory;

public class UnbindForceAllCommand extends AdvancedCommand {
   public UnbindForceAllCommand() {
      super(List.of("unbind"));
      this.addLiteral("force");
      this.addLiteral("all");
      this.setUsage("/em unbind force all");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.unbind.force");
      this.setDescription("Forcefully unbinds every soulbound item in your inventory.");
   }

   @Override
   public void execute(CommandData commandData) {
      PlayerInventory inventory = commandData.getPlayerSender().getInventory();
      int unbound = 0;

      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack itemStack = inventory.getItem(slot);
         if (itemStack != null && itemStack.hasItemMeta() && SoulbindEnchantment.itemHasSoulbindEnchantment(itemStack.getItemMeta())) {
            inventory.setItem(slot, ItemConsumables.unbind(itemStack));
            unbound++;
         }
      }

      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getUnbindForceAllMessage().replace("$amount", String.valueOf(unbound)));
   }
}
