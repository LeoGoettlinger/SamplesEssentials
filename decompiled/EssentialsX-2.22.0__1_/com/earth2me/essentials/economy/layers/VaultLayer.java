package com.earth2me.essentials.economy.layers;

import com.earth2me.essentials.economy.EconomyLayer;
import java.math.BigDecimal;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class VaultLayer implements EconomyLayer {
   private Plugin plugin;
   private Economy adapter;

   @Override
   public boolean hasAccount(OfflinePlayer player) {
      return this.adapter.hasAccount(player);
   }

   @Override
   public boolean createPlayerAccount(OfflinePlayer player) {
      return this.adapter.createPlayerAccount(player);
   }

   @Override
   public BigDecimal getBalance(OfflinePlayer player) {
      return BigDecimal.valueOf(this.adapter.getBalance(player));
   }

   @Override
   public boolean deposit(OfflinePlayer player, BigDecimal amount) {
      return this.adapter.depositPlayer(player, amount.doubleValue()).transactionSuccess();
   }

   @Override
   public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
      return this.adapter.withdrawPlayer(player, amount.doubleValue()).transactionSuccess();
   }

   @Override
   public String getName() {
      return "Vault Compatibility Layer";
   }

   @Override
   public String getBackendName() {
      return this.adapter.getName();
   }

   @Override
   public void enable(Plugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onServerLoad() {
      this.adapter = (Economy)Bukkit.getServicesManager().load(Economy.class);
      return this.adapter != null && !this.adapter.getName().equals("Essentials Economy") && !this.adapter.getName().equals("EssentialsX Economy");
   }

   @Override
   public void disable() {
      this.plugin = null;
      this.adapter = null;
   }

   @Override
   public String getPluginName() {
      return "Vault";
   }

   @Override
   public String getPluginVersion() {
      return this.plugin == null ? null : this.plugin.getDescription().getVersion();
   }
}
