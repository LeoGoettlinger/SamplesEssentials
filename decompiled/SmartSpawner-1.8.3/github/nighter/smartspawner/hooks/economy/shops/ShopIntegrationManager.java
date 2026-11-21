package github.nighter.smartspawner.hooks.economy.shops;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.economyshopgui.ESGUICompatibilityHandler;
import github.nighter.smartspawner.hooks.economy.shops.providers.economyshopgui.EconomyShopGUIProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus.ShopGuiPlusProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus.SpawnerHook;
import github.nighter.smartspawner.hooks.economy.shops.providers.zshop.ZShopProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public class ShopIntegrationManager {
   private final SmartSpawner plugin;
   private ShopProvider activeProvider;
   private final List<ShopProvider> availableProviders = new ArrayList<>();
   private SpawnerHook spawnerHook = null;
   private ESGUICompatibilityHandler esguiCompatibilityHandler = null;

   public void initialize() {
      this.availableProviders.clear();
      this.activeProvider = null;
      this.detectAndRegisterActiveProviders();
      this.selectActiveProvider();
   }

   private void detectAndRegisterActiveProviders() {
      String configuredShop = this.plugin.getItemPriceManager().getSellConfig().getString("shop_integration.preferred_plugin", "auto");
      boolean autoDetect = "auto".equalsIgnoreCase(configuredShop);
      if (!autoDetect) {
         if (this.tryRegisterSpecificProvider(configuredShop)) {
            return;
         }

         this.plugin.getLogger().warning("Configured shop plugin '" + configuredShop + "' could not be loaded. Falling back to auto-detection.");
      }

      this.registerProviderIfAvailable("EconomyShopGUI", () -> {
         EconomyShopGUIProvider provider = new EconomyShopGUIProvider(this.plugin);
         if (provider.isAvailable() && this.esguiCompatibilityHandler == null) {
            try {
               this.esguiCompatibilityHandler = new ESGUICompatibilityHandler(this.plugin);
               this.plugin.getServer().getPluginManager().registerEvents(this.esguiCompatibilityHandler, this.plugin);
            } catch (Exception var3) {
               this.plugin.getLogger().warning("Failed to register ESGUICompatibilityHandler: " + var3.getMessage());
            }
         }

         return provider;
      });
      if (this.isPluginAvailable("ShopGUIPlus")) {
         this.registerProviderIfAvailable("ShopGUIPlus", () -> {
            if (this.spawnerHook == null) {
               try {
                  this.spawnerHook = new SpawnerHook(this.plugin);
                  this.plugin.getServer().getPluginManager().registerEvents(this.spawnerHook, this.plugin);
               } catch (Exception var2x) {
                  throw var2x;
               }
            }

            return new ShopGuiPlusProvider(this.plugin);
         });
      }
   }

   private boolean tryRegisterSpecificProvider(String providerName) {
      try {
         String var2 = providerName.toLowerCase();
         switch (var2) {
            case "economyshopgui":
               if (this.isPluginAvailable("EconomyShopGUI")) {
                  this.registerProviderIfAvailable("EconomyShopGUI", () -> {
                     EconomyShopGUIProvider provider = new EconomyShopGUIProvider(this.plugin);
                     if (provider.isAvailable() && this.esguiCompatibilityHandler == null) {
                        try {
                           this.esguiCompatibilityHandler = new ESGUICompatibilityHandler(this.plugin);
                           this.plugin.getServer().getPluginManager().registerEvents(this.esguiCompatibilityHandler, this.plugin);
                        } catch (Exception var3) {
                           this.plugin.getLogger().warning("Failed to register ESGUICompatibilityHandler: " + var3.getMessage());
                        }
                     }

                     return provider;
                  });
                  return !this.availableProviders.isEmpty();
               }
               break;
            case "shopguiplus":
               if (this.isPluginAvailable("ShopGUIPlus")) {
                  this.registerProviderIfAvailable("ShopGUIPlus", () -> {
                     if (this.spawnerHook == null) {
                        this.spawnerHook = new SpawnerHook(this.plugin);
                        this.plugin.getServer().getPluginManager().registerEvents(this.spawnerHook, this.plugin);
                     }

                     return new ShopGuiPlusProvider(this.plugin);
                  });
                  return !this.availableProviders.isEmpty();
               }
               break;
            case "zshop":
               if (this.isPluginAvailable("ZShop")) {
                  this.registerProviderIfAvailable("ZShop", () -> new ZShopProvider(this.plugin));
                  return !this.availableProviders.isEmpty();
               }
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private boolean isPluginAvailable(String pluginName) {
      Plugin targetPlugin = this.plugin.getServer().getPluginManager().getPlugin(pluginName);
      return targetPlugin != null && targetPlugin.isEnabled();
   }

   private void registerProviderIfAvailable(String providerName, Supplier<ShopProvider> providerSupplier) {
      if (this.availableProviders.isEmpty()) {
         try {
            ShopProvider provider = providerSupplier.get();
            if (provider.isAvailable()) {
               this.availableProviders.add(provider);
            }
         } catch (NoClassDefFoundError var4) {
         } catch (Exception var5) {
         }
      }
   }

   private void selectActiveProvider() {
      if (this.availableProviders.isEmpty()) {
         this.plugin.getLogger().info("No compatible shop plugins found. Shop integration is disabled.");
      } else {
         this.activeProvider = this.availableProviders.getFirst();
         this.plugin.getLogger().info("Auto-detected & successfully hook into shop plugin: " + this.activeProvider.getPluginName());
      }
   }

   public double getPrice(Material material) {
      if (this.activeProvider != null && material != null) {
         try {
            return this.activeProvider.getSellPrice(material);
         } catch (Exception var3) {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   public String getActiveShopPlugin() {
      return this.activeProvider != null ? this.activeProvider.getPluginName() : "None";
   }

   public boolean hasActiveProvider() {
      return this.activeProvider != null;
   }

   public void cleanup() {
      this.availableProviders.clear();
      this.activeProvider = null;
      if (this.spawnerHook != null) {
         this.spawnerHook.unregister();
         this.spawnerHook = null;
      }

      if (this.esguiCompatibilityHandler != null) {
         this.esguiCompatibilityHandler = null;
      }
   }

   @Generated
   public ShopIntegrationManager(SmartSpawner plugin) {
      this.plugin = plugin;
   }
}
