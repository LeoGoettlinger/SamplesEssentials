package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

public class ExcellentEconomyHook implements CurrencyHook {
   private Method method;
   private ExcellentEconomyAPI api;
   private ExcellentCurrency currency = null;
   private final Map<String, Object> settings;
   private final String internal;

   public ExcellentEconomyHook(Map<Object, Object> settings) {
      Map<String, Object> map = new HashMap<>();

      for (Entry<Object, Object> entry : settings.entrySet()) {
         map.put((String)entry.getKey(), entry.getValue());
      }

      this.settings = map;
      this.internal = (String)settings.get("currency-name");

      try {
         Class<?> clazz = Class.forName("su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency");
         this.method = clazz.getMethod("isDecimal");
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   @Override
   public void setup() {
      this.api = (ExcellentEconomyAPI)Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class).getProvider();
      this.currency = this.api.getCurrency(this.internal);
      if (this.currency == null) {
         Bukkit.getConsoleSender()
            .sendMessage(
               StringUtils.formatToString(
                  "&#FF0000[AxTrade] ExcellentEconomy currency named &#DD0000"
                     + this.internal
                     + " &#FF0000not found! Change the currency-name or disable the hook to get rid of this warning!"
               )
            );
      }
   }

   @Override
   public String getName() {
      return "ExcellentEconomy-" + this.internal;
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
      try {
         return (Boolean)this.method.invoke(this.currency);
      } catch (Exception var2) {
         var2.printStackTrace();
         return true;
      }
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return this.currency == null ? 0.0 : this.api.getBalance(Bukkit.getPlayer(player), this.currency);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CompletableFuture<Boolean> cf = new CompletableFuture<>();
         this.api.depositAsync(player, this.currency, amount).thenAccept(result -> cf.complete(result.success()));
         return cf;
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CompletableFuture<Boolean> cf = new CompletableFuture<>();
         this.api.withdrawAsync(player, this.currency, amount).thenAccept(result -> cf.complete(result.success()));
         return cf;
      }
   }
}
