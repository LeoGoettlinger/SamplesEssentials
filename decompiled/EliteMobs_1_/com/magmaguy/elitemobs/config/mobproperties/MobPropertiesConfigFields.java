package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class MobPropertiesConfigFields {
   public static final double NATIVE_BASE_HEALTH = 0.0;
   private final String fileName;
   private final EntityType entityType;
   private final boolean isEnabled;
   private final String name;
   private final List<String> deathMessages;
   private final double baseDamage;
   private final double defaultMaxHealth;
   private final String behavior;
   private final EliteMindBodyLocomotion locomotion;

   public MobPropertiesConfigFields(
      String fileName, EntityType entityType, boolean isEnabled, String name, List<String> deathMessages, double baseDamage, double defaultMaxHealth
   ) {
      this.fileName = fileName + ".yml";
      this.entityType = entityType;
      this.isEnabled = isEnabled;
      this.name = name;
      this.deathMessages = List.copyOf(deathMessages);
      this.baseDamage = baseDamage;
      this.defaultMaxHealth = defaultMaxHealth;
      this.behavior = MobTypeDefaults.behavior(entityType);
      this.locomotion = MobTypeDefaults.locomotion(entityType);
   }

   public MobPropertiesConfigFields(FileConfiguration configuration, File file, MobPropertiesConfigFields defaults) {
      this.fileName = file.getName();
      this.entityType = defaults.entityType;
      if (!this.entityType.name().equals(configuration.getString("entityType"))) {
         throw new IllegalArgumentException("Mob properties " + this.fileName + " must use entityType " + this.entityType);
      } else {
         this.isEnabled = configuration.getBoolean("isEnabled", defaults.isEnabled);
         this.name = TranslationsConfig.add(this.fileName, "name", configuration.getString("name", defaults.name));
         List<String> configuredMessages = configuration.getStringList("deathMessages");
         if (configuredMessages.isEmpty()) {
            Logger.warn("Mob properties " + this.fileName + " has no death messages; using its default messages.");
            configuredMessages = defaults.deathMessages;
         }

         this.deathMessages = List.copyOf(TranslationsConfig.add(this.fileName, "deathMessages", configuredMessages));
         this.baseDamage = configuration.getDouble("baseDamageV2", defaults.baseDamage);
         if (Double.isFinite(this.baseDamage) && !(this.baseDamage < 0.0)) {
            this.defaultMaxHealth = defaults.defaultMaxHealth;
            String selectedBehavior = configuration.getString("behavior", defaults.behavior);
            this.behavior = selectedBehavior != null && !selectedBehavior.isBlank() && !selectedBehavior.trim().equalsIgnoreCase("native")
               ? selectedBehavior.trim()
               : null;
            this.locomotion = defaults.locomotion;
         } else {
            throw new IllegalArgumentException("Mob properties " + this.fileName + " requires finite nonnegative baseDamageV2");
         }
      }
   }

   public void generateConfigDefaults(FileConfiguration fileConfiguration) {
      fileConfiguration.addDefault("isEnabled", this.isEnabled);
      fileConfiguration.addDefault("entityType", this.entityType.toString());
      fileConfiguration.addDefault("name", this.name);
      fileConfiguration.addDefault("deathMessages", this.deathMessages);
      fileConfiguration.addDefault("baseDamageV2", this.baseDamage);
      fileConfiguration.addDefault("behavior", this.behavior == null ? "native" : this.behavior);
   }

   public String getFileName() {
      return this.fileName;
   }

   public EntityType getEntityType() {
      return this.entityType;
   }

   public boolean isEnabled() {
      return this.isEnabled;
   }

   public String getName() {
      return this.name;
   }

   public List<String> getDeathMessages() {
      return this.deathMessages;
   }

   public double getBaseDamage() {
      return this.baseDamage;
   }

   public double getDefaultMaxHealth() {
      return this.defaultMaxHealth;
   }

   public String getBehavior() {
      return this.behavior;
   }

   public EliteMindBodyLocomotion getLocomotion() {
      return this.locomotion;
   }
}
