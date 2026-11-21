package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class WitherSickles {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey witherSickleKey;
   private final NamespacedKey additionalSickleKey;

   public WitherSickles(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.witherSickleKey = new NamespacedKey(plugin, "wither_sickle");
      this.additionalSickleKey = new NamespacedKey(plugin, "additional_sickle");
   }

   public ItemStack createWitherSickle() {
      ItemStack sickle = new ItemStack(Material.STONE_HOE);
      ItemMeta meta = sickle.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("wither_sickles");
         if (weaponName.isEmpty()) {
            weaponName = "§c§lСерпы визера";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(3);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.wither_sickles.damage", 11.0);
         double attackSpeed = this.plugin.getConfig().getDouble("weapons.wither_sickles.attack_speed", -1.6);
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

         meta.getPersistentDataContainer().set(this.witherSickleKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "wither_sickles");
         sickle.setItemMeta(meta);
      }

      return sickle;
   }

   public ItemStack createAdditionalSickle() {
      ItemStack sickle = new ItemStack(Material.STONE_HOE);
      ItemMeta meta = sickle.getItemMeta();
      if (meta != null) {
         meta.displayName(Component.text("§c§lДополнительный серп"));
         meta.setCustomModelData(3);
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.additionalSickleKey, PersistentDataType.BOOLEAN, true);
         sickle.setItemMeta(meta);
      }

      return sickle;
   }

   public boolean isWitherSickle(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.witherSickleKey, PersistentDataType.BOOLEAN) : false;
   }

   public boolean isAdditionalSickle(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.additionalSickleKey, PersistentDataType.BOOLEAN)
         : false;
   }

   public boolean isOnThrowCooldown(Player player) {
      return false;
   }

   public long getRemainingThrowCooldown(Player player) {
      return 0L;
   }
}
