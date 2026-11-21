package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.economy.VaultEconomyService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyCoordinator {
   private final EzRtpPlugin plugin;

   public EconomyCoordinator(EzRtpPlugin plugin) {
      this.plugin = plugin;
   }

   public EconomyService initializeEconomy() {
      PluginManager pluginManager = this.plugin.getServer().getPluginManager();
      if (!pluginManager.isPluginEnabled("Vault")) {
         this.plugin.getLogger().info("Vault not found. Random teleport costs will be disabled.");
         return EconomyService.disabled();
      } else {
         RegisteredServiceProvider<Economy> registration = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
         if (registration != null && registration.getProvider() != null) {
            VaultEconomyService vaultEconomy = new VaultEconomyService((Economy)registration.getProvider(), this.plugin.getLogger());
            this.plugin.getLogger().info("Economy: " + vaultEconomy.getProviderName() + " (Vault)");
            return vaultEconomy;
         } else {
            this.plugin.getLogger().warning("Vault is installed but no economy provider is registered. Random teleport costs will be disabled.");
            return EconomyService.disabled();
         }
      }
   }
}
