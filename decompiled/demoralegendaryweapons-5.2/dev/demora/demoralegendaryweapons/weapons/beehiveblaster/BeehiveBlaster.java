package dev.demora.demoralegendaryweapons.weapons.beehiveblaster;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BeehiveBlaster {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey beehiveBlasterKey;

   public BeehiveBlaster(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.beehiveBlasterKey = new NamespacedKey(plugin, "beehive_blaster");
   }

   public ItemStack createBeehiveBlaster() {
      ItemStack bow = new ItemStack(Material.BOW);
      ItemMeta meta = bow.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("beehive_blaster");
         if (weaponName.isEmpty()) {
            weaponName = "§e§lПчелиный бластер";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(9);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.removeEnchant(Enchantment.INFINITY);
         meta.getPersistentDataContainer().set(this.beehiveBlasterKey, PersistentDataType.STRING, "beehive_blaster");
         this.plugin.applyWeaponLore(meta, "beehive_blaster");
         bow.setItemMeta(meta);
      }

      return bow;
   }

   public boolean isBeehiveBlaster(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.beehiveBlasterKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public double getBeeHealth() {
      return this.plugin.getConfig().getDouble("weapons.beehive_blaster.bee_health", 12.0);
   }

   public double getBeeDamage() {
      return this.plugin.getConfig().getDouble("weapons.beehive_blaster.bee_damage", 4.0);
   }

   public int getBeeDespawnSeconds() {
      return this.plugin.getConfig().getInt("weapons.beehive_blaster.bee_despawn_seconds", 20);
   }

   public int getCooldownSeconds() {
      return this.plugin.getConfig().getInt("weapons.beehive_blaster.cooldown_seconds", 25);
   }
}
