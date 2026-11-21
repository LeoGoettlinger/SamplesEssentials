package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class RavagerHorn {
   private final Plugin plugin;
   private final HornKey hornKey;

   public RavagerHorn(Plugin plugin) {
      this.plugin = plugin;
      this.hornKey = new HornKey(plugin);
   }

   public ItemStack createRavagerHorn() {
      return HornItemBuilder.build(this.plugin, this.hornKey);
   }

   public boolean isRavagerHorn(ItemStack item) {
      return this.hornKey.matches(item);
   }
}
