package github.nighter.smartspawner.hooks.economy;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.currency.CurrencyManager;
import github.nighter.smartspawner.hooks.economy.shops.ShopIntegrationManager;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ItemPriceManager {
   private static final String SELL_FILE_NAME = "sell_integration.yml";
   private static final String PRICES_SECTION = "custom_prices.prices";
   private final SmartSpawner plugin;
   private final Map<String, Double> itemPrices = new ConcurrentHashMap<>();
   private File sellFile;
   private FileConfiguration sellConfig;
   private ShopIntegrationManager shopIntegrationManager;
   private CurrencyManager currencyManager;
   private double defaultPrice;
   private ItemPriceManager.PriceSourceMode priceSourceMode;
   private boolean economyEnabled;
   public boolean customPricesEnabled;
   public boolean shopIntegrationEnabled;

   public void init() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdirs();
      }

      this.loadConfiguration();
      if (this.economyEnabled) {
         this.currencyManager = new CurrencyManager(this.plugin);
         this.currencyManager.initialize();
         if (this.shopIntegrationEnabled) {
            this.shopIntegrationManager = new ShopIntegrationManager(this.plugin);
            this.shopIntegrationManager.initialize();
         }

         if (this.customPricesEnabled) {
            this.loadPrices();
         }

         this.validatePriceSourceMode();
      } else {
         this.plugin.getLogger().info("Sell integration is disabled. No sell integration will be available.");
      }
   }

   private void loadConfiguration() {
      this.sellFile = new File(this.plugin.getDataFolder(), "sell_integration.yml");
      if (!this.sellFile.exists()) {
         this.plugin.saveResource("sell_integration.yml", false);
      }

      this.sellConfig = YamlConfiguration.loadConfiguration(this.sellFile);
      FileConfiguration config = this.sellConfig;
      this.economyEnabled = config.getBoolean("enabled", true);
      this.defaultPrice = config.getDouble("custom_prices.default_price", 1.0);
      this.customPricesEnabled = config.getBoolean("custom_prices.enabled", true);
      this.shopIntegrationEnabled = config.getBoolean("shop_integration.enabled", true);
      String modeString = config.getString("price_source_mode", "SHOP_PRIORITY");

      try {
         this.priceSourceMode = ItemPriceManager.PriceSourceMode.valueOf(modeString.toUpperCase());
      } catch (IllegalArgumentException var4) {
         this.plugin.getLogger().warning("Invalid price source mode: " + modeString + ". Using SHOP_PRIORITY");
         this.priceSourceMode = ItemPriceManager.PriceSourceMode.SHOP_PRIORITY;
      }
   }

   private void validatePriceSourceMode() {
      if (this.economyEnabled) {
         boolean hasValidShopIntegration = this.shopIntegrationEnabled
            && this.shopIntegrationManager != null
            && this.shopIntegrationManager.hasActiveProvider();
         boolean hasValidCustomPrices = this.customPricesEnabled && !this.itemPrices.isEmpty();
         if (this.priceSourceMode == ItemPriceManager.PriceSourceMode.CUSTOM_ONLY && hasValidShopIntegration) {
            this.plugin.getLogger().warning("Price source mode is set to CUSTOM_ONLY but shop integration is enabled and working.");
            this.plugin.getLogger().warning("Prices from shop integration will not be used.");
         }

         if (this.priceSourceMode == ItemPriceManager.PriceSourceMode.SHOP_ONLY && !hasValidShopIntegration) {
            this.plugin.getLogger().warning("Price source mode is set to SHOP_ONLY but no valid shop integration is available.");
            this.plugin.getLogger().warning("Selling items from spawner will be disabled.");
         }

         if ((
               this.priceSourceMode == ItemPriceManager.PriceSourceMode.CUSTOM_PRIORITY
                  || this.priceSourceMode == ItemPriceManager.PriceSourceMode.SHOP_PRIORITY
            )
            && !hasValidCustomPrices
            && !hasValidShopIntegration) {
            this.plugin.getLogger().warning("Price source mode " + this.priceSourceMode + " requires at least one valid price source (custom or shop).");
            this.plugin.getLogger().warning("Selling items from spawner will be disabled.");
         }

         if (this.priceSourceMode == ItemPriceManager.PriceSourceMode.CUSTOM_ONLY && !hasValidCustomPrices) {
            this.plugin.getLogger().warning("Price source mode is set to CUSTOM_ONLY but no valid custom prices are available.");
            this.plugin.getLogger().warning("Custom prices enabled: " + this.customPricesEnabled + ", Loaded prices: " + this.itemPrices.size());
            this.plugin.getLogger().warning("Selling items from spawner will be disabled.");
         }
      }
   }

   private void loadPrices() {
      this.itemPrices.clear();
      ConfigurationSection prices = this.sellConfig.getConfigurationSection("custom_prices.prices");
      if (prices != null) {
         for (String key : prices.getKeys(false)) {
            this.itemPrices.put(key, prices.getDouble(key, this.defaultPrice));
         }
      }
   }

   public double getPrice(Material material) {
      if (material != null && this.economyEnabled) {
         switch (this.priceSourceMode) {
            case CUSTOM_ONLY:
               return this.getCustomPrice(material);
            case SHOP_ONLY:
               return this.getShopPrice(material);
            case CUSTOM_PRIORITY:
               double customPrice = this.getCustomPrice(material);
               return customPrice > 0.0 ? customPrice : this.getShopPrice(material);
            case SHOP_PRIORITY:
               double shopPrice = this.getShopPrice(material);
               return shopPrice > 0.0 ? shopPrice : this.getCustomPrice(material);
            default:
               return this.defaultPrice;
         }
      } else {
         return 0.0;
      }
   }

   private double getCustomPrice(Material material) {
      return this.economyEnabled && this.customPricesEnabled ? this.itemPrices.getOrDefault(material.name(), this.defaultPrice) : 0.0;
   }

   private double getShopPrice(Material material) {
      return this.economyEnabled && this.shopIntegrationEnabled && this.shopIntegrationManager != null ? this.shopIntegrationManager.getPrice(material) : 0.0;
   }

   public void setPrice(Material material, double price) {
      if (material != null && this.economyEnabled && this.customPricesEnabled) {
         this.itemPrices.put(material.name(), price);
         this.sellConfig.set("custom_prices.prices." + material.name(), price);
         this.saveConfig();
      }
   }

   public void reload() {
      this.loadConfiguration();
      if (this.economyEnabled) {
         if (this.currencyManager != null) {
            this.currencyManager.reload();
         } else {
            this.currencyManager = new CurrencyManager(this.plugin);
            this.currencyManager.initialize();
         }

         if (this.shopIntegrationEnabled) {
            if (this.shopIntegrationManager == null) {
               this.shopIntegrationManager = new ShopIntegrationManager(this.plugin);
            }

            this.shopIntegrationManager.initialize();
         } else {
            this.shopIntegrationManager = null;
         }

         if (this.customPricesEnabled) {
            this.loadPrices();
         } else {
            this.itemPrices.clear();
         }

         this.validatePriceSourceMode();
      } else {
         if (this.currencyManager != null) {
            this.currencyManager.cleanup();
            this.currencyManager = null;
         }

         this.shopIntegrationManager = null;
         this.itemPrices.clear();
         this.plugin.getLogger().info("Storage selling disabled - all sell integration cleaned up.");
      }
   }

   public void reloadShopIntegration() {
      if (this.shopIntegrationEnabled) {
         if (this.shopIntegrationManager == null) {
            this.shopIntegrationManager = new ShopIntegrationManager(this.plugin);
         }

         this.shopIntegrationManager.initialize();
      } else {
         this.shopIntegrationManager = null;
      }
   }

   public boolean hasSellIntegration() {
      if (!this.economyEnabled) {
         return false;
      } else if (this.currencyManager != null && this.currencyManager.isCurrencyAvailable()) {
         boolean hasValidCustomPrices = this.customPricesEnabled && !this.itemPrices.isEmpty();
         boolean hasValidShopIntegration = this.shopIntegrationEnabled
            && this.shopIntegrationManager != null
            && this.shopIntegrationManager.hasActiveProvider();
         if (this.priceSourceMode == ItemPriceManager.PriceSourceMode.CUSTOM_ONLY) {
            return hasValidCustomPrices;
         } else {
            return this.priceSourceMode == ItemPriceManager.PriceSourceMode.SHOP_ONLY
               ? hasValidShopIntegration
               : hasValidCustomPrices || hasValidShopIntegration;
         }
      } else {
         return false;
      }
   }

   public boolean hasPriceFor(Material material) {
      if (material != null && this.economyEnabled) {
         return switch (this.priceSourceMode) {
            case CUSTOM_ONLY -> this.customPricesEnabled && this.itemPrices.containsKey(material.name());
            case SHOP_ONLY -> this.shopIntegrationEnabled && this.shopIntegrationManager != null && this.shopIntegrationManager.getPrice(material) > 0.0;
            case CUSTOM_PRIORITY, SHOP_PRIORITY -> this.customPricesEnabled && this.itemPrices.containsKey(material.name())
               || this.shopIntegrationEnabled && this.shopIntegrationManager != null && this.shopIntegrationManager.getPrice(material) > 0.0;
            default -> false;
         };
      } else {
         return false;
      }
   }

   public void removePrice(Material material) {
      if (material != null && this.economyEnabled && this.customPricesEnabled) {
         this.itemPrices.remove(material.name());
         this.sellConfig.set("custom_prices.prices." + material.name(), null);
         this.saveConfig();
      }
   }

   public Map<String, Double> getAllPrices() {
      return !this.economyEnabled ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(this.itemPrices);
   }

   public String getActivePriceSource() {
      if (!this.economyEnabled) {
         return "Economy Disabled";
      } else {
         StringBuilder sources = new StringBuilder();
         if (!this.customPricesEnabled && !this.shopIntegrationEnabled) {
            sources.append("None (using default prices)");
         } else {
            if (this.customPricesEnabled) {
               sources.append("Custom");
            }

            if (this.shopIntegrationEnabled) {
               if (sources.length() > 0) {
                  sources.append(" + ");
               }

               String activeShop = this.shopIntegrationManager != null ? this.shopIntegrationManager.getActiveShopPlugin() : "None";
               sources.append("Shop (").append(activeShop).append(")");
            }
         }

         sources.append(" [Mode: ").append(this.priceSourceMode).append("]");
         if (this.currencyManager != null) {
            sources.append(" [Currency: ").append(this.currencyManager.getActiveCurrencyProvider()).append("]");
         }

         return sources.toString();
      }
   }

   private void saveConfig() {
      if (this.economyEnabled) {
         try {
            this.sellConfig.save(this.sellFile);
         } catch (IOException var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save sell_integration.yml", (Throwable)var2);
         }
      }
   }

   public void cleanup() {
      if (this.currencyManager != null) {
         this.currencyManager.cleanup();
         this.currencyManager = null;
      }

      if (this.shopIntegrationManager != null) {
         this.shopIntegrationManager.cleanup();
         this.shopIntegrationManager = null;
      }

      this.itemPrices.clear();
   }

   @Generated
   public ItemPriceManager(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   @Generated
   public FileConfiguration getSellConfig() {
      return this.sellConfig;
   }

   @Generated
   public ShopIntegrationManager getShopIntegrationManager() {
      return this.shopIntegrationManager;
   }

   @Generated
   public CurrencyManager getCurrencyManager() {
      return this.currencyManager;
   }

   public static enum PriceSourceMode {
      CUSTOM_ONLY,
      SHOP_ONLY,
      CUSTOM_PRIORITY,
      SHOP_PRIORITY;
   }
}
