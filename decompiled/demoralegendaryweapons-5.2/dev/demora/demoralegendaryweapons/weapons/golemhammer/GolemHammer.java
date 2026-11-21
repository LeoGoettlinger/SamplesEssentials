package dev.demora.demoralegendaryweapons.weapons.golemhammer;

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

public class GolemHammer {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey golemHammerKey;

   public GolemHammer(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.golemHammerKey = new NamespacedKey(plugin, "golem_hammer");
   }

   public ItemStack createGolemHammer() {
      ItemStack hammer = new ItemStack(Material.STONE_AXE);
      ItemMeta meta = hammer.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("golem_hammer");
         if (weaponName.isEmpty()) {
            weaponName = "§7§lМолот голема";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(3);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.golem_hammer.attribute_damage", 12.0);
         double attackSpeed = this.plugin.getConfig().getDouble("weapons.golem_hammer.attack_speed", -3.0);
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
         container.set(this.golemHammerKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "golem_hammer");
         hammer.setItemMeta(meta);
      }

      return hammer;
   }

   public boolean isGolemHammer(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.golemHammerKey, PersistentDataType.BOOLEAN) : false;
   }

   public NamespacedKey getGolemHammerKey() {
      return this.golemHammerKey;
   }
}
