package github.nighter.smartspawner.hooks.economy.shops.providers.zshop;

import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.api.buttons.ItemButton;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import java.util.Optional;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public class ZShopProvider implements ShopProvider {
   private final SmartSpawner plugin;
   private ShopManager shopManager;

   @Override
   public String getPluginName() {
      return "zShop";
   }

   @Override
   public boolean isAvailable() {
      try {
         Plugin zShopPlugin = Bukkit.getPluginManager().getPlugin("zShop");
         if (zShopPlugin != null && zShopPlugin.isEnabled()) {
            Class.forName("fr.maxlego08.shop.api.ShopManager");
            Class.forName("fr.maxlego08.shop.api.buttons.ItemButton");
            ShopManager manager = this.getShopManager();
            return manager != null;
         }
      } catch (NoClassDefFoundError | ClassNotFoundException var3) {
      } catch (Exception var4) {
         this.plugin.getLogger().warning("Error initializing zShop integration: " + var4.getMessage());
      }

      return false;
   }

   @Override
   public double getSellPrice(Material material) {
      try {
         Optional<ItemButton> itemButtonOpt = this.getItemButton(material);
         if (itemButtonOpt.isEmpty()) {
            return 0.0;
         } else {
            ItemButton itemButton = itemButtonOpt.get();
            double sellPrice = itemButton.getSellPrice();
            return sellPrice > 0.0 ? sellPrice : 0.0;
         }
      } catch (Exception var6) {
         return 0.0;
      }
   }

   private ShopManager getShopManager() {
      if (this.shopManager != null) {
         return this.shopManager;
      } else {
         try {
            this.shopManager = (ShopManager)this.plugin.getServer().getServicesManager().getRegistration(ShopManager.class).getProvider();
            return this.shopManager;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   private Optional<ItemButton> getItemButton(Material material) {
      try {
         ShopManager manager = this.getShopManager();
         return manager == null ? Optional.empty() : manager.getItemButton(material);
      } catch (Exception var3) {
         this.plugin.getLogger().log(Level.SEVERE, "Error getting item button from zShop", (Throwable)var3);
         return Optional.empty();
      }
   }

   @Generated
   public ZShopProvider(SmartSpawner plugin) {
      this.plugin = plugin;
   }
}
