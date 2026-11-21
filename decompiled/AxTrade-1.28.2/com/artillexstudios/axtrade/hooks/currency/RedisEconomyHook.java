package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import dev.unnm3d.rediseconomy.currency.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class RedisEconomyHook implements CurrencyHook {
   private Currency currency = null;
   private final Map<String, Object> settings;
   private final String name;
   private final String internal;

   public RedisEconomyHook(Map<Object, Object> settings) {
      Map<String, Object> map = new HashMap<>();

      for (Entry<Object, Object> entry : settings.entrySet()) {
         map.put((String)entry.getKey(), entry.getValue());
      }

      this.settings = map;
      this.internal = (String)settings.get("currency-name");
      this.name = (String)settings.get("name");
   }

   @Override
   public void setup() {
      RedisEconomyAPI api = RedisEconomyAPI.getAPI();
      this.currency = api.getCurrencyByName(this.internal);
      if (this.currency == null) {
         Bukkit.getConsoleSender()
            .sendMessage(
               StringUtils.formatToString(
                  "&#FF0000[AxTrade] RedisEconomy currency named &#DD0000"
                     + this.internal
                     + " &#FF0000not found! Change the currency-name or disable the hook to get rid of this warning!"
               )
            );
      }
   }

   @Override
   public String getName() {
      return this.internal;
   }

   @Override
   public Map<String, Object> getSettings() {
      return this.settings;
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
      return this.currency == null ? 0.0 : this.currency.getBalance(player);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         EconomyResponse economyResponse = this.currency.depositPlayer(player, null, amount, null);
         return CompletableFuture.completedFuture(economyResponse.transactionSuccess());
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         EconomyResponse economyResponse = this.currency.withdrawPlayer(player, null, amount, null);
         return CompletableFuture.completedFuture(economyResponse.transactionSuccess());
      }
   }
}
