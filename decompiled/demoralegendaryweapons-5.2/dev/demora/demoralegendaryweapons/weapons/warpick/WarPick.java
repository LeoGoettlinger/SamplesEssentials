package dev.demora.demoralegendaryweapons.weapons.warpick;

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

public class WarPick {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey warPickKey;

   public WarPick(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.warPickKey = new NamespacedKey(plugin, "war_pick");
   }

   public ItemStack createWarPick() {
      ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
      ItemMeta meta = pick.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("war_pick");
         if (weaponName.isEmpty()) {
            weaponName = "§c§lКирка войны";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(1);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.war_pick.damage", 8.0);
         double attackSpeed = this.plugin.getConfig().getDouble("weapons.war_pick.attack_speed", -2.8);
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

         meta.getPersistentDataContainer().set(this.warPickKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "war_pick");
         pick.setItemMeta(meta);
      }

      return pick;
   }

   public boolean isWarPick(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.warPickKey, PersistentDataType.BOOLEAN) : false;
   }
}
