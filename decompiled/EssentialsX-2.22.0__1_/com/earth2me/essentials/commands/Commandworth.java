package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandworth extends EssentialsCommand {
   public Commandworth() {
      super("worth");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      List<ItemStack> is = this.ess.getItemDb().getMatching(user, args);
      int count = 0;
      boolean isBulk = is.size() > 1;
      BigDecimal totalWorth = BigDecimal.ZERO;

      for (ItemStack stack : is) {
         try {
            if (stack.getAmount() > 0) {
               totalWorth = totalWorth.add(this.itemWorth(user.getSource(), user, stack, args));
               stack = stack.clone();
               count++;

               for (ItemStack zeroStack : is) {
                  if (zeroStack.isSimilar(stack)) {
                     zeroStack.setAmount(0);
                  }
               }
            }
         } catch (Exception var13) {
            if (!isBulk) {
               throw var13;
            }
         }
      }

      if (count > 1) {
         AdventureUtil.ParsedPlaceholder totalWorthStr = AdventureUtil.parsed(NumberUtil.displayCurrency(totalWorth, this.ess));
         if (args.length > 0 && args[0].equalsIgnoreCase("blocks")) {
            user.sendTl("totalSellableBlocks", totalWorthStr, totalWorthStr);
            return;
         }

         user.sendTl("totalSellableAll", totalWorthStr, totalWorthStr);
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         this.itemWorth(sender, null, this.ess.getItemDb().get(args[0]), args);
      }
   }

   private BigDecimal itemWorth(final CommandSource sender, final User user, final ItemStack is, final String[] args) throws Exception {
      int amount = 1;
      if (user == null) {
         if (args.length > 1) {
            try {
               amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException var11) {
               throw new NotEnoughArgumentsException(var11);
            }
         }
      } else {
         amount = this.ess.getWorth().getAmount(this.ess, user, is, args, true);
      }

      BigDecimal worth = this.ess.getWorth().getPrice(this.ess, is);
      if (worth == null) {
         throw new TranslatableException("itemCannotBeSold");
      } else {
         if (amount < 0) {
            amount = 0;
         }

         BigDecimal result = worth.multiply(BigDecimal.valueOf((long)amount));
         String typeName = is.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         AdventureUtil.ParsedPlaceholder resultDisplay = AdventureUtil.parsed(NumberUtil.displayCurrency(result, this.ess));
         AdventureUtil.ParsedPlaceholder worthDisplay = AdventureUtil.parsed(NumberUtil.displayCurrency(worth, this.ess));
         if (MaterialUtil.getDamage(is) != 0) {
            sender.sendTl("worthMeta", typeName, MaterialUtil.getDamage(is), resultDisplay, amount, worthDisplay);
         } else {
            sender.sendTl("worth", typeName, resultDisplay, amount, worthDisplay);
         }

         return result;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getMatchingItems(args[0]);
      } else {
         return (List<String>)(args.length == 2 ? Lists.newArrayList(new String[]{"1", "64"}) : Collections.emptyList());
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getItems();
      } else {
         return (List<String>)(args.length == 2 ? Lists.newArrayList(new String[]{"1", "64"}) : Collections.emptyList());
      }
   }
}
