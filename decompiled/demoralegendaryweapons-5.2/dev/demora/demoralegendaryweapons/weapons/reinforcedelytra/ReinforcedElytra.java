package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ReinforcedElytra {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey elytraKey;
   private final Map<UUID, Long> boostCooldowns;
   private final Map<UUID, Long> explosionLandingCooldowns;
   private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-def0-123456789abc");

   public ReinforcedElytra(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.elytraKey = new NamespacedKey(plugin, "reinforced_elytra");
      this.boostCooldowns = new HashMap<>();
      this.explosionLandingCooldowns = new HashMap<>();
   }

   public ItemStack createReinforcedElytra() {
      ItemStack elytra = new ItemStack(Material.ELYTRA);
      ItemMeta meta = elytra.getItemMeta();
      if (meta != null) {
         String name = this.plugin.getLanguageManager().getWeaponName("reinforced_elytra");
         if (name.isEmpty()) {
            name = "§9§lУсиленная элитра";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
         meta.setCustomModelData(1);
         meta.setUnbreakable(true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         double armorBonus = this.plugin.getConfig().getDouble("weapons.reinforced_elytra.armor_bonus", 4.0);
         AttributeModifier armorModifier = new AttributeModifier(ARMOR_MODIFIER_UUID, "generic.armor", armorBonus, Operation.ADD_NUMBER, EquipmentSlot.CHEST);
         if (AttributeCompat.getArmor() != null) {
            meta.addAttributeModifier(AttributeCompat.getArmor(), armorModifier);
         }

         meta.getPersistentDataContainer().set(this.elytraKey, PersistentDataType.STRING, "reinforced_elytra");
         this.plugin.applyWeaponLore(meta, "reinforced_elytra");
         elytra.setItemMeta(meta);
      }

      return elytra;
   }

   public boolean isReinforcedElytra(ItemStack item) {
      if (item != null && item.getType() == Material.ELYTRA && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && meta.getPersistentDataContainer().has(this.elytraKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public boolean isOnBoostCooldown(UUID playerId) {
      if (!this.boostCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.boostCooldowns.get(playerId);
         long cooldown = this.plugin.getConfig().getLong("weapons.reinforced_elytra.boost_cooldown_seconds", 3L) * 1000L;
         return System.currentTimeMillis() - lastUse < cooldown;
      }
   }

   public long getRemainingBoostCooldownSeconds(UUID playerId) {
      if (!this.boostCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.boostCooldowns.get(playerId);
         long cooldown = this.plugin.getConfig().getLong("weapons.reinforced_elytra.boost_cooldown_seconds", 3L) * 1000L;
         long remaining = cooldown - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public void setBoostCooldown(UUID playerId) {
      this.boostCooldowns.put(playerId, System.currentTimeMillis());
   }

   public void removePlayerData(UUID playerId) {
      this.boostCooldowns.remove(playerId);
      this.explosionLandingCooldowns.remove(playerId);
   }

   public boolean isOnExplosionLandingCooldown(UUID playerId) {
      if (!this.explosionLandingCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.explosionLandingCooldowns.get(playerId);
         long cooldown = this.plugin.getConfig().getLong("weapons.reinforced_elytra.explosion_landing_cooldown_seconds", 5L) * 1000L;
         return System.currentTimeMillis() - lastUse < cooldown;
      }
   }

   public void setExplosionLandingCooldown(UUID playerId) {
      this.explosionLandingCooldowns.put(playerId, System.currentTimeMillis());
   }

   public long getRemainingExplosionLandingCooldownSeconds(UUID playerId) {
      if (!this.explosionLandingCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.explosionLandingCooldowns.get(playerId);
         long cooldown = this.plugin.getConfig().getLong("weapons.reinforced_elytra.explosion_landing_cooldown_seconds", 5L) * 1000L;
         long remaining = cooldown - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public double getBoostPower() {
      return this.plugin.getConfig().getDouble("weapons.reinforced_elytra.boost_power", 2.5);
   }

   public double getMinTriggerSpeed() {
      return this.plugin.getConfig().getDouble("weapons.reinforced_elytra.min_trigger_speed", 0.3);
   }

   public double getMaxSpeedForExplosion() {
      return this.plugin.getConfig().getDouble("weapons.reinforced_elytra.max_speed_for_explosion", 3.0);
   }

   public float getMinExplosionPower() {
      return (float)this.plugin.getConfig().getDouble("weapons.reinforced_elytra.min_explosion_power", 0.5);
   }

   public float getMaxExplosionPower() {
      return (float)this.plugin.getConfig().getDouble("weapons.reinforced_elytra.max_explosion_power", 4.0);
   }

   public boolean isExplosionBreakBlocks() {
      return this.plugin.getConfig().getBoolean("weapons.reinforced_elytra.explosion_break_blocks", false);
   }

   public boolean isExplosionSetFire() {
      return false;
   }

   public boolean isDamagePlayer() {
      return false;
   }
}
