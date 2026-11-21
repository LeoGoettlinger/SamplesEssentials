package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandsell extends EssentialsCommand {
   public Commandsell() {
      super("sell");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      BigDecimal totalWorth = BigDecimal.ZERO;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else if (args[0].equalsIgnoreCase("hand") && !user.isAuthorized("essentials.sell.hand")) {
         throw new TranslatableException("sellHandPermission");
      } else if ((args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("invent") || args[0].equalsIgnoreCase("all"))
         && !user.isAuthorized("essentials.sell.bulk")) {
         throw new TranslatableException("sellBulkPermission");
      } else {
         List<ItemStack> is = this.ess.getItemDb().getMatching(user, args);
         int count = 0;
         boolean isBulk = is.size() > 1;
         List<ItemStack> notSold = new ArrayList<>();

         for (ItemStack stack : is) {
            if (!this.ess.getSettings().isAllowSellNamedItems() && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()) {
               if (!isBulk) {
                  throw new TranslatableException("cannotSellNamedItem");
               }

               notSold.add(stack);
            } else {
               try {
                  if (stack.getAmount() > 0) {
                     totalWorth = totalWorth.add(this.sellItem(user, stack, args, isBulk));
                     stack = stack.clone();
                     count++;

                     for (ItemStack zeroStack : is) {
                        if (zeroStack.isSimilar(stack)) {
                           zeroStack.setAmount(0);
                        }
                     }
                  }
               } catch (Exception var14) {
                  if (!isBulk) {
                     throw var14;
                  }
               }
            }
         }

         if (!notSold.isEmpty()) {
            List<String> names = new ArrayList<>();

            for (ItemStack stackx : notSold) {
               if (stackx.getItemMeta() != null) {
                  names.add(stackx.getItemMeta().getDisplayName());
               }
            }

            this.ess
               .showError(user.getSource(), new TranslatableException("cannotSellTheseNamedItems", String.join(ChatColor.RESET + ", ", names)), commandLabel);
         }

         if (count != 1) {
            AdventureUtil.ParsedPlaceholder totalWorthStr = AdventureUtil.parsed(NumberUtil.displayCurrency(totalWorth, this.ess));
            if (args[0].equalsIgnoreCase("blocks")) {
               user.sendTl("totalWorthBlocks", totalWorthStr, totalWorthStr);
            } else {
               user.sendTl("totalWorthAll", totalWorthStr, totalWorthStr);
            }
         }
      }
   }

   private BigDecimal sellItem(final User user, final ItemStack is, final String[] args, final boolean isBulkSell) throws Exception {
      int amount = this.ess.getWorth().getAmount(this.ess, user, is, args, isBulkSell);
      BigDecimal originalWorth = this.ess.getWorth().getPrice(this.ess, is);
      BigDecimal worth = originalWorth == null ? null : originalWorth.multiply(this.ess.getSettings().getMultiplier(user));
      if (worth == null) {
         throw new TranslatableException("itemCannotBeSold");
      } else if (amount <= 0) {
         if (!isBulkSell) {
            user.sendTl(
               "itemSold",
               AdventureUtil.parsed(NumberUtil.displayCurrency(BigDecimal.ZERO, this.ess)),
               BigDecimal.ZERO,
               is.getType().toString().toLowerCase(Locale.ENGLISH),
               NumberUtil.displayCurrency(worth, this.ess)
            );
         }

         return BigDecimal.ZERO;
      } else {
         BigDecimal result = worth.multiply(BigDecimal.valueOf((long)amount));
         ItemStack ris = is.clone();
         ris.setAmount(amount);
         if (!Inventories.containsAtLeast(user.getBase(), ris, amount)) {
            throw new IllegalStateException("Trying to remove more items than are available.");
         } else {
            Inventories.removeItemAmount(user.getBase(), ris, ris.getAmount());
            user.getBase().updateInventory();
            Trade.log(
               "Command",
               "Sell",
               "Item",
               user.getName(),
               new Trade(ris, this.ess),
               user.getName(),
               new Trade(result, this.ess),
               user.getLocation(),
               user.getMoney(),
               this.ess
            );
            user.giveMoney(result, null, UserBalanceUpdateEvent.Cause.COMMAND_SELL);
            String typeName = is.getType().toString().toLowerCase(Locale.ENGLISH);
            AdventureUtil.ParsedPlaceholder worthDisplay = AdventureUtil.parsed(NumberUtil.displayCurrency(worth, this.ess));
            user.sendTl("itemSold", AdventureUtil.parsed(NumberUtil.displayCurrency(result, this.ess)), amount, typeName, worthDisplay);
            this.ess
               .getLogger()
               .log(
                  Level.INFO,
                  this.ess
                     .getAdventureFacet()
                     .miniToLegacy(
                        I18n.tlLiteral(
                           "itemSoldConsole",
                           user.getName(),
                           typeName,
                           this.ess.getAdventureFacet().miniToLegacy(NumberUtil.displayCurrency(result, this.ess)),
                           amount,
                           this.ess.getAdventureFacet().miniToLegacy(worthDisplay.toString()),
                           user.getDisplayName()
                        )
                     )
               );
            return result;
         }
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
}
