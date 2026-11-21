package dev.demora.demoralegendaryweapons.weapons.elementalstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ElementalStaff {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey staffKey;
   private final NamespacedKey elementKey;
   private final List<String> elementNames = Arrays.asList("Стихия воды", "Стихия яда", "Стихия хаоса", "Стихия огня", "Стихия тьмы");
   private final List<String> elementColors = Arrays.asList("§9", "§a", "§c", "§e", "§5");
   private final int[] sphereModelData = new int[]{22, 23, 24, 25, 26};

   public ElementalStaff(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.staffKey = new NamespacedKey(plugin, "elemental_staff");
      this.elementKey = new NamespacedKey(plugin, "elemental_staff_element");
   }

   public ItemStack createElementalStaff() {
      ItemStack staff = new ItemStack(Material.STONE_AXE);
      ItemMeta meta = staff.getItemMeta();
      if (meta != null) {
         meta.setDisplayName("§c§lПосох стихий");
         meta.setCustomModelData(7);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.staffKey, PersistentDataType.STRING, "elemental_staff");
         meta.getPersistentDataContainer().set(this.elementKey, PersistentDataType.INTEGER, 0);
         staff.setItemMeta(meta);
      }

      return staff;
   }

   public boolean isElementalStaff(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.staffKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public int getElementIndex(ItemStack item) {
      if (!this.isElementalStaff(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().getOrDefault(this.elementKey, PersistentDataType.INTEGER, 0);
      }
   }

   public int cycleElement(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return 0;
      } else {
         int current = (Integer)meta.getPersistentDataContainer().getOrDefault(this.elementKey, PersistentDataType.INTEGER, 0);
         int next = (current + 1) % this.elementNames.size();
         meta.getPersistentDataContainer().set(this.elementKey, PersistentDataType.INTEGER, next);
         item.setItemMeta(meta);
         return next;
      }
   }

   public String getElementName(int index) {
      return index >= 0 && index < this.elementNames.size() ? this.elementColors.get(index) + "§l" + this.elementNames.get(index) : "";
   }

   public int getElementCount() {
      return this.elementNames.size();
   }

   public int[] getSphereModelData() {
      return this.sphereModelData;
   }

   public boolean isImplemented(int index) {
      return index >= 0 && index < this.elementNames.size();
   }

   public int getThrowCooldownSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.throw_cooldown_seconds", 30);
   }

   public int getSwitchCooldownSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.switch_cooldown_seconds", 5);
   }

   public double getChaosRadius() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.chaos.radius", 4.0);
   }

   public double getChaosDamage() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.chaos.damage", 15.0);
   }

   public double getProjectileSpeed() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.projectile_speed", 1.2);
   }

   public double getDarknessRadius() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.darkness.radius", 3.0);
   }

   public double getDarknessDamage() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.darkness.damage", 10.0);
   }

   public int getDarknessBlindnessSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.darkness.blindness_seconds", 5);
   }

   public int getDarknessWitherSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.darkness.wither_seconds", 7);
   }

   public int getWaterColumnHeight() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.water.column_height", 6);
   }

   public int getWaterColumnWidth() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.water.column_width", 3);
   }

   public double getWaterLaunchPower() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.water.launch_power", 1.2);
   }

   public int getWaterDrowningSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.water.drowning_seconds", 5);
   }

   public double getWaterDrowningDamage() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.water.drowning_damage", 2.0);
   }

   public double getPoisonRadius() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.poison.radius", 2.0);
   }

   public int getPoisonCloudSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.poison.cloud_seconds", 6);
   }

   public int getPoisonEffectSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.poison.poison_seconds", 6);
   }

   public int getPoisonAmplifier() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.poison.poison_amplifier", 1);
   }

   public double getPoisonImpactDamage() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.poison.impact_damage", 4.0);
   }

   public double getFireRadius() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.fire.radius", 3.0);
   }

   public double getFireDamage() {
      return this.plugin.getConfig().getDouble("weapons.elemental_staff.fire.damage", 8.0);
   }

   public int getFireBurnSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.fire.burn_seconds", 6);
   }

   public int getFireBlockLifetimeSeconds() {
      return this.plugin.getConfig().getInt("weapons.elemental_staff.fire.fire_block_lifetime_seconds", 8);
   }

   public boolean isFireBlocksEnabled() {
      return this.plugin.getConfig().getBoolean("weapons.elemental_staff.fire.place_fire_blocks", true);
   }
}
