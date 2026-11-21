package com.earth2me.essentials.commands;

import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commanditem extends EssentialsCommand {
   public Commanditem() {
      super("item");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[0]);
         String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (!user.canSpawnItem(stack.getType())) {
            throw new TranslatableException("cantSpawnItem", itemname);
         } else {
            try {
               if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
                  stack.setAmount(Integer.parseInt(args[1]));
               } else if (this.ess.getSettings().getDefaultStackSize() > 0) {
                  stack.setAmount(this.ess.getSettings().getDefaultStackSize());
               } else if (this.ess.getSettings().getOversizedStackSize() > 0 && user.isAuthorized("essentials.oversizedstacks")) {
                  stack.setAmount(this.ess.getSettings().getOversizedStackSize());
               }
            } catch (NumberFormatException var9) {
               throw new NotEnoughArgumentsException();
            }

            MetaItemStack metaStack = new MetaItemStack(stack);
            if (!metaStack.canSpawn(this.ess)) {
               throw new TranslatableException("unableToSpawnItem", itemname);
            } else {
               if (args.length > 2) {
                  boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments() && user.isAuthorized("essentials.enchantments.allowunsafe");
                  metaStack.parseStringMeta(user.getSource(), allowUnsafe, args, 2, this.ess);
                  stack = metaStack.getItemStack();
               }

               if (stack.getType() == Material.AIR) {
                  throw new TranslatableException("cantSpawnItem", "Air");
               } else {
                  String displayName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
                  user.sendTl("itemSpawn", stack.getAmount(), displayName);
                  Inventories.addItem(
                     user.getBase(), user.isAuthorized("essentials.oversizedstacks") ? this.ess.getSettings().getOversizedStackSize() : 0, stack
                  );
                  user.getBase().updateInventory();
               }
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getItems();
      } else if (args.length == 2) {
         return Lists.newArrayList(new String[]{"1", "64"});
      } else {
         return (List<String>)(args.length == 3 ? Lists.newArrayList(new String[]{"0"}) : Collections.emptyList());
      }
   }
}
