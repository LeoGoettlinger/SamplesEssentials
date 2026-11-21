package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class TokenManagerHook implements CurrencyHook {
   private TokenManager eco;

   @Override
   public void setup() {
      this.eco = (TokenManager)Bukkit.getPluginManager().getPlugin("TokenManager");
   }

   @Override
   public String getName() {
      return "TokenManager";
   }

   @Override
   public Map<String, Object> getSettings() {
      return AxTrade.HOOKS.getSection("currencies." + this.getName()).getStringRouteMappedValues(true);
   }

   @Override
   public boolean worksOffline() {
      return false;
   }

   @Override
   public boolean usesDouble() {
      return false;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return this.eco.getTokens(Bukkit.getPlayer(player)).orElse(0L);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      return CompletableFuture.completedFuture(this.eco.addTokens(Bukkit.getPlayer(player), (long)amount));
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      return CompletableFuture.completedFuture(this.eco.removeTokens(Bukkit.getPlayer(player), (long)amount));
   }
}
