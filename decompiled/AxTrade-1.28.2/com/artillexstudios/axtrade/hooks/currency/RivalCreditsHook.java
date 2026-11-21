package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.rivaldev.credits.CreditAPI;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class RivalCreditsHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "RivalCredits";
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
      return CreditAPI.getInstance().getCredits(Bukkit.getOfflinePlayer(player));
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      CreditAPI.getInstance().addCredits(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      CreditAPI.getInstance().removeCredits(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(true);
   }
}
