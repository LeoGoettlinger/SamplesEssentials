package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.config.EliteMobPowersConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public final class EliteMobProperties extends PluginMobProperties {
   private static Map<EntityType, EliteMobProperties> eliteMobData = Map.of();
   private final MobPropertiesConfigFields config;
   private final HashSet<PowersConfigFields> validMajorPowers;
   private final HashSet<PowersConfigFields> validDefensivePowers;
   private final HashSet<PowersConfigFields> validOffensivePowers;
   private final HashSet<PowersConfigFields> validMiscellaneousPowers;

   private EliteMobProperties(MobPropertiesConfigFields config) {
      super(config.isEnabled(), config.getName(), config.getEntityType(), config.getDefaultMaxHealth(), config.getBaseDamage());
      this.config = config;
      this.validMajorPowers = EliteMobPowersConfig.getMajorPowerFields(this.entityType);
      this.validDefensivePowers = EliteMobPowersConfig.getDefensivePowerFields();
      this.validOffensivePowers = EliteMobPowersConfig.getOffensivePowerFields();
      this.validMiscellaneousPowers = EliteMobPowersConfig.getMiscellaneousPowerFields();
      HashSet<PowersConfigFields> disabledPowers = EliteMobPowersConfig.getDisabledPowerFields(this.entityType);
      this.validMajorPowers.removeAll(disabledPowers);
      this.validDefensivePowers.removeAll(disabledPowers);
      this.validOffensivePowers.removeAll(disabledPowers);
      this.validMiscellaneousPowers.removeAll(disabledPowers);
   }

   public static void initializeEliteMobValues() {
      Map<EntityType, EliteMobProperties> loaded = new EnumMap<>(EntityType.class);
      MobPropertiesConfig.getMobProperties().forEach((type, config) -> loaded.put(type, new EliteMobProperties(config)));
      eliteMobData = Collections.unmodifiableMap(loaded);
   }

   public static void shutdown() {
      eliteMobData = Map.of();
   }

   public static boolean isValidEliteMobType(Entity entity) {
      return entity instanceof Mob && isValidEliteMobType(entity.getType());
   }

   public static boolean isValidEliteMobType(EntityType entityType) {
      EliteMobProperties properties = getPluginData(entityType);
      return properties != null && properties.isEnabled();
   }

   public static EliteMobProperties getPluginData(EntityType entityType) {
      return entityType == null ? null : eliteMobData.get(entityType);
   }

   public static double getBaselineDamage(EntityType entityType, EliteEntity eliteEntity) {
      if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat()) {
         return MobCombatSettingsConfig.getNormalizedBaselineDamage();
      } else {
         EliteMobProperties properties = getPluginData(entityType);
         if (properties == null) {
            throw new IllegalArgumentException("No elite properties for " + entityType);
         } else {
            return properties.baseDamage;
         }
      }
   }

   public static EliteMobProperties getPluginData(Entity entity) {
      return entity == null ? null : getPluginData(entity.getType());
   }

   public static HashSet<EntityType> getValidMobTypes() {
      return new HashSet<>(eliteMobData.keySet());
   }

   public String getBehavior() {
      return this.config.getBehavior();
   }

   public EliteMindBodyLocomotion getLocomotion() {
      return this.config.getLocomotion();
   }

   public HashSet<PowersConfigFields> getValidMajorPowers() {
      return new HashSet<>(this.validMajorPowers);
   }

   public HashSet<PowersConfigFields> getValidDefensivePowers() {
      return new HashSet<>(this.validDefensivePowers);
   }

   public HashSet<PowersConfigFields> getValidOffensivePowers() {
      return new HashSet<>(this.validOffensivePowers);
   }

   public HashSet<PowersConfigFields> getValidMiscellaneousPowers() {
      return new HashSet<>(this.validMiscellaneousPowers);
   }

   public void addMajorPower(String powerName) {
      PowersConfigFields power = PowersConfig.getPower(powerName);
      if (power != null && power.isEnabled()) {
         this.validMajorPowers.add(power);
      }
   }

   public void removeOffensivePower(String filename) {
      this.validOffensivePowers.remove(PowersConfig.getPower(filename));
   }

   public void removeDefensivePower(String filename) {
      this.validDefensivePowers.remove(PowersConfig.getPower(filename));
   }
}
