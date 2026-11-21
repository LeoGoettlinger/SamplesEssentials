package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.ess3.provider.PlayerLocaleProvider;
import org.bukkit.Server;

public class Commandeco extends EssentialsLoopCommand {
   public Commandeco() {
      super("eco");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         Commandeco.EcoCommands cmd;
         boolean isPercent;
         BigDecimal amount;
         try {
            cmd = Commandeco.EcoCommands.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            isPercent = cmd != Commandeco.EcoCommands.RESET && args[2].endsWith("%");
            if (cmd == Commandeco.EcoCommands.RESET) {
               amount = this.ess.getSettings().getStartingBalance();
            } else if (sender.isPlayer() && this.ess.getSettings().isPerPlayerLocale()) {
               String playerLocale = this.ess.provider(PlayerLocaleProvider.class).getLocale(sender.getPlayer());
               amount = NumberUtil.parseStringToBDecimal(args[2], this.ess.getUser(sender.getPlayer()).getPlayerLocale(playerLocale));
            } else {
               amount = NumberUtil.parseStringToBDecimal(args[2]);
            }
         } catch (Exception var9) {
            throw new NotEnoughArgumentsException(var9);
         }

         this.loopOfflinePlayersConsumer(
            server,
            sender,
            false,
            true,
            args[1],
            player -> {
               BigDecimal userAmount = amount;
               if (isPercent) {
                  userAmount = player.getMoney().multiply(amount).scaleByPowerOfTen(-2);
               }

               switch (cmd) {
                  case GIVE:
                     player.giveMoney(userAmount, sender, UserBalanceUpdateEvent.Cause.COMMAND_ECO);
                     break;
                  case TAKE:
                     if (player.getMoney().subtract(userAmount).compareTo(this.ess.getSettings().getMinMoney()) >= 0) {
                        player.takeMoney(userAmount, sender, UserBalanceUpdateEvent.Cause.COMMAND_ECO);
                     } else {
                        this.ess
                           .showError(
                              sender,
                              new TranslatableException(
                                 "minimumBalanceError", AdventureUtil.parsed(NumberUtil.displayCurrency(this.ess.getSettings().getMinMoney(), this.ess))
                              ),
                              commandLabel
                           );
                     }
                     break;
                  case SET:
                  case RESET:
                     BigDecimal minBal = this.ess.getSettings().getMinMoney();
                     BigDecimal maxBal = this.ess.getSettings().getMaxMoney();
                     boolean underMin = userAmount.compareTo(minBal) < 0;
                     boolean aboveMax = userAmount.compareTo(maxBal) > 0;
                     player.setMoney(underMin ? minBal : (aboveMax ? maxBal : userAmount), UserBalanceUpdateEvent.Cause.COMMAND_ECO);
                     player.sendTl("setBal", AdventureUtil.parsed(NumberUtil.displayCurrency(player.getMoney(), this.ess)));
                     sender.sendTl("setBalOthers", player.getDisplayName(), AdventureUtil.parsed(NumberUtil.displayCurrency(player.getMoney(), this.ess)));
               }
            }
         );
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) throws NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException {
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         if (args.length == 2) {
            return this.getPlayers(sender);
         } else if (args.length != 3 || args[0].equalsIgnoreCase(Commandeco.EcoCommands.RESET.name())) {
            return Collections.emptyList();
         } else {
            return args[0].equalsIgnoreCase(Commandeco.EcoCommands.SET.name())
               ? Lists.newArrayList(new String[]{"0", this.ess.getSettings().getStartingBalance().toString()})
               : Lists.newArrayList(new String[]{"1", "10", "100", "1000"});
         }
      } else {
         List<String> options = Lists.newArrayList();

         for (Commandeco.EcoCommands command : Commandeco.EcoCommands.values()) {
            options.add(command.name().toLowerCase(Locale.ENGLISH));
         }

         return options;
      }
   }

   private static enum EcoCommands {
      GIVE,
      TAKE,
      SET,
      RESET;
   }
}
