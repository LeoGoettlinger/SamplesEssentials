package dev.demora.demoralegendaryweapons.weapons.artemisbow;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ArtemisBow {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey artemisBowKey;

   public ArtemisBow(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.artemisBowKey = new NamespacedKey(plugin, "artemis_bow");
   }

   public ItemStack createArtemisBow() {
      ItemStack bow = new ItemStack(Material.BOW);
      ItemMeta meta = bow.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("artemis_bow");
         if (weaponName.isEmpty()) {
            weaponName = "§e§lЛук Артемиды";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(3);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.removeEnchant(Enchantment.INFINITY);
         meta.getPersistentDataContainer().set(this.artemisBowKey, PersistentDataType.STRING, "artemis_bow");
         this.plugin.applyWeaponLore(meta, "artemis_bow");
         bow.setItemMeta(meta);
      }

      return bow;
   }

   public boolean isArtemisBow(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.artemisBowKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public double getSpecialArrowChance() {
      return this.plugin.getConfig().getDouble("weapons.artemis_bow.special_arrow_chance", 0.3);
   }

   public double getArrowDamage() {
      return this.plugin.getConfig().getDouble("weapons.artemis_bow.arrow_damage", 6.0);
   }

   public double getSpecialArrowDamage() {
      return this.plugin.getConfig().getDouble("weapons.artemis_bow.special_arrow_damage", 10.0);
   }
}
