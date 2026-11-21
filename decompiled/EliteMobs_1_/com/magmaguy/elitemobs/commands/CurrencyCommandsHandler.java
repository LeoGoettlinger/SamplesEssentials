package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.magmacore.util.Round;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CurrencyCommandsHandler {
   public static void payCommand(Player sender, String recipientName, double amount) {
      if (amount <= 0.0) {
         sender.sendMessage(CommandMessagesConfig.getPayNiceTryMessage());
      } else {
         Player recipient = Bukkit.getPlayer(recipientName);
         if (recipient == null) {
            sender.sendMessage(CommandMessagesConfig.getPayPlayerNotOnlineMessage().replace("$player", recipientName));
         } else if (ready(sender, sender) && ready(sender, recipient)) {
            double senderCurrency = EconomyHandler.checkCurrency(sender.getUniqueId());
            if (senderCurrency - amount < 0.0) {
               sender.sendMessage(
                  EconomySettingsConfig.getEconomyPaymentInsufficientCurrency().replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
            } else {
               double actuallyReceivedAmount = Round.twoDecimalPlaces(amount - amount * EconomySettingsConfig.getPlayerToPlayerTaxes());
               double taxes = Round.twoDecimalPlaces(amount * EconomySettingsConfig.getPlayerToPlayerTaxes());
               EconomyHandler.addCurrency(recipient.getUniqueId(), actuallyReceivedAmount);
               EconomyHandler.subtractCurrency(sender.getUniqueId(), amount);
               sender.sendMessage(
                  EconomySettingsConfig.getEconomyPayMessage()
                     .replace("$amount_sent", EconomyHandler.formatCurrency(amount))
                     .replace("$amount_received", EconomyHandler.formatCurrency(actuallyReceivedAmount))
                     .replace("$taxes", EconomyHandler.formatCurrency(taxes))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                     .replace("$receiver", recipient.getDisplayName())
               );
               sender.sendMessage(
                  EconomySettingsConfig.getEconomyCurrencyLeftMessage()
                     .replace("$amount_left", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(sender.getUniqueId())))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
               recipient.sendMessage(
                  EconomySettingsConfig.getEconomyPaymentReceivedMessage()
                     .replace("$amount_sent", EconomyHandler.formatCurrency(amount))
                     .replace("$amount_received", EconomyHandler.formatCurrency(actuallyReceivedAmount))
                     .replace("$taxes", EconomyHandler.formatCurrency(taxes))
                     .replace("$sender", sender.getDisplayName())
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
               recipient.sendMessage(
                  EconomySettingsConfig.getEconomyCurrencyLeftMessage()
                     .replace("$amount_left", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(recipient.getUniqueId())))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
            }
         }
      }
   }

   public static void addCommand(Player player, double amount) {
      EconomyHandler.addCurrency(player.getUniqueId(), amount);
   }

   public static void addCommand(CommandSender commandSender, String onlinePlayer, double amount) {
      Player player = Bukkit.getPlayer(onlinePlayer);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
      } else if (ready(commandSender, player)) {
         addCommand(player, amount);
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencyAddedMessage().replace("$amount", EconomyHandler.formatCurrency(amount)).replace("$player", onlinePlayer)
         );
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencyNowHasMessage()
               .replace("$amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
         );
      }
   }

   public static void addAllCommand(CommandSender commandSender, double amount) {
      List<? extends Player> players = List.copyOf(Bukkit.getOnlinePlayers());

      for (Player player : players) {
         if (!ready(commandSender, player)) {
            return;
         }
      }

      for (Player playerx : players) {
         addCommand(playerx, amount);
      }

      commandSender.sendMessage(CommandMessagesConfig.getCurrencyAddedAllMessage().replace("$amount", EconomyHandler.formatCurrency(amount)));
   }

   public static void subtractCommand(String playerName, double amount) {
      EconomyHandler.subtractCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);
   }

   public static void subtractCommand(CommandSender commandSender, String onlinePlayer, int amount) {
      Player player = Bukkit.getPlayer(onlinePlayer);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
      } else if (ready(commandSender, player)) {
         subtractCommand(onlinePlayer, amount);
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencySubtractedMessage().replace("$amount", EconomyHandler.formatCurrency(amount)).replace("$player", onlinePlayer)
         );
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencyNowHasMessage()
               .replace("$amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
         );
      }
   }

   public static void setCommand(CommandSender commandSender, String onlinePlayer, double amount) {
      Player player = Bukkit.getPlayer(onlinePlayer);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
      } else if (ready(commandSender, player)) {
         EconomyHandler.setCurrency(player.getUniqueId(), amount);
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencySetMessage()
               .replace("$player", onlinePlayer)
               .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
               .replace("$amount", EconomyHandler.formatCurrency(amount))
         );
      }
   }

   public static void checkCommand(CommandSender commandSender, String playerName) {
      Player player = Bukkit.getPlayer(playerName);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", playerName));
      } else if (ready(commandSender, player)) {
         double money = EconomyHandler.checkCurrency(player.getUniqueId());
         commandSender.sendMessage(
            CommandMessagesConfig.getCurrencyCheckMessage()
               .replace("$player", playerName)
               .replace("$amount", EconomyHandler.formatCurrency(money))
               .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
         );
      }
   }

   public static void walletCommand(Player player) {
      if (ready(player, player)) {
         player.sendMessage(
            EconomySettingsConfig.getEconomyWalletCommand()
               .replace("$balance", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
               .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
         );
      }
   }

   private static boolean ready(CommandSender sender, Player target) {
      if (EconomyHandler.isReady(target.getUniqueId())) {
         return true;
      } else {
         sender.sendMessage(CommandMessagesConfig.getCurrencyDataNotReadyMessage().replace("$player", target.getName()));
         return false;
      }
   }
}
