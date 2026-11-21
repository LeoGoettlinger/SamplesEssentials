package dev.demora.demoralegendaryweapons.weapons.fugucannon;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FuguCannon {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey fuguCannonKey;

   public FuguCannon(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.fuguCannonKey = new NamespacedKey(plugin, "fugu_cannon");
   }

   public ItemStack createFuguCannon() {
      ItemStack cannon = new ItemStack(Material.CROSSBOW);
      ItemMeta meta = cannon.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("fugu_cannon");
         if (weaponName.isEmpty()) {
            weaponName = "§e§lФуга Пушка";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(2);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.fuguCannonKey, PersistentDataType.STRING, "fugu_cannon");
         this.plugin.applyWeaponLore(meta, "fugu_cannon");
         cannon.setItemMeta(meta);
      }

      return cannon;
   }

   public boolean isFuguCannon(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.fuguCannonKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public int getCooldownSeconds() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.cooldown_seconds", 12);
   }

   public int getProjectileCount() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.projectile_count", 3);
   }

   public double getFlightDistance() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.flight_distance", 12.0);
   }

   public double getFlightSpeed() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.flight_speed", 0.9);
   }

   public double getSpread() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.spread", 0.18);
   }

   public double getImpactDamage() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.impact_damage", 8.0);
   }

   public double getTouchRadius() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.touch_radius", 1.4);
   }

   public int getPoisonDurationSeconds() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.poison_duration_seconds", 6);
   }

   public int getPoisonAmplifier() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.poison_amplifier", 1);
   }

   public int getHitCooldownSeconds() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.hit_cooldown_seconds", 1);
   }

   public double getFishHealth() {
      return this.plugin.getConfig().getDouble("weapons.fugu_cannon.fish_health", 100.0);
   }

   public int getLifetimeSeconds() {
      return this.plugin.getConfig().getInt("weapons.fugu_cannon.lifetime_seconds", 20);
   }
}
