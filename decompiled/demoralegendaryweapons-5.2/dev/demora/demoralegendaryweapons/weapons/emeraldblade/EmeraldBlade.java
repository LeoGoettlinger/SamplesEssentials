package dev.demora.demoralegendaryweapons.weapons.emeraldblade;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EmeraldBlade {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey emeraldBladeKey;
   private final NamespacedKey upgradeCountKey;
   private final NamespacedKey emeraldCountKey;

   public EmeraldBlade(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.emeraldBladeKey = new NamespacedKey(plugin, "emerald_blade");
      this.upgradeCountKey = new NamespacedKey(plugin, "emerald_blade_upgrades");
      this.emeraldCountKey = new NamespacedKey(plugin, "emerald_blade_emeralds");
   }

   public ItemStack createEmeraldBlade() {
      return this.createEmeraldBladeWithEmeralds(0);
   }

   public ItemStack createEmeraldBladeWithEmeralds(int emeraldCount) {
      ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = sword.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("emerald_blade");
         if (weaponName.isEmpty()) {
            weaponName = "§a§lИзумрудный клинок";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(3);
         meta.setUnbreakable(true);
         double baseDamage = this.plugin.getConfig().getDouble("weapons.emerald_blade.base_damage", 7.0);
         double damagePerUpgrade = this.plugin.getConfig().getDouble("weapons.emerald_blade.damage_per_upgrade", 2.0);
         double maxDamage = this.plugin.getConfig().getDouble("weapons.emerald_blade.max_damage", 40.0);
         int emeraldsPerUpgrade = this.plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_upgrade", 64);
         int upgradeCount = emeraldsPerUpgrade > 0 ? emeraldCount / emeraldsPerUpgrade : 0;
         double totalDamage = baseDamage + upgradeCount * damagePerUpgrade;
         if (totalDamage > maxDamage) {
            totalDamage = maxDamage;
         }

         AttributeModifier damageModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_damage", totalDamage, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.emerald_blade.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.emeraldBladeKey, PersistentDataType.BOOLEAN, true);
         container.set(this.upgradeCountKey, PersistentDataType.INTEGER, upgradeCount);
         container.set(this.emeraldCountKey, PersistentDataType.INTEGER, emeraldCount);
         this.plugin.applyWeaponLore(meta, "emerald_blade");
         sword.setItemMeta(meta);
      }

      return sword;
   }

   public boolean isEmeraldBlade(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta.getPersistentDataContainer().has(this.emeraldBladeKey, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   public int getUpgradeCount(ItemStack item) {
      if (!this.isEmeraldBlade(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().getOrDefault(this.upgradeCountKey, PersistentDataType.INTEGER, 0);
      }
   }

   public int getEmeraldCount(ItemStack item) {
      if (!this.isEmeraldBlade(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().getOrDefault(this.emeraldCountKey, PersistentDataType.INTEGER, 0);
      }
   }

   public NamespacedKey getEmeraldBladeKey() {
      return this.emeraldBladeKey;
   }

   public NamespacedKey getUpgradeCountKey() {
      return this.upgradeCountKey;
   }
}
