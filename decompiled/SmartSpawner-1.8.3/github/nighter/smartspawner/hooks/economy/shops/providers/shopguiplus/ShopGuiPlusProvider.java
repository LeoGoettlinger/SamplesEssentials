package github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import lombok.Generated;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ShopGuiPlusProvider implements ShopProvider {
   private final SmartSpawner plugin;

   @Override
   public String getPluginName() {
      return "ShopGUIPlus";
   }

   @Override
   public boolean isAvailable() {
      try {
         Plugin shopGuiPlugin = Bukkit.getPluginManager().getPlugin("ShopGUIPlus");
         if (shopGuiPlugin != null && shopGuiPlugin.isEnabled()) {
            Class.forName("net.brcdev.shopgui.ShopGuiPlusApi");
            return true;
         }
      } catch (NoClassDefFoundError | ClassNotFoundException var2) {
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Error initializing ShopGUIPlus integration: " + var3.getMessage());
      }

      return false;
   }

   @Override
   public double getSellPrice(Material material) {
      try {
         ItemStack item = new ItemStack(material);
         double sellPrice = ShopGuiPlusApi.getItemStackPriceSell(item);
         return sellPrice > 0.0 ? sellPrice : 0.0;
      } catch (Exception var5) {
         return 0.0;
      }
   }

   @Generated
   public ShopGuiPlusProvider(SmartSpawner plugin) {
      this.plugin = plugin;
   }
}
