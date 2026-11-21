package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ReworkElytra {
   private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey elytraKey;
   private final Map<UUID, Long> boostCooldowns;
   private final Map<UUID, Long> explosionLandingCooldowns;
   private FileConfiguration reworkConfig;

   public ReworkElytra(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.elytraKey = new NamespacedKey(plugin, "rework_elytra");
      this.boostCooldowns = new HashMap<>();
      this.explosionLandingCooldowns = new HashMap<>();
      this.loadReworkConfig();
   }

   private void loadReworkConfig() {
      File reworkFile = new File(this.plugin.getDataFolder(), "rework.yml");
      if (!reworkFile.exists()) {
         try {
            InputStream defaultConfig = this.plugin.getResource("rework.yml");
            if (defaultConfig != null) {
               Files.copy(defaultConfig, reworkFile.toPath());
            }
         } catch (Exception var3) {
            this.plugin.getLogger().severe("Failed to create rework.yml: " + var3.getMessage());
         }
      }

      this.reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
   }

   public void reloadConfig() {
      this.loadReworkConfig();
   }

   private String path(String key) {
      return "rework_elytra." + key;
   }

   public ItemStack createReworkElytra() {
      ItemStack elytra = new ItemStack(Material.ELYTRA);
      ItemMeta meta = elytra.getItemMeta();
      if (meta != null) {
         String name = this.plugin.getLanguageManager().getWeaponName("rework_elytra");
         if (name.isEmpty()) {
            name = "§4§lАлые элитры";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
         meta.setCustomModelData(2);
         meta.setUnbreakable(true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         double armorBonus = this.reworkConfig.getDouble(this.path("armor_bonus"), 4.0);
         AttributeModifier armorModifier = new AttributeModifier(ARMOR_MODIFIER_UUID, "generic.armor", armorBonus, Operation.ADD_NUMBER, EquipmentSlot.CHEST);
         if (AttributeCompat.getArmor() != null) {
            meta.addAttributeModifier(AttributeCompat.getArmor(), armorModifier);
         }

         meta.getPersistentDataContainer().set(this.elytraKey, PersistentDataType.STRING, "rework_elytra");
         this.plugin.applyWeaponLore(meta, "rework_elytra");
         elytra.setItemMeta(meta);
      }

      return elytra;
   }

   public boolean isReworkElytra(ItemStack item) {
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
         long cooldown = this.reworkConfig.getLong(this.path("boost_cooldown_seconds"), 3L) * 1000L;
         return System.currentTimeMillis() - lastUse < cooldown;
      }
   }

   public long getRemainingBoostCooldownSeconds(UUID playerId) {
      if (!this.boostCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.boostCooldowns.get(playerId);
         long cooldown = this.reworkConfig.getLong(this.path("boost_cooldown_seconds"), 3L) * 1000L;
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
         long cooldown = this.reworkConfig.getLong(this.path("explosion_landing_cooldown_seconds"), 5L) * 1000L;
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
         long cooldown = this.reworkConfig.getLong(this.path("explosion_landing_cooldown_seconds"), 5L) * 1000L;
         long remaining = cooldown - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public double getBoostPower() {
      return this.reworkConfig.getDouble(this.path("boost_power"), 2.5);
   }

   public double getMinTriggerSpeed() {
      return this.reworkConfig.getDouble(this.path("min_trigger_speed"), 0.3);
   }

   public double getMaxSpeedForExplosion() {
      return this.reworkConfig.getDouble(this.path("max_speed_for_explosion"), 3.0);
   }

   public float getMinExplosionPower() {
      return (float)this.reworkConfig.getDouble(this.path("min_explosion_power"), 0.5);
   }

   public float getMaxExplosionPower() {
      return (float)this.reworkConfig.getDouble(this.path("max_explosion_power"), 4.0);
   }

   public boolean isExplosionBreakBlocks() {
      return this.reworkConfig.getBoolean(this.path("explosion_break_blocks"), false);
   }

   public boolean isExplosionSetFire() {
      return false;
   }

   public boolean isDamagePlayer() {
      return false;
   }

   public int getNetherWartSpreadRadius() {
      return this.reworkConfig.getInt(this.path("nether_wart_spread.radius"), 5);
   }

   public int getNetherWartBlocksPerTick() {
      return this.reworkConfig.getInt(this.path("nether_wart_spread.blocks_per_tick"), 3);
   }

   public int getNetherWartTickDelay() {
      return this.reworkConfig.getInt(this.path("nether_wart_spread.tick_delay"), 2);
   }

   public double getNetherWartReplaceChance() {
      return this.reworkConfig.getDouble(this.path("nether_wart_spread.replace_chance"), 0.7);
   }

   public int getBleedDurationTicks() {
      return this.reworkConfig.getInt(this.path("bleeding.duration_seconds"), 4) * 20;
   }

   public double getBleedDamage() {
      return this.reworkConfig.getDouble(this.path("bleeding.damage_per_tick"), 2.0);
   }

   public int getCrimsonSpiritsCount() {
      return this.reworkConfig.getInt(this.path("crimson_spirits.count"), 3);
   }

   public int getCrimsonSpiritsDurationTicks() {
      return this.reworkConfig.getInt(this.path("crimson_spirits.duration_seconds"), 4) * 20;
   }

   public double getCrimsonSpiritDamage() {
      return this.reworkConfig.getDouble(this.path("crimson_spirits.damage"), 5.0);
   }

   public double getCrimsonSpiritSpeed() {
      return this.reworkConfig.getDouble(this.path("crimson_spirits.speed"), 0.4);
   }

   public double getCrimsonSpiritSpawnRadius() {
      return this.reworkConfig.getDouble(this.path("crimson_spirits.spawn_radius"), 1.5);
   }

   public NamespacedKey getElytraKey() {
      return this.elytraKey;
   }
}
