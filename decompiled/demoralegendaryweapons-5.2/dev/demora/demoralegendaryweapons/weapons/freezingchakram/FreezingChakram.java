package dev.demora.demoralegendaryweapons.weapons.freezingchakram;

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

public class FreezingChakram {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey chakramKey;
   private final NamespacedKey additionalChakramKey;

   public FreezingChakram(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.chakramKey = new NamespacedKey(plugin, "freezing_chakram");
      this.additionalChakramKey = new NamespacedKey(plugin, "additional_chakram");
   }

   public ItemStack createFreezingChakram() {
      ItemStack chakram = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = chakram.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("freezing_chakram");
         if (weaponName.isEmpty()) {
            weaponName = "§b§lЛедяные сюрикены";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(13);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.freezing_chakram.damage", 9.0);
         double attackSpeed = this.plugin.getConfig().getDouble("weapons.freezing_chakram.attack_speed", -2.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         AttributeModifier speedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), speedModifier);
         }

         meta.getPersistentDataContainer().set(this.chakramKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "freezing_chakram");
         chakram.setItemMeta(meta);
      }

      return chakram;
   }

   public ItemStack createAdditionalChakram() {
      ItemStack chakram = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = chakram.getItemMeta();
      if (meta != null) {
         String additionalName = this.plugin.getMessage("weapon-names.freezing_chakram_additional");
         if (additionalName == null || additionalName.isEmpty() || additionalName.equals("weapon-names.freezing_chakram_additional")) {
            additionalName = "§b§lДополнительный сюрикен";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(additionalName));
         meta.setCustomModelData(13);
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.additionalChakramKey, PersistentDataType.BOOLEAN, true);
         chakram.setItemMeta(meta);
      }

      return chakram;
   }

   public boolean isFreezingChakram(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.chakramKey, PersistentDataType.BOOLEAN) : false;
   }

   public boolean isAdditionalChakram(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.additionalChakramKey, PersistentDataType.BOOLEAN)
         : false;
   }
}
