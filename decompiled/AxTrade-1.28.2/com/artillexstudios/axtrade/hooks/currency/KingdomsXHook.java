package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.player.KingdomPlayer;

public class KingdomsXHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "KingdomsX";
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
      KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
      return kingdomPlayer.getKingdom() == null ? 0.0 : kingdomPlayer.getKingdom().getResourcePoints();
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
      kingdomPlayer.getKingdom().addResourcePoints((long)amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
      kingdomPlayer.getKingdom().addResourcePoints((long)(amount * -1.0));
      return CompletableFuture.completedFuture(true);
   }
}
