package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.ess3.provider.PlayerLocaleProvider;
import org.bukkit.Server;

public class Commandpay extends EssentialsLoopCommand {
   public Commandpay() {
      super("pay");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         String ogStr = args[1];
         if (ogStr.contains("-")) {
            throw new TranslatableException("payMustBePositive");
         } else {
            String sanitizedString = ogStr.replaceAll("[^0-9.]", "");
            if (sanitizedString.isEmpty()) {
               throw new NotEnoughArgumentsException();
            } else {
               BigDecimal amount;
               if (this.ess.getSettings().isPerPlayerLocale()) {
                  String playerLocale = this.ess.provider(PlayerLocaleProvider.class).getLocale(user.getBase());
                  amount = NumberUtil.parseStringToBDecimal(ogStr, user.getPlayerLocale(playerLocale));
               } else {
                  amount = NumberUtil.parseStringToBDecimal(ogStr);
               }

               if (amount.compareTo(this.ess.getSettings().getMinimumPayAmount()) < 0) {
                  throw new TranslatableException(
                     "minimumPayAmount", AdventureUtil.parsed(NumberUtil.displayCurrencyExactly(this.ess.getSettings().getMinimumPayAmount(), this.ess))
                  );
               } else {
                  AtomicBoolean informToConfirm = new AtomicBoolean(false);
                  boolean canPayOffline = user.isAuthorized("essentials.pay.offline");
                  if (!canPayOffline && args[0].equals("**")) {
                     user.sendTl("payOffline");
                  } else {
                     this.loopOfflinePlayersConsumer(
                        server,
                        user.getSource(),
                        false,
                        user.isAuthorized("essentials.pay.multiple"),
                        args[0],
                        player -> {
                           try {
                              if (player.getBase() != null && (!player.getBase().isOnline() || player.isHidden(user.getBase())) && !canPayOffline) {
                                 user.sendTl("payOffline");
                                 return;
                              }

                              if (!player.isAcceptingPay() || this.ess.getSettings().isPayExcludesIgnoreList() && player.isIgnoredPlayer(user)) {
                                 user.sendTl("notAcceptingPay", player.getDisplayName());
                                 return;
                              }

                              if (user.isPromptingPayConfirm() && !amount.equals(user.getConfirmingPayments().get(player))) {
                                 if (!informToConfirm.get()) {
                                    user.getConfirmingPayments().clear();
                                    informToConfirm.set(true);
                                 }

                                 user.getConfirmingPayments().put(player, amount);
                                 return;
                              }

                              user.payUser(player, amount, UserBalanceUpdateEvent.Cause.COMMAND_PAY);
                              user.getConfirmingPayments().remove(player);
                              Trade.log(
                                 "Command",
                                 "Pay",
                                 "Player",
                                 user.getName(),
                                 new Trade(amount, this.ess),
                                 player.getName(),
                                 new Trade(amount, this.ess),
                                 user.getLocation(),
                                 user.getMoney(),
                                 this.ess
                              );
                           } catch (MaxMoneyException var9x) {
                              user.sendTl("maxMoney");

                              try {
                                 user.setMoney(user.getMoney().add(amount));
                              } catch (MaxMoneyException var8x) {
                              }
                           } catch (TranslatableException var10x) {
                              throw var10x;
                           } catch (Exception var11x) {
                              throw new TranslatableException("errorWithMessage", var11x.getMessage());
                           }
                        }
                     );
                     if (informToConfirm.get()) {
                        String cmd = "/" + commandLabel + " " + StringUtil.joinList(" ", args);
                        user.sendTl("confirmPayment", AdventureUtil.parsed(NumberUtil.displayCurrency(amount, this.ess)), cmd);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User player, final String[] args) {
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else {
         return (List<String>)(args.length == 2
            ? Lists.newArrayList(new String[]{this.ess.getSettings().getMinimumPayAmount().toString()})
            : Collections.emptyList());
      }
   }
}
