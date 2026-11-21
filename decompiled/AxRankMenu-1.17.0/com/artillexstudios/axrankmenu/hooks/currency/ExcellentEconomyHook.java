package com.artillexstudios.axrankmenu.hooks.currency;

import com.artillexstudios.axrankmenu.AxRankMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

public class ExcellentEconomyHook implements CurrencyHook {
   private ExcellentEconomyAPI api;
   private ExcellentCurrency currency = null;

   @Override
   public void setup() {
      this.api = (ExcellentEconomyAPI)Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class).getProvider();
      this.currency = this.api.getCurrency(AxRankMenu.CONFIG.getString("hooks.ExcellentEconomy.currency-name", "coins"));
   }

   @Override
   public String getName() {
      return "ExcellentEconomy";
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull Player p) {
      return this.currency == null ? 0.0 : this.api.getBalance(p, this.currency);
   }

   @Override
   public void giveBalance(@NotNull Player p, double amount) {
      if (this.currency != null) {
         this.api.deposit(p, this.currency, amount);
      }
   }

   @Override
   public void takeBalance(@NotNull Player p, double amount) {
      if (this.currency != null) {
         this.api.withdraw(p, this.currency, amount);
      }
   }
}
