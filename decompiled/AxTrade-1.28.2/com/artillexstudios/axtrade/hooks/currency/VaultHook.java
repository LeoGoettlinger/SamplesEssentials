package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultHook implements CurrencyHook {
   private Economy econ = null;

   @Override
   public void setup() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
         this.econ = (Economy)rsp.getProvider();
      }
   }

   @Override
   public String getName() {
      return "Vault";
   }

   @Override
   public Map<String, Object> getSettings() {
      return AxTrade.HOOKS.getSection("currencies." + this.getName()).getStringRouteMappedValues(true);
   }

   @Override
   public boolean worksOffline() {
      return true;
   }

   @Override
   public boolean usesDouble() {
      return true;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return this.econ.getBalance(Bukkit.getOfflinePlayer(player));
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      EconomyResponse response = this.econ.depositPlayer(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(response.transactionSuccess());
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      EconomyResponse response = this.econ.withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(response.transactionSuccess());
   }
}
