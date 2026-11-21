package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends BaseDependency {
   private final Economy economy = this.setupEconomy();

   public VaultHook(Hook hook) {
      super(hook);
   }

   private Economy setupEconomy() {
      RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
      if (economyProvider != null) {
         return (Economy)economyProvider.getProvider();
      } else {
         throw new DependencyException("Error loading Vault! No Economy plugin found", Hook.VAULT);
      }
   }

   @Override
   public String onEnableMessage() {
      return "Vault Found! Using it for currency related features";
   }

   public Economy getEconomy() {
      return this.economy;
   }
}
