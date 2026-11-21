package dev.demora.demoralegendaryweapons.weapons.toxiccrossbow;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ToxicCrossbow {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey toxicCrossbowKey;

   public ToxicCrossbow(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.toxicCrossbowKey = new NamespacedKey(plugin, "toxic_crossbow");
   }

   public ItemStack createToxicCrossbow() {
      ItemStack crossbow = new ItemStack(Material.CROSSBOW);
      ItemMeta meta = crossbow.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("toxic_crossbow");
         if (weaponName.isEmpty()) {
            weaponName = "§a§lТоксичный арбалет";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(4);
         meta.addEnchant(Enchantment.QUICK_CHARGE, 2, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.toxicCrossbowKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "toxic_crossbow");
         crossbow.setItemMeta(meta);
      }

      return crossbow;
   }

   public boolean isToxicCrossbow(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.toxicCrossbowKey, PersistentDataType.BOOLEAN)
         : false;
   }
}
