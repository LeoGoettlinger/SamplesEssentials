package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

public class CoinsEngineHook implements CurrencyHook {
   private Currency currency = null;
   private final Map<String, Object> settings;
   private final String internal;

   public CoinsEngineHook(Map<Object, Object> settings) {
      Map<String, Object> map = new HashMap<>();

      for (Entry<Object, Object> entry : settings.entrySet()) {
         map.put((String)entry.getKey(), entry.getValue());
      }

      this.settings = map;
      this.internal = (String)settings.get("currency-name");
   }

   @Override
   public void setup() {
      this.currency = CoinsEngineAPI.getCurrency(this.internal);
      if (this.currency == null) {
         Bukkit.getConsoleSender()
            .sendMessage(
               StringUtils.formatToString(
                  "&#FF0000[AxTrade] CoinsEngine currency named &#DD0000"
                     + this.internal
                     + " &#FF0000not found! Change the currency-name or disable the hook to get rid of this warning!"
               )
            );
      }
   }

   @Override
   public String getName() {
      return "CoinsEngine-" + this.internal;
   }

   @Override
   public Map<String, Object> getSettings() {
      return this.settings;
   }

   @Override
   public boolean worksOffline() {
      return false;
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
      return this.currency == null ? 0.0 : CoinsEngineAPI.getBalance(Bukkit.getPlayer(player), this.currency);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CoinsEngineAPI.addBalance(Bukkit.getPlayer(player), this.currency, amount);
         return CompletableFuture.completedFuture(true);
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CoinsEngineAPI.removeBalance(Bukkit.getPlayer(player), this.currency, amount);
         return CompletableFuture.completedFuture(true);
      }
   }
}
