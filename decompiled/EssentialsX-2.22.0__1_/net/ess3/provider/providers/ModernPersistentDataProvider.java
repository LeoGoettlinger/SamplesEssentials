package net.ess3.provider.providers;

import net.ess3.provider.PersistentDataProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

@ProviderData(
   description = "1.14.4+ Persistent Data Container Provider",
   weight = 1
)
public class ModernPersistentDataProvider implements PersistentDataProvider {
   private final Plugin plugin;

   public ModernPersistentDataProvider(Plugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void set(ItemStack itemStack, String key, String value) {
      if (itemStack != null && itemStack.getItemMeta() != null && key != null && value != null) {
         ItemMeta im = itemStack.getItemMeta();
         im.getPersistentDataContainer().set(new NamespacedKey(this.plugin, key), PersistentDataType.STRING, value);
         itemStack.setItemMeta(im);
      }
   }

   @Override
   public String getString(ItemStack itemStack, String key) {
      if (itemStack != null && itemStack.getItemMeta() != null && key != null) {
         try {
            return (String)itemStack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this.plugin, key), PersistentDataType.STRING);
         } catch (IllegalArgumentException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public void remove(ItemStack itemStack, String key) {
      itemStack.getItemMeta().getPersistentDataContainer().remove(new NamespacedKey(this.plugin, key));
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.persistence.PersistentDataHolder");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }
}
