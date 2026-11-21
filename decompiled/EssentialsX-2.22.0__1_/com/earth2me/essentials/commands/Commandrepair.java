package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.IUser;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandrepair extends EssentialsCommand {
   public Commandrepair() {
      super("repair");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 0 && !args[0].equalsIgnoreCase("hand") && user.isAuthorized("essentials.repair.all")) {
         if (!args[0].equalsIgnoreCase("all")) {
            throw new NotEnoughArgumentsException();
         }

         Trade charge = new Trade("repair-all", this.ess);
         charge.isAffordableFor(user);
         this.repairAll(user);
         charge.charge(user);
      } else {
         this.repairHand(user);
      }
   }

   public void repairHand(final User user) throws Exception {
      ItemStack item = user.getItemInHand();
      if (item == null || item.getType().isBlock() || MaterialUtil.getDamage(item) == 0) {
         throw new TranslatableException("repairInvalidType");
      } else if (!item.getEnchantments().isEmpty() && !this.ess.getSettings().getRepairEnchanted() && !user.isAuthorized("essentials.repair.enchanted")) {
         throw new TranslatableException("repairEnchanted");
      } else {
         String itemName = item.getType().toString().toLowerCase(Locale.ENGLISH);
         Trade charge = this.getCharge(item.getType());
         charge.isAffordableFor(user);
         this.repairItem(item);
         charge.charge(user);
         user.getBase().updateInventory();
         user.sendTl("repair", itemName.replace('_', ' '));
      }
   }

   public void repairAll(final User user) throws Exception {
      List<String> repaired = new ArrayList<>();
      this.repairItems(Inventories.getInventory(user.getBase(), false), user, repaired);
      if (user.isAuthorized("essentials.repair.armor")) {
         this.repairItems(user.getBase().getInventory().getArmorContents(), user, repaired);
      }

      user.getBase().updateInventory();
      if (repaired.isEmpty()) {
         throw new TranslatableException("repairNone");
      } else {
         user.sendTl("repair", StringUtil.joinList(repaired));
      }
   }

   private void repairItem(final ItemStack item) throws Exception {
      Material material = item.getType();
      if (material.isBlock() || material.getMaxDurability() < 1) {
         throw new TranslatableException("repairInvalidType");
      } else if (MaterialUtil.getDamage(item) == 0) {
         throw new TranslatableException("repairAlreadyFixed");
      } else {
         MaterialUtil.setDamage(item, 0);
      }
   }

   private void repairItems(final ItemStack[] items, final IUser user, final List<String> repaired) {
      for (ItemStack item : items) {
         if (item != null && !item.getType().isBlock() && MaterialUtil.getDamage(item) != 0) {
            String itemName = item.getType().toString().toLowerCase(Locale.ENGLISH);
            Trade charge = this.getCharge(item.getType());

            try {
               charge.isAffordableFor(user);
            } catch (ChargeException var13) {
               user.sendMessage(var13.getMessage());
               continue;
            }

            if (item.getEnchantments().isEmpty() || this.ess.getSettings().getRepairEnchanted() || user.isAuthorized("essentials.repair.enchanted")) {
               try {
                  this.repairItem(item);
               } catch (Exception var12) {
                  continue;
               }

               try {
                  charge.charge(user);
               } catch (ChargeException var11) {
                  user.sendMessage(var11.getMessage());
               }

               repaired.add(itemName.replace('_', ' '));
            }
         }
      }
   }

   private Trade getCharge(final Material material) {
      String itemName = material.toString().toLowerCase(Locale.ENGLISH);
      if (VersionUtil.PRE_FLATTENING) {
         int itemId = material.getId();
         return new Trade("repair-" + itemName.replace('_', '-'), new Trade("repair-" + itemId, new Trade("repair-item", this.ess), this.ess), this.ess);
      } else {
         return new Trade("repair-" + itemName.replace('_', '-'), new Trade("repair-item", this.ess), this.ess);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList(new String[]{"hand"});
         if (user.isAuthorized("essentials.repair.all")) {
            options.add("all");
         }

         return options;
      } else {
         return Collections.emptyList();
      }
   }
}
