package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public interface CurrencyHook {
   void setup();

   String getName();

   default Map<String, Object> getSettings() {
      return AxTrade.HOOKS.getSection("currencies." + this.getName()).getStringRouteMappedValues(true);
   }

   boolean worksOffline();

   boolean usesDouble();

   boolean isPersistent();

   double getBalance(@NotNull UUID player);

   CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount);

   CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount);
}
