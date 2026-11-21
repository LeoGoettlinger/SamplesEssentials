package com.magmaguy.elitemobs.config.potioneffects;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectsConfigFields {
   private final String fileName;
   private final boolean isEnabled;
   private final String name;
   private final int onHitDuration;
   private final double value;
   private final HashMap<String, Object> additionalConfigOptions = new HashMap<>();
   private PotionEffectType potionEffectType;
   private FileConfiguration configuration;

   public PotionEffectsConfigFields(String fileName, boolean isEnabled, String name, int onHitDuration, double value) {
      this.fileName = fileName + ".yml";
      this.isEnabled = isEnabled;
      this.name = name;
      this.onHitDuration = onHitDuration;
      this.value = value;
   }

   public PotionEffectsConfigFields(FileConfiguration fileConfiguration, File file) {
      this.fileName = file.getName();
      this.isEnabled = fileConfiguration.getBoolean("isEnabled");
      this.name = TranslationsConfig.add(this.fileName, "name", fileConfiguration.getString("name"));
      this.onHitDuration = fileConfiguration.getInt("onHitDuration");
      this.value = fileConfiguration.getDouble("value");
      this.potionEffectType = PotionEffectType.getByName(this.fileName.toUpperCase(Locale.ROOT));
      this.configuration = fileConfiguration;
   }

   public void generateConfigDefaults(FileConfiguration fileConfiguration) {
      fileConfiguration.addDefault("isEnabled", this.isEnabled);
      fileConfiguration.addDefault("name", this.name);
      fileConfiguration.addDefault("onHitDuration", this.onHitDuration);
      fileConfiguration.addDefault("value", this.value);
      if (!this.additionalConfigOptions.isEmpty()) {
         fileConfiguration.addDefaults(this.additionalConfigOptions);
      }
   }

   public String getFileName() {
      return this.fileName;
   }

   public boolean isEnabled() {
      return this.isEnabled;
   }

   public String getName() {
      return this.name;
   }

   public PotionEffectType getPotionEffectType() {
      return this.potionEffectType;
   }

   public Map<String, Object> getAdditionalConfigOptions() {
      return this.additionalConfigOptions;
   }

   public FileConfiguration getConfiguration() {
      return this.configuration;
   }

   public double getValue() {
      return this.value;
   }

   public int getOnHitDuration() {
      return this.onHitDuration;
   }
}
