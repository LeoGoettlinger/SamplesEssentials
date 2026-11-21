package dev.demora.demoralegendaryweapons.weapons.withersickles.rework;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AwakenedWitherSickles {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey awakenedSickleKey;
   private final NamespacedKey additionalAwakenedSickleKey;

   public AwakenedWitherSickles(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.awakenedSickleKey = new NamespacedKey(plugin, "awakened_wither_sickle");
      this.additionalAwakenedSickleKey = new NamespacedKey(plugin, "additional_awakened_sickle");
   }

   public ItemStack createAwakenedWitherSickle() {
      ItemStack sickle = new ItemStack(Material.STONE_HOE);
      ItemMeta meta = sickle.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("awakened_wither_sickles");
         if (weaponName.isEmpty()) {
            weaponName = "§6§lПробуждённые серпы визера";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(6);
         meta.setUnbreakable(true);
         double damage = this.plugin.getReworkConfig().getDouble("awakened_wither_sickles.damage", 13.0);
         double attackSpeed = this.plugin.getReworkConfig().getDouble("awakened_wither_sickles.attack_speed", -1.6);
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

         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.awakenedSickleKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "awakened_wither_sickles");
         sickle.setItemMeta(meta);
      }

      return sickle;
   }

   public ItemStack createAdditionalAwakenedSickle() {
      ItemStack sickle = new ItemStack(Material.STONE_HOE);
      ItemMeta meta = sickle.getItemMeta();
      if (meta != null) {
         meta.displayName(Component.text("§6§lДополнительный пробуждённый серп"));
         meta.setCustomModelData(6);
         meta.setUnbreakable(true);
         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.additionalAwakenedSickleKey, PersistentDataType.BOOLEAN, true);
         sickle.setItemMeta(meta);
      }

      return sickle;
   }

   public boolean isAwakenedWitherSickle(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.awakenedSickleKey, PersistentDataType.BOOLEAN)
         : false;
   }

   public boolean isAdditionalAwakenedSickle(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.additionalAwakenedSickleKey, PersistentDataType.BOOLEAN)
         : false;
   }

   public NamespacedKey getAwakenedSickleKey() {
      return this.awakenedSickleKey;
   }

   public NamespacedKey getAdditionalAwakenedSickleKey() {
      return this.additionalAwakenedSickleKey;
   }

   public boolean isOnThrowCooldown(Player player) {
      return false;
   }

   public long getRemainingThrowCooldown(Player player) {
      return 0L;
   }
}
