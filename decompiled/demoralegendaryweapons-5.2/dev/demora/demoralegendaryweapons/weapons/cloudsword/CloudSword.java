package dev.demora.demoralegendaryweapons.weapons.cloudsword;

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
import org.bukkit.persistence.PersistentDataType;

public class CloudSword {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey cloudSwordKey;

   public CloudSword(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.cloudSwordKey = new NamespacedKey(plugin, "cloud_sword");
   }

   public ItemStack createCloudSword() {
      ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = sword.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("cloud_sword");
         if (weaponName.isEmpty()) {
            weaponName = "§f§lОблачный меч";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(7);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.cloud_sword.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.cloud_sword.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         double speedBoost = this.plugin.getConfig().getDouble("weapons.cloud_sword.speed_boost", 0.15);
         AttributeModifier speedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.movement_speed", speedBoost, Operation.ADD_SCALAR, EquipmentSlot.HAND
         );
         if (AttributeCompat.getMovementSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getMovementSpeed(), speedModifier);
         }

         meta.getPersistentDataContainer().set(this.cloudSwordKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "cloud_sword");
         sword.setItemMeta(meta);
      }

      return sword;
   }

   public boolean isCloudSword(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.cloudSwordKey, PersistentDataType.BOOLEAN) : false;
   }
}
