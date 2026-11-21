package dev.demora.demoralegendaryweapons.weapons.sculkweaverslantern;

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

public class SculkweaversLantern {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey lanternKey;

   public SculkweaversLantern(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.lanternKey = new NamespacedKey(plugin, "sculkweavers_lantern");
   }

   public ItemStack createSculkweaversLantern() {
      ItemStack lantern = new ItemStack(Material.FISHING_ROD);
      ItemMeta meta = lantern.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("sculkweavers_lantern");
         if (weaponName.isEmpty()) {
            weaponName = "§b§lСкалковый фонарь";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(5);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.sculkweavers_lantern.damage", 8.0);
         double attackSpeed = this.plugin.getConfig().getDouble("weapons.sculkweavers_lantern.attack_speed", -2.8);
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

         meta.getPersistentDataContainer().set(this.lanternKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "sculkweavers_lantern");
         lantern.setItemMeta(meta);
      }

      return lantern;
   }

   public boolean isSculkweaversLantern(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.lanternKey, PersistentDataType.BOOLEAN) : false;
   }
}
