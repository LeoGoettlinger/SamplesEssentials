package dev.demora.demoralegendaryweapons.weapons.chainsawsword;

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

public class ChainsawSword {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey chainsawSwordKey;
   private final NamespacedKey curseKey;

   public ChainsawSword(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.chainsawSwordKey = new NamespacedKey(plugin, "chainsaw_sword");
      this.curseKey = new NamespacedKey(plugin, "chainsaw_curse");
   }

   public ItemStack createChainsawSword() {
      ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = sword.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("chainsaw_sword");
         if (weaponName.isEmpty()) {
            weaponName = "§c§lМеч-пила";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(8);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.chainsaw_sword.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.chainsaw_sword.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.chainsawSwordKey, PersistentDataType.BOOLEAN, true);
         container.remove(this.curseKey);
         this.plugin.applyWeaponLore(meta, "chainsaw_sword");
         sword.setItemMeta(meta);
      }

      return sword;
   }

   public boolean isChainsawSword(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         return container.has(this.chainsawSwordKey, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   public boolean hasCurse(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return (Boolean)meta.getPersistentDataContainer().getOrDefault(this.curseKey, PersistentDataType.BOOLEAN, false);
      } else {
         return false;
      }
   }

   public void setCurse(ItemStack item, boolean value) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer container = meta.getPersistentDataContainer();
         if (value) {
            container.set(this.curseKey, PersistentDataType.BOOLEAN, true);
         } else {
            container.remove(this.curseKey);
         }

         item.setItemMeta(meta);
      }
   }
}
