package github.nighter.smartspawner.hooks.economy.shops.providers.economyshopgui;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import lombok.Generated;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EconomyShopGUIProvider implements ShopProvider {
   private final SmartSpawner plugin;
   private static final String[] PLUGIN_NAMES = new String[]{"EconomyShopGUI", "EconomyShopGUI-Premium"};

   @Override
   public String getPluginName() {
      return this.plugin.getServer().getPluginManager().getPlugin("EconomyShopGUI-Premium") != null ? "EconomyShopGUI-Premium" : "EconomyShopGUI";
   }

   @Override
   public boolean isAvailable() {
      try {
         Plugin economyShopGUI = null;

         for (String pluginName : PLUGIN_NAMES) {
            economyShopGUI = Bukkit.getPluginManager().getPlugin(pluginName);
            if (economyShopGUI != null) {
               break;
            }
         }

         if (economyShopGUI != null) {
            return true;
         }
      } catch (Exception var6) {
         this.plugin.getLogger().warning("Error initializing EconomyShopGUI integration: " + var6.getMessage());
      }

      return false;
   }

   @Override
   public double getSellPrice(Material material) {
      try {
         ItemStack item = new ItemStack(material);
         ShopItem shopItem = EconomyShopGUIHook.getShopItem(item);
         if (shopItem == null) {
            return 0.0;
         } else {
            Double sellPrice = EconomyShopGUIHook.getItemSellPrice(shopItem, item);
            return sellPrice != null ? sellPrice : 0.0;
         }
      } catch (Exception var5) {
         return 0.0;
      }
   }

   @Generated
   public EconomyShopGUIProvider(SmartSpawner plugin) {
      this.plugin = plugin;
   }
}
