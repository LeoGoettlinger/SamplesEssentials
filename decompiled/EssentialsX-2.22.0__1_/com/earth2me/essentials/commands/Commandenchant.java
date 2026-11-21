package com.earth2me.essentials.commands;

import com.earth2me.essentials.Enchantments;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class Commandenchant extends EssentialsCommand {
   public Commandenchant() {
      super("enchant");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (stack != null && stack.getType() != Material.AIR) {
         if (args.length == 0) {
            Set<String> usableEnchants = new TreeSet<>();

            for (Entry<String, Enchantment> entry : Enchantments.entrySet()) {
               String name = Enchantments.getRealName(entry.getValue());
               if (usableEnchants.contains(name) || user.isAuthorized("essentials.enchantments." + name) && entry.getValue().canEnchantItem(stack)) {
                  usableEnchants.add(entry.getKey());
               }
            }

            throw new NotEnoughArgumentsException(user.playerTl("enchantments", StringUtil.joinList(usableEnchants.toArray())));
         } else {
            int level = 1;
            if (args.length > 1) {
               try {
                  level = Integer.parseInt(args[1]);
               } catch (NumberFormatException var10) {
                  throw new NotEnoughArgumentsException();
               }
            }

            MetaItemStack metaStack = new MetaItemStack(stack);
            Enchantment enchantment = metaStack.getEnchantment(user, args[0]);
            metaStack.addEnchantment(
               user.getSource(),
               this.ess.getSettings().allowUnsafeEnchantments() && user.isAuthorized("essentials.enchantments.allowunsafe"),
               enchantment,
               level
            );
            stack.setItemMeta(metaStack.getItemStack().getItemMeta());
            user.getBase().updateInventory();
            String enchantName = Enchantments.getRealName(enchantment).replace('_', ' ');
            if (level == 0) {
               user.sendTl("enchantmentRemoved", enchantName);
            } else {
               user.sendTl("enchantmentApplied", enchantName);
            }
         }
      } else {
         throw new TranslatableException("nothingInHand");
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return new ArrayList<>(Enchantments.keySet());
      } else if (args.length != 2) {
         return Collections.emptyList();
      } else {
         Enchantment enchantment = Enchantments.getByName(args[0]);
         if (enchantment == null) {
            return Collections.emptyList();
         } else {
            int min = enchantment.getStartLevel();
            int max = enchantment.getMaxLevel();
            List<String> options = Lists.newArrayList();

            for (int i = min; i <= max; i++) {
               options.add(Integer.toString(i));
            }

            return options;
         }
      }
   }
}
