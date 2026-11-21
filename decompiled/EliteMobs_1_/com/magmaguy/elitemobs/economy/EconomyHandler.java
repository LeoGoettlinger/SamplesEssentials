package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EconomyHandler {
   private static final Set<UUID> activeMutations = new HashSet<>();

   public static boolean isReady(UUID user) {
      return user != null && PlayerData.isDataLoaded(user);
   }

   private static void requireReady(UUID user) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Economy mutation off server thread");
      } else if (!isReady(user)) {
         throw new IllegalStateException("Player economy data is not loaded for " + user);
      }
   }

   public static void addCurrency(UUID user, double amount) {
      if (!tryCredit(user, amount)) {
         throw new IllegalStateException("Economy provider rejected credit for " + user);
      }
   }

   public static boolean tryCredit(UUID user, double amount) {
      requireReady(user);
      if (Double.isFinite(amount) && !(amount < 0.0)) {
         amount = Round.twoDecimalPlaces(amount);
         if (amount == 0.0) {
            return true;
         } else if (!activeMutations.add(user)) {
            return false;
         } else {
            boolean var17;
            try {
               double debt = PlayerData.getGamblingDebt(user);
               double collection = Math.min(debt, amount);
               double proceeds = Round.twoDecimalPlaces(amount - collection);
               double remainingDebt = Round.twoDecimalPlaces(debt - collection);
               if (VaultCompatibility.VAULT_ENABLED) {
                  if (proceeds > 0.0) {
                     Economy economy = VaultCompatibility.getEconomy();
                     if (economy == null || !economy.depositPlayer(Bukkit.getOfflinePlayer(user), proceeds).transactionSuccess()) {
                        return false;
                     }
                  }

                  if (collection > 0.0) {
                     PlayerData.setGamblingDebt(user, remainingDebt);
                  }
               } else {
                  PlayerData.setCurrencyAndGamblingDebt(user, Round.twoDecimalPlaces(checkCurrency(user) + proceeds), remainingDebt);
               }

               if (collection > 0.0) {
                  notifyDebtCollection(user, collection, remainingDebt);
               }

               var17 = true;
            } finally {
               activeMutations.remove(user);
            }

            return var17;
         }
      } else {
         throw new IllegalArgumentException("Invalid credit");
      }
   }

   private static void notifyDebtCollection(UUID user, double amount, double remainingDebt) {
      try {
         Player player = Bukkit.getPlayer(user);
         if (player == null || !player.isOnline()) {
            return;
         }

         player.sendMessage(
            ChatColorConverter.convert(
               remainingDebt > 0.0
                  ? GamblingConfig.getDebtAutoCollectedMessage()
                     .replace("$amount", String.format("%.0f", amount))
                     .replace("$remaining", String.format("%.0f", remainingDebt))
                  : GamblingConfig.getDebtAutoClearedMessage()
            )
         );
      } catch (RuntimeException var6) {
         Logger.warn("Could not display accepted debt collection for " + user + ": " + var6);
      }
   }

   public static boolean tryWithdraw(UUID user, double amount) {
      requireReady(user);
      if (!Double.isFinite(amount) || amount < 0.0) {
         throw new IllegalArgumentException("Invalid purchase price");
      } else if (!activeMutations.add(user)) {
         return false;
      } else {
         boolean var4;
         try {
            if (!VaultCompatibility.VAULT_ENABLED) {
               double balance = checkCurrency(user);
               if (!(balance < amount)) {
                  PlayerData.setCurrency(user, Round.twoDecimalPlaces(balance - amount));
                  return true;
               }

               return false;
            }

            Economy economy = VaultCompatibility.getEconomy();
            var4 = economy != null && economy.withdrawPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
         } finally {
            activeMutations.remove(user);
         }

         return var4;
      }
   }

   public static boolean refundPayment(UUID user, double amount) {
      requireReady(user);
      if (!Double.isFinite(amount) || amount < 0.0) {
         throw new IllegalArgumentException("Invalid refund");
      } else if (!activeMutations.add(user)) {
         return false;
      } else {
         boolean var4;
         try {
            if (!VaultCompatibility.VAULT_ENABLED) {
               PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) + amount));
               return true;
            }

            Economy economy = VaultCompatibility.getEconomy();
            var4 = economy != null && economy.depositPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
         } finally {
            activeMutations.remove(user);
         }

         return var4;
      }
   }

   public static void subtractCurrency(UUID user, double amount) {
      requireReady(user);
      if (activeMutations.contains(user)) {
         throw new IllegalStateException("Economy mutation already in progress");
      } else if (VaultCompatibility.VAULT_ENABLED) {
         if (!tryWithdraw(user, amount)) {
            throw new IllegalStateException("Economy provider rejected withdrawal for " + user);
         }
      } else {
         PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) - amount));
      }
   }

   public static void setCurrency(UUID user, double amount) {
      requireReady(user);
      if (activeMutations.contains(user)) {
         throw new IllegalStateException("Economy mutation already in progress");
      } else if (VaultCompatibility.VAULT_ENABLED) {
         VaultCompatibility.setCurrency(user, amount);
      } else {
         PlayerData.setCurrency(user, Round.twoDecimalPlaces(amount));
      }
   }

   public static double checkCurrency(UUID user) {
      return VaultCompatibility.VAULT_ENABLED ? VaultCompatibility.checkCurrency(user) : PlayerData.getCurrency(user);
   }

   public static double checkCurrency(UUID user, boolean databaseAccess) {
      return VaultCompatibility.VAULT_ENABLED ? VaultCompatibility.checkCurrency(user) : PlayerData.getCurrency(user, databaseAccess);
   }

   public static String formatCurrency(double amount) {
      return String.format("%.2f", amount);
   }
}
