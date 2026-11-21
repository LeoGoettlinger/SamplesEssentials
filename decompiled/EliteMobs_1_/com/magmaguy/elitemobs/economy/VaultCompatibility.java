package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultCompatibility {
   public static boolean VAULT_ENABLED = false;
   private static Economy econ = null;

   public static void vaultSetup() {
      if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
         Logger.info("[(EliteMobs] Vault detected.");
         if (EconomySettingsConfig.isUseVault()) {
            Logger.warn("Vault preference detected. This is not the recommended setting. Ask the dev or check the wiki as to why.");
            VAULT_ENABLED = true;
            setupEconomy();
         }
      }
   }

   public static void setupEconomy() {
      if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp != null) {
            econ = (Economy)rsp.getProvider();
         }
      }
   }

   public static void addVaultCurrency(UUID user, double amount) {
      econ.depositPlayer(Bukkit.getOfflinePlayer(user), amount);
   }

   public static void subtractCurrency(UUID user, double amount) {
      econ.withdrawPlayer(Bukkit.getOfflinePlayer(user), amount);
   }

   public static void setCurrency(UUID user, double amount) {
      Logger.warn(
         "Someone just attempted to set the Vault currency through EliteMobs. For safety reasons, this command is off. Please use your dedicated economy plugin for this."
      );
   }

   public static double checkCurrency(UUID user) {
      double currency = 0.0;

      try {
         currency = econ.getBalance(Bukkit.getOfflinePlayer(user));
      } catch (NullPointerException var4) {
         Logger.warn(
            "Player tried to check currency when they had no economy entryassociated to them. This is an issue with your Vault/economy implementation, not EliteMobs."
         );
      }

      return currency;
   }

   public static Economy getEconomy() {
      return econ;
   }
}
