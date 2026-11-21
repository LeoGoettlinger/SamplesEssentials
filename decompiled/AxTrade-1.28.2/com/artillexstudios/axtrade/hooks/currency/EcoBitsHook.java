package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.willfp.ecobits.currencies.Currencies;
import com.willfp.ecobits.currencies.Currency;
import com.willfp.ecobits.currencies.CurrencyUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class EcoBitsHook implements CurrencyHook {
   private Currency currency = null;
   private final Map<String, Object> settings;
   private final String internal;

   public EcoBitsHook(Map<Object, Object> settings) {
      Map<String, Object> map = new HashMap<>();

      for (Entry<Object, Object> entry : settings.entrySet()) {
         map.put((String)entry.getKey(), entry.getValue());
      }

      this.settings = map;
      this.internal = (String)settings.get("currency-name");
   }

   @Override
   public void setup() {
      this.currency = Currencies.getByID(this.internal);
      if (this.currency == null) {
         Bukkit.getConsoleSender()
            .sendMessage(
               StringUtils.formatToString(
                  "&#FF0000[AxTrade] EcoBits currency named &#DD0000"
                     + this.internal
                     + " &#FF0000not found! Change the currency-name or disable the hook to get rid of this warning!"
               )
            );
      }
   }

   @Override
   public String getName() {
      return "EcoBits-" + this.internal;
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
      return this.currency == null ? 0.0 : CurrencyUtils.getBalance(Bukkit.getOfflinePlayer(player), this.currency).doubleValue();
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CurrencyUtils.adjustBalance(Bukkit.getOfflinePlayer(player), this.currency, BigDecimal.valueOf(amount));
         return CompletableFuture.completedFuture(true);
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      if (this.currency == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CurrencyUtils.adjustBalance(Bukkit.getOfflinePlayer(player), this.currency, BigDecimal.valueOf(amount * -1.0));
         return CompletableFuture.completedFuture(true);
      }
   }
}
