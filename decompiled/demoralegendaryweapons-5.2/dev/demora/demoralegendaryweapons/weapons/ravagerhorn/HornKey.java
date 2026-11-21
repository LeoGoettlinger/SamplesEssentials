package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

class HornKey {
   private final NamespacedKey key;

   public HornKey(Plugin plugin) {
      this.key = new NamespacedKey(plugin, "ravager_horn");
   }

   public boolean matches(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.key, PersistentDataType.BOOLEAN) : false;
   }

   public NamespacedKey getKey() {
      return this.key;
   }
}
