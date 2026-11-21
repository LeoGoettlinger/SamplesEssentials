package github.nighter.smartspawner.hooks.economy.currency;

import github.nighter.smartspawner.SmartSpawner;
import java.util.logging.Level;
import lombok.Generated;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;

public class CurrencyManager {
   private final SmartSpawner plugin;
   private boolean currencyAvailable = false;
   private String activeCurrencyProvider = "None";
   private Economy vaultEconomy;
   private ExcellentEconomyAPI excellentEconomyApi;
   private ExcellentCurrency excellentEconomyCurrency;
   private OperationContext excellentEconomyContext;
   private String configuredCurrencyType;
   private String configuredExcellentEconomyCurrency;

   public CurrencyManager(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
      this.loadConfiguration();
      this.setupCurrency();
   }

   private void loadConfiguration() {
      FileConfiguration config = this.plugin.getItemPriceManager().getSellConfig();
      this.configuredCurrencyType = config.getString("currency", "VAULT");
      this.configuredExcellentEconomyCurrency = config.getString("excellenteconomy_currency", "coins");
   }

   private void setupCurrency() {
      this.currencyAvailable = false;
      this.activeCurrencyProvider = "None";
      if (this.configuredCurrencyType.equalsIgnoreCase("VAULT")) {
         this.currencyAvailable = this.setupVaultEconomy();
      } else if (this.configuredCurrencyType.equalsIgnoreCase("EXCELLENTECONOMY")) {
         this.currencyAvailable = this.setupExcellentEconomy();
      } else {
         this.plugin.getLogger().warning("Unsupported currency type: " + this.configuredCurrencyType + ". Supported types: VAULT, EXCELLENTECONOMY.");
         this.plugin.getLogger().warning("Economy features will be disabled.");
      }
   }

   private boolean setupVaultEconomy() {
      if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
         this.plugin.getLogger().warning("Vault not found! Selling items from spawner will be disabled.");
         return false;
      } else {
         try {
            RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
               this.plugin.getLogger().warning("No economy provider found for Vault! Selling items from spawner will be disabled.");
               return false;
            } else {
               this.vaultEconomy = (Economy)rsp.getProvider();
               if (this.vaultEconomy == null) {
                  this.plugin.getLogger().warning("Failed to get economy provider from Vault! Selling items from spawner will be disabled.");
                  return false;
               } else {
                  this.activeCurrencyProvider = "Vault (" + this.vaultEconomy.getName() + ")";
                  this.plugin.getLogger().info("Successfully connected to Vault & Economy provider: " + this.vaultEconomy.getName());
                  return true;
               }
            }
         } catch (Exception var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Error setting up Vault economy integration", (Throwable)var2);
            return false;
         }
      }
   }

   private boolean setupExcellentEconomy() {
      if (this.plugin.getServer().getPluginManager().getPlugin("ExcellentEconomy") == null) {
         this.plugin.getLogger().warning("ExcellentEconomy not found! Selling items from spawner will be disabled.");
         return false;
      } else {
         try {
            RegisteredServiceProvider<ExcellentEconomyAPI> provider = this.plugin.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
            if (provider == null) {
               this.plugin.getLogger().warning("No ExcellentEconomy API provider found! Selling items from spawner will be disabled.");
               return false;
            } else {
               this.excellentEconomyApi = (ExcellentEconomyAPI)provider.getProvider();
               if (this.excellentEconomyApi == null) {
                  this.plugin.getLogger().warning("Failed to get ExcellentEconomy API provider! Selling items from spawner will be disabled.");
                  return false;
               } else {
                  this.excellentEconomyCurrency = (ExcellentCurrency)this.excellentEconomyApi
                     .currencyById(this.configuredExcellentEconomyCurrency)
                     .orElse(null);
                  if (this.excellentEconomyCurrency == null) {
                     this.plugin
                        .getLogger()
                        .warning(
                           "Could not find ExcellentEconomy currency '"
                              + this.configuredExcellentEconomyCurrency
                              + "'. Selling items from spawner will be disabled."
                        );
                     return false;
                  } else {
                     this.excellentEconomyContext = OperationContext.custom(this.plugin.getName())
                        .silentFor(new NotificationTarget[]{NotificationTarget.USER, NotificationTarget.EXECUTOR, NotificationTarget.CONSOLE_LOGGER});
                     this.activeCurrencyProvider = "ExcellentEconomy (" + this.excellentEconomyCurrency.getName() + ")";
                     this.plugin.getLogger().info("Successfully connected to ExcellentEconomy with currency: " + this.excellentEconomyCurrency.getName());
                     return true;
                  }
               }
            }
         } catch (Exception var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Error setting up ExcellentEconomy integration", (Throwable)var2);
            return false;
         }
      }
   }

   public boolean deposit(double amount, Player player) {
      if (!this.currencyAvailable) {
         this.plugin.getLogger().warning("Currency not available for deposit operation.");
         return false;
      } else if (this.configuredCurrencyType.equalsIgnoreCase("VAULT")) {
         if (this.vaultEconomy == null) {
            this.plugin.getLogger().warning("Vault economy is not initialized.");
            return false;
         } else {
            return this.vaultEconomy.depositPlayer(player, amount).transactionSuccess();
         }
      } else if (this.configuredCurrencyType.equalsIgnoreCase("EXCELLENTECONOMY")) {
         if (this.excellentEconomyApi == null || this.excellentEconomyCurrency == null || this.excellentEconomyContext == null) {
            this.plugin.getLogger().warning("ExcellentEconomy is not initialized.");
            return false;
         } else if (!this.excellentEconomyApi.canPerformOperations()) {
            this.plugin.getLogger().warning("ExcellentEconomy currency operations are currently disabled.");
            return false;
         } else {
            return this.excellentEconomyApi.deposit(player, this.excellentEconomyCurrency, amount, this.excellentEconomyContext);
         }
      } else {
         this.plugin.getLogger().warning("Unsupported currency type during deposit: " + this.configuredCurrencyType);
         return false;
      }
   }

   public boolean withdraw(double amount, Player player) {
      if (!this.currencyAvailable) {
         this.plugin.getLogger().warning("Currency not available for withdraw operation.");
         return false;
      } else if (this.configuredCurrencyType.equalsIgnoreCase("VAULT")) {
         if (this.vaultEconomy == null) {
            this.plugin.getLogger().warning("Vault economy is not initialized.");
            return false;
         } else {
            return this.vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
         }
      } else if (this.configuredCurrencyType.equalsIgnoreCase("EXCELLENTECONOMY")) {
         if (this.excellentEconomyApi == null || this.excellentEconomyCurrency == null || this.excellentEconomyContext == null) {
            this.plugin.getLogger().warning("ExcellentEconomy is not initialized.");
            return false;
         } else if (!this.excellentEconomyApi.canPerformOperations()) {
            this.plugin.getLogger().warning("ExcellentEconomy currency operations are currently disabled.");
            return false;
         } else {
            return this.excellentEconomyApi.withdraw(player, this.excellentEconomyCurrency, amount, this.excellentEconomyContext);
         }
      } else {
         this.plugin.getLogger().warning("Unsupported currency type during withdraw: " + this.configuredCurrencyType);
         return false;
      }
   }

   public void reload() {
      this.cleanup();
      this.loadConfiguration();
      this.setupCurrency();
   }

   public void cleanup() {
      this.vaultEconomy = null;
      this.excellentEconomyApi = null;
      this.excellentEconomyCurrency = null;
      this.excellentEconomyContext = null;
      this.currencyAvailable = false;
      this.activeCurrencyProvider = "None";
   }

   @Generated
   public boolean isCurrencyAvailable() {
      return this.currencyAvailable;
   }

   @Generated
   public String getActiveCurrencyProvider() {
      return this.activeCurrencyProvider;
   }

   @Generated
   public String getConfiguredCurrencyType() {
      return this.configuredCurrencyType;
   }

   @Generated
   public String getConfiguredExcellentEconomyCurrency() {
      return this.configuredExcellentEconomyCurrency;
   }
}
