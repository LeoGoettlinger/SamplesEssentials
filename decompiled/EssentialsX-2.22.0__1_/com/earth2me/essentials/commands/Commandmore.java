package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandmore extends EssentialsCommand {
   public Commandmore() {
      super("more");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (stack != null && stack.getType() != Material.AIR) {
         boolean canOversized = user.isAuthorized("essentials.oversizedstacks");
         if (stack.getAmount() >= (canOversized ? this.ess.getSettings().getOversizedStackSize() : stack.getMaxStackSize())) {
            throw new TranslatableException("fullStack");
         } else {
            String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
            if (!user.canSpawnItem(stack.getType())) {
               throw new TranslatableException("cantSpawnItem", itemname);
            } else {
               int newStackSize = stack.getAmount();
               if (args.length >= 1) {
                  if (!NumberUtil.isPositiveInt(args[0])) {
                     throw new TranslatableException("nonZeroPosNumber");
                  }

                  int cap = canOversized ? this.ess.getSettings().getOversizedStackSize() : stack.getMaxStackSize();
                  long newSizeLong = (long)stack.getAmount() + Integer.parseInt(args[0]);
                  if (newSizeLong > cap) {
                     user.sendTl(canOversized ? "fullStackDefaultOversize" : "fullStackDefault", cap);
                     newStackSize = cap;
                  } else {
                     newStackSize = (int)newSizeLong;
                  }
               } else if (canOversized) {
                  newStackSize = this.ess.getSettings().getOversizedStackSize();
               } else {
                  newStackSize = stack.getMaxStackSize();
               }

               stack.setAmount(newStackSize);
               user.getBase().updateInventory();
            }
         }
      } else {
         throw new TranslatableException("cantSpawnItem", "Air");
      }
   }
}
