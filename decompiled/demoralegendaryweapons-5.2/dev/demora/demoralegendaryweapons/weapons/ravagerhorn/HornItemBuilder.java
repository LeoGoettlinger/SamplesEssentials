package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class HornItemBuilder {
   private static final String LANG_KEY = "ravager_horn";

   public static ItemStack build(Plugin plugin, HornKey hornKey) {
      ItemStack stack = new ItemStack(Material.GOAT_HORN);
      ItemMeta meta = stack.getItemMeta();
      if (meta == null) {
         return stack;
      } else {
         String name = "";
         if (plugin instanceof DemoraLegendaryWeapons demora) {
            name = demora.getLanguageManager().getWeaponName("ravager_horn");
         }

         if (name == null || name.isEmpty()) {
            name = "§7§lРог разорителя";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
         meta.setCustomModelData(1);
         meta.getPersistentDataContainer().set(hornKey.getKey(), PersistentDataType.BOOLEAN, true);
         if (plugin instanceof DemoraLegendaryWeapons demora) {
            demora.applyWeaponLore(meta, "ravager_horn");
         }

         stack.setItemMeta(meta);
         return stack;
      }
   }
}
