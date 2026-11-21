package com.artillexstudios.axrankmenu.hooks.currency;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultHook implements CurrencyHook {
   private Economy econ = null;

   @Override
   public void setup() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
         this.econ = (Economy)rsp.getProvider();
      }
   }

   @Override
   public String getName() {
      return "Vault";
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull Player p) {
      return this.econ.getBalance(p);
   }

   @Override
   public void giveBalance(@NotNull Player p, double amount) {
      this.econ.depositPlayer(p, amount);
   }

   @Override
   public void takeBalance(@NotNull Player p, double amount) {
      this.econ.withdrawPlayer(p, amount);
   }
}
