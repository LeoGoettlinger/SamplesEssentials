package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationEngine;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class GamblingEconomyHandler {
   public static final double MAX_DEBT = 500.0;
   private static volatile double houseEarnings = 0.0;
   private static final Set<UUID> unresolvedGames = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Double> pendingPayouts = new HashMap<>();
   private static final Set<UUID> uncertainTransactions = new HashSet<>();
   private static final Set<UUID> transactionsInProgress = new HashSet<>();
   private static final Object houseStateLock = new Object();
   private static final Object houseWriteLock = new Object();
   private static long houseRevision;
   private static long savedHouseRevision;
   private static volatile long saveGeneration;
   private static BukkitTask saveTask;
   private static File houseDataFile;
   private static FileConfiguration houseDataConfig;

   public static void initialize() {
      if (saveTask != null) {
         saveTask.cancel();
      }

      saveTask = null;
      synchronized (houseWriteLock) {
         saveGeneration++;
         if (GamblingConfig.isGamblingEnabled()) {
            houseDataFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "house_earnings.yml");
            YamlConfiguration loaded = new YamlConfiguration();

            try {
               loaded.load(houseDataFile);
            } catch (FileNotFoundException var5) {
            } catch (InvalidConfigurationException | IOException var6) {
               houseDataConfig = null;
               Logger.warn("Cannot load house_earnings.yml; preserving the file and unsaved totals: " + var6);
               return;
            }

            houseDataConfig = loaded;
            synchronized (houseStateLock) {
               if (houseRevision == savedHouseRevision) {
                  houseEarnings = loaded.getDouble("houseEarnings", 0.0);
               }
            }

            long generation = saveGeneration;
            saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, () -> saveHouseEarnings(generation), 100L, 100L);
         }
      }
   }

   public static void saveHouseEarnings() {
      saveHouseEarnings(saveGeneration);
   }

   private static void saveHouseEarnings(long generation) {
      synchronized (houseWriteLock) {
         if (generation == saveGeneration && houseDataConfig != null) {
            double value;
            long revision;
            synchronized (houseStateLock) {
               if (houseRevision == savedHouseRevision) {
                  return;
               }

               value = houseEarnings;
               revision = houseRevision;
            }

            houseDataConfig.set("houseEarnings", value);

            try {
               ConfigurationEngine.fileSaverSerialized(houseDataConfig.saveToString(), houseDataFile);
               synchronized (houseStateLock) {
                  savedHouseRevision = revision;
               }
            } catch (RuntimeException var12) {
               Logger.warn("Could not save house_earnings.yml; total retained for retry: " + value + "; " + var12);
            }
         }
      }
   }

   public static void recordHouseWin(double amount) {
      if (isPositiveAmount(amount)) {
         synchronized (houseStateLock) {
            houseEarnings += amount;
            houseRevision++;
         }
      }
   }

   public static void recordHouseLoss(double amount) {
      if (isPositiveAmount(amount)) {
         synchronized (houseStateLock) {
            houseEarnings -= amount;
            houseRevision++;
         }
      }
   }

   public static String getFormattedHouseEarnings() {
      return houseEarnings >= 0.0 ? "&a+" + String.format("%.2f", houseEarnings) : "&c" + String.format("%.2f", houseEarnings);
   }

   public static void shutdown() {
      if (saveTask != null) {
         saveTask.cancel();
      }

      saveTask = null;
      synchronized (houseWriteLock) {
         saveGeneration++;
         saveHouseEarnings(saveGeneration);
      }

      if (!pendingPayouts.isEmpty() || !uncertainTransactions.isEmpty()) {
         Logger.warn(
            "Unsettled gambling payments retained in memory: payouts="
               + pendingPayouts
               + "; uncertain players="
               + uncertainTransactions
               + ". Reconcile before restarting the server."
         );
      }
   }

   public static boolean canAffordBet(UUID uuid, double betAmount) {
      if (GamblingConfig.isGamblingEnabled() && EconomyHandler.isReady(uuid)) {
         betAmount = Round.twoDecimalPlaces(betAmount);
         if (!isPositiveAmount(betAmount)) {
            return false;
         } else {
            double balance = EconomyHandler.checkCurrency(uuid);
            double currentDebt = PlayerData.getGamblingDebt(uuid);
            double availableCredit = 500.0 - currentDebt;
            double spendingPower = balance + availableCredit;
            return Double.isFinite(spendingPower) && spendingPower >= betAmount;
         }
      } else {
         return false;
      }
   }

   public static double getMaxBet(UUID uuid) {
      if (GamblingConfig.isGamblingEnabled() && uuid != null) {
         double balance = EconomyHandler.checkCurrency(uuid);
         double currentDebt = PlayerData.getGamblingDebt(uuid);
         double availableCredit = 500.0 - currentDebt;
         double maxBet = balance + availableCredit;
         return Double.isFinite(maxBet) ? Math.max(0.0, maxBet) : 0.0;
      } else {
         return 0.0;
      }
   }

   public static double getAvailableCredit(UUID uuid) {
      if (GamblingConfig.isGamblingEnabled() && uuid != null) {
         double currentDebt = PlayerData.getGamblingDebt(uuid);
         double availableCredit = 500.0 - currentDebt;
         return Double.isFinite(availableCredit) ? Math.max(0.0, availableCredit) : 0.0;
      } else {
         return 0.0;
      }
   }

   public static boolean placeBet(UUID uuid, double betAmount) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Gambling mutation off server thread");
      } else if (!GamblingConfig.isGamblingEnabled() || !EconomyHandler.isReady(uuid)) {
         return false;
      } else if (uncertainTransactions.contains(uuid)) {
         transactionFailure(uuid, "An earlier gambling provider result needs manual reconciliation");
         return false;
      } else {
         if (pendingPayouts.containsKey(uuid)) {
            try {
               resolveOutcome(uuid, pendingPayouts.get(uuid));
            } catch (IllegalStateException var17) {
               return false;
            }
         }

         betAmount = Round.twoDecimalPlaces(betAmount);
         if (isPositiveAmount(betAmount) && transactionsInProgress.add(uuid)) {
            try {
               double balance = EconomyHandler.checkCurrency(uuid);
               double debt = PlayerData.getGamblingDebt(uuid);
               if (!Double.isFinite(balance) || !Double.isFinite(debt) || balance + 500.0 - debt < betAmount) {
                  return false;
               } else {
                  double fromBalance = Math.min(Math.max(0.0, balance), betAmount);
                  double fromDebt = Round.twoDecimalPlaces(betAmount - fromBalance);

                  try {
                     if (VaultCompatibility.VAULT_ENABLED) {
                        if (fromBalance > 0.0 && !EconomyHandler.tryWithdraw(uuid, fromBalance)) {
                           return false;
                        }

                        if (fromDebt > 0.0) {
                           PlayerData.setGamblingDebt(uuid, debt + fromDebt);
                        }
                     } else {
                        PlayerData.setCurrencyAndGamblingDebt(uuid, balance - fromBalance, debt + fromDebt);
                     }
                  } catch (RuntimeException var18) {
                     uncertainTransactions.add(uuid);
                     transactionFailure(
                        uuid, "Unconfirmed gambling stake " + betAmount + "; balance debit=" + fromBalance + "; debt addition=" + fromDebt + "; " + var18
                     );
                     return false;
                  }

                  unresolvedGames.add(uuid);
                  recordHouseWin(betAmount);
                  return true;
               }
            } finally {
               transactionsInProgress.remove(uuid);
            }
         } else {
            return false;
         }
      }
   }

   public static double resolveOutcome(UUID uuid, double payoutAmount) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Gambling mutation off server thread");
      } else if (uuid == null || !unresolvedGames.contains(uuid)) {
         return 0.0;
      } else if (uncertainTransactions.contains(uuid)) {
         throw new IllegalStateException("An earlier gambling provider result needs manual reconciliation for " + uuid);
      } else if (!Double.isFinite(payoutAmount) || payoutAmount < 0.0) {
         throw new IllegalArgumentException("Invalid gambling payout");
      } else if (!transactionsInProgress.add(uuid)) {
         throw new IllegalStateException("Gambling transaction already in progress");
      } else {
         double var12;
         try {
            double owed = pendingPayouts.computeIfAbsent(uuid, ignored -> Round.twoDecimalPlaces(payoutAmount));
            if (owed > 0.0 && !EconomyHandler.isReady(uuid)) {
               throw new IllegalStateException("Player data unavailable; gambling payout retained for " + uuid);
            }

            if (owed > 0.0) {
               try {
                  accepted = EconomyHandler.tryCredit(uuid, owed);
               } catch (RuntimeException var10) {
                  uncertainTransactions.add(uuid);
                  transactionFailure(uuid, "Unconfirmed gambling payout " + owed + "; " + var10);
                  throw var10;
               }

               if (!accepted) {
                  transactionFailure(uuid, "Provider rejected gambling payout " + owed + "; payout retained");
                  throw new IllegalStateException("Gambling payout rejected for " + uuid);
               }

               recordHouseLoss(owed);
            }

            pendingPayouts.remove(uuid);
            unresolvedGames.remove(uuid);
            var12 = owed;
         } finally {
            transactionsInProgress.remove(uuid);
         }

         return var12;
      }
   }

   public static void awardWinnings(UUID uuid, double amount) {
      if (uuid != null && isPositiveAmount(amount)) {
         amount = Round.twoDecimalPlaces(amount);
         if (!EconomyHandler.tryCredit(uuid, amount)) {
            throw new IllegalStateException("Provider rejected gambling payout for " + uuid);
         } else {
            recordHouseLoss(amount);
         }
      }
   }

   private static void transactionFailure(UUID uuid, String reason) {
      Logger.warn(reason + "; player=" + uuid);
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && player.isOnline()) {
         player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
      }
   }

   public static boolean isInDebt(UUID uuid) {
      return PlayerData.hasGamblingDebt(uuid);
   }

   public static double getDebt(UUID uuid) {
      return PlayerData.getGamblingDebt(uuid);
   }

   public static double getBalance(UUID uuid) {
      return EconomyHandler.checkCurrency(uuid);
   }

   public static String getFinancialStatus(UUID uuid) {
      double balance = getBalance(uuid);
      double debt = getDebt(uuid);
      return debt > 0.0 ? String.format("Balance: %.2f | Debt: %.2f", balance, debt) : String.format("Balance: %.2f", balance);
   }

   public static double calculatePayout(double betAmount, double multiplier) {
      if (isPositiveAmount(betAmount) && Double.isFinite(multiplier) && !(multiplier < 0.0)) {
         double payout = Round.twoDecimalPlaces(betAmount * multiplier);
         return Double.isFinite(payout) && payout >= 0.0 ? payout : 0.0;
      } else {
         return 0.0;
      }
   }

   private static boolean isPositiveAmount(double amount) {
      return Double.isFinite(amount) && amount > 0.0;
   }

   @Generated
   public static double getHouseEarnings() {
      return houseEarnings;
   }
}
