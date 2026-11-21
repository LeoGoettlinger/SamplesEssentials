package com.artillexstudios.axrankmenu.hooks.currency;

import com.artillexstudios.axrankmenu.AxRankMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

public class CoinsEngineHook implements CurrencyHook {
   private Currency currency = null;

   @Override
   public void setup() {
      this.currency = CoinsEngineAPI.getCurrency(AxRankMenu.CONFIG.getString("hooks.CoinsEngine.currency-name", "coins"));
   }

   @Override
   public String getName() {
      return "CoinsEngine";
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull Player p) {
      return this.currency == null ? 0.0 : CoinsEngineAPI.getBalance(p, this.currency);
   }

   @Override
   public void giveBalance(@NotNull Player p, double amount) {
      if (this.currency != null) {
         CoinsEngineAPI.addBalance(p, this.currency, amount);
      }
   }

   @Override
   public void takeBalance(@NotNull Player p, double amount) {
      if (this.currency != null) {
         CoinsEngineAPI.removeBalance(p, this.currency, amount);
      }
   }
}
