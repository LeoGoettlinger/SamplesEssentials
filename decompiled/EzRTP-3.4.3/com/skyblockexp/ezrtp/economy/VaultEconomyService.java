package com.skyblockexp.ezrtp.economy;

import java.util.Locale;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public final class VaultEconomyService implements EconomyService {
   private final Economy economy;
   private final Logger logger;

   public VaultEconomyService(Economy economy, Logger logger) {
      this.economy = economy;
      this.logger = logger;
   }

   @Override
   public boolean isEnabled() {
      return this.economy != null;
   }

   @Override
   public boolean hasBalance(Player player, double amount) {
      return this.economy != null && !(amount <= 0.0) ? this.economy.has(player, amount) : true;
   }

   @Override
   public boolean withdraw(Player player, double amount) {
      if (this.economy != null && !(amount <= 0.0)) {
         EconomyResponse response = this.economy.withdrawPlayer(player, amount);
         if (response.transactionSuccess()) {
            return true;
         } else {
            if (this.logger != null) {
               this.logger
                  .warning(
                     String.format(
                        Locale.US,
                        "Failed to withdraw %.2f from %s: %s",
                        amount,
                        player.getName(),
                        response.errorMessage == null ? "unknown error" : response.errorMessage
                     )
                  );
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public String getProviderName() {
      return this.economy != null ? this.economy.getName() : "Unknown";
   }
}
