package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.powers.scripts.loading.ScriptValueNormalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

public class PowersConfigFields extends CustomConfigFields {
   private String effect;
   private int powerCooldown = 0;
   private int globalCooldown = 0;
   private ConfigurationSection scripts = null;
   private Map<String, Object> rawScripts = new HashMap<>();
   private List<EliteScriptBlueprint> eliteScriptBlueprints = new ArrayList<>();
   private Class<? extends ElitePower> elitePowerClass = null;
   private PowersConfigFields.PowerType powerType;

   public PowersConfigFields(
      String fileName, boolean isEnabled, String material, Class<? extends ElitePower> elitePowerClass, PowersConfigFields.PowerType powerType
   ) {
      super(fileName, isEnabled);
      this.effect = material;
      this.elitePowerClass = elitePowerClass;
      this.powerType = powerType;
   }

   public PowersConfigFields(String fileName, boolean isEnabled, String material, Map<String, Object> rawScripts, PowersConfigFields.PowerType powerType) {
      super(fileName, isEnabled);
      this.effect = material;
      this.rawScripts = rawScripts;
      this.powerType = powerType;
   }

   public PowersConfigFields(
      String fileName,
      boolean isEnabled,
      String material,
      int powerCooldown,
      int globalCooldown,
      Class<? extends ElitePower> elitePowerClass,
      PowersConfigFields.PowerType powerType
   ) {
      super(fileName, isEnabled);
      this.effect = material;
      this.powerCooldown = powerCooldown;
      this.globalCooldown = globalCooldown;
      this.elitePowerClass = elitePowerClass;
      this.powerType = powerType;
   }

   public PowersConfigFields(String fileName, boolean isEnabled) {
      super(fileName, isEnabled);
   }

   protected static Map<String, Object> addScriptEntry(
      String scriptName, List<String> events, List<String> conditions, List<Map<String, Object>> actions, Map<String, Object> cooldowns
   ) {
      Map<String, Object> defaults = new HashMap<>();
      defaults.put("eliteScript." + scriptName + ".Events", events);
      defaults.put("eliteScript." + scriptName + ".Conditions", conditions);
      defaults.put("eliteScript." + scriptName + ".Actions", actions);
      defaults.put("eliteScript." + scriptName + ".Cooldowns", cooldowns);
      return defaults;
   }

   public void setEffect(String configured) {
      if (configured != null && !configured.isEmpty()) {
         Material material = null;

         try {
            material = Material.valueOf(configured);
         } catch (IllegalArgumentException var5) {
         }

         if (material != null && material.isItem()) {
            this.effect = configured;
         } else {
            try {
               Particle particle = Particle.valueOf(configured);
               if (particle.getDataType() != Void.class) {
                  throw new IllegalArgumentException("particle requires " + particle.getDataType().getSimpleName() + " data");
               }

               this.effect = configured;
            } catch (IllegalArgumentException var4) {
               this.effect = null;
               Logger.warn("Disabled invalid power trail '" + configured + "' in " + this.getFilename() + ": " + var4.getMessage());
            }
         }
      } else {
         this.effect = null;
      }
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.setEffect(this.processString("effect", this.effect, null, false));
      this.powerCooldown = this.processInt("powerCooldown", this.powerCooldown, 0, false);
      this.globalCooldown = this.processInt("globalCooldown", this.globalCooldown, 0, false);
      this.rawScripts = this.processMap("eliteScript", this.rawScripts);
      this.scripts = this.processConfigurationSection("eliteScript", this.rawScripts);
      this.powerType = this.processEnum("powerType", this.powerType, null, PowersConfigFields.PowerType.class, false);
      this.processAdditionalFields();
   }

   public void initializeScripts() {
      try {
         Map<String, Object> mergedScripts = ScriptValueNormalizer.normalizeSection(this.scripts);
         if (!mergedScripts.isEmpty()) {
            this.eliteScriptBlueprints = EliteScriptBlueprint.parseBossScripts(mergedScripts, this);
         } else {
            this.eliteScriptBlueprints = new ArrayList<>();
         }
      } catch (Exception var2) {
         Logger.warn("You have a script with invalid data! Script in " + this.filename + " is not valid.");
         var2.printStackTrace();
      }
   }

   public void processAdditionalFields() {
   }

   public static boolean isMajorPowerType(PowersConfigFields.PowerType powerType) {
      return powerType != null && powerType.isMajor();
   }

   @Generated
   public String getEffect() {
      return this.effect;
   }

   @Generated
   public int getPowerCooldown() {
      return this.powerCooldown;
   }

   @Generated
   public void setPowerCooldown(int powerCooldown) {
      this.powerCooldown = powerCooldown;
   }

   @Generated
   public int getGlobalCooldown() {
      return this.globalCooldown;
   }

   @Generated
   public void setGlobalCooldown(int globalCooldown) {
      this.globalCooldown = globalCooldown;
   }

   @Generated
   public ConfigurationSection getScripts() {
      return this.scripts;
   }

   @Generated
   public void setScripts(ConfigurationSection scripts) {
      this.scripts = scripts;
   }

   @Generated
   public Map<String, Object> getRawScripts() {
      return this.rawScripts;
   }

   @Generated
   public void setRawScripts(Map<String, Object> rawScripts) {
      this.rawScripts = rawScripts;
   }

   @Generated
   public List<EliteScriptBlueprint> getEliteScriptBlueprints() {
      return this.eliteScriptBlueprints;
   }

   @Generated
   public void setEliteScriptBlueprints(List<EliteScriptBlueprint> eliteScriptBlueprints) {
      this.eliteScriptBlueprints = eliteScriptBlueprints;
   }

   @Generated
   public Class<? extends ElitePower> getElitePowerClass() {
      return this.elitePowerClass;
   }

   @Generated
   public PowersConfigFields.PowerType getPowerType() {
      return this.powerType;
   }

   @Generated
   public void setPowerType(PowersConfigFields.PowerType powerType) {
      this.powerType = powerType;
   }

   public static enum PowerType {
      OFFENSIVE,
      DEFENSIVE,
      MISCELLANEOUS,
      MAJOR_ZOMBIE,
      MAJOR_SKELETON,
      MAJOR_BLAZE,
      MAJOR_ENDERMAN,
      MAJOR_GHAST,
      UNIQUE;

      public boolean isMajor() {
         return this == MAJOR_ZOMBIE || this == MAJOR_SKELETON || this == MAJOR_BLAZE || this == MAJOR_ENDERMAN || this == MAJOR_GHAST;
      }
   }
}
