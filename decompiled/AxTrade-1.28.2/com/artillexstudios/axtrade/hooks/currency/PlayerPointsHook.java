package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.jetbrains.annotations.NotNull;

public class PlayerPointsHook implements CurrencyHook {
   private PlayerPointsAPI econ = null;

   @Override
   public void setup() {
      this.econ = PlayerPoints.getInstance().getAPI();
   }

   @Override
   public String getName() {
      return "PlayerPoints";
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
      return false;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return this.econ.look(player);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      return CompletableFuture.completedFuture(this.econ.give(player, (int)amount));
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      return CompletableFuture.completedFuture(this.econ.take(player, (int)Math.round(amount)));
   }
}
