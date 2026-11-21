package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.enums.ConditionType;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

public class ScriptConditionsBlueprint {
   private final String scriptName;
   private final String filename;
   private Boolean isAlive = null;
   private Boolean runIfIsAliveIs = true;
   private List<String> hasTags = null;
   private Boolean runIfHasTagIs = true;
   private Boolean locationIsAir = null;
   private Boolean runIfLocationIsAirIs = true;
   private Boolean isOnFloor = null;
   private Boolean runIfIsOnFloorIs = true;
   private List<String> doesNotHaveTags = null;
   private Boolean runIfDoesNotHaveTagIs = true;
   private ScriptTargetsBlueprint scriptTargets = null;
   private Boolean runIfScriptTargetsIs = true;
   private Double randomChance = null;
   private Boolean runIfRandomChanceIs = true;
   private ConditionType conditionType = null;
   private Boolean runIfConditionTypeIs = true;
   private Integer targetCountLowerThan = null;
   private Boolean runIfTargetCountLowerThanIs = true;
   private Integer targetCountGreaterThan = null;
   private Boolean runIfTargetCountGreaterThanIs = true;
   private Material isStandingOnMaterial = null;
   private Boolean runIfIsStandingOnMaterialIs = true;

   public ScriptConditionsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
      this.scriptName = scriptName;
      this.filename = filename;
      if (configurationSection.get("Conditions") != null) {
         Map<?, ?> values = ((ConfigurationSection)configurationSection.get("Conditions")).getValues(false);
         this.processMapList(values);
         if (this.scriptTargets == null) {
            this.scriptTargets = new ScriptTargetsBlueprint(new HashMap(), scriptName, filename);
         }
      }
   }

   public ScriptConditionsBlueprint(Map<?, ?> values, String scriptName, String filename) {
      this.scriptName = scriptName;
      this.filename = filename;
      if (values != null) {
         this.processMapList(values);
      }

      if (this.scriptTargets == null) {
         this.scriptTargets = new ScriptTargetsBlueprint(new HashMap(), scriptName, filename);
      }
   }

   private void processKeyAndValue(String key, Object value) {
      String var3 = key.toLowerCase(Locale.ROOT);
      switch (var3) {
         case "isalive":
            this.isAlive = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "runifaliveis":
            this.runIfIsAliveIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "hastags":
            this.hasTags = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "runifhastagis":
            this.runIfHasTagIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "locationisair":
            this.locationIsAir = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "runiflocationisairis":
            this.runIfLocationIsAirIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "isonfloor":
            this.isOnFloor = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "runifisonflooris":
            this.runIfIsOnFloorIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "doesnothavetags":
            this.doesNotHaveTags = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "runifdoesnothavetagis":
            this.runIfDoesNotHaveTagIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "randomchance":
            this.randomChance = MapListInterpreter.parseDouble(key, value, this.scriptName);
            break;
         case "runifrandomchanceis":
            this.runIfRandomChanceIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "targetcountlowerthan":
            this.targetCountLowerThan = MapListInterpreter.parseInteger(key, value, this.scriptName);
            break;
         case "runiftargetcountlowerthanis":
            this.runIfTargetCountLowerThanIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "targetcountgreaterthan":
            this.targetCountGreaterThan = MapListInterpreter.parseInteger(key, value, this.scriptName);
            break;
         case "runiftargetcountgreaterthanis":
            this.runIfTargetCountGreaterThanIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "conditiontype":
            this.conditionType = MapListInterpreter.parseEnum(key, value, ConditionType.class, this.scriptName);
            break;
         case "runifconditiontypeis":
            this.runIfConditionTypeIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "isstandingonmaterial":
            this.isStandingOnMaterial = MapListInterpreter.parseEnum(key, value, Material.class, this.scriptName);
            break;
         case "runifisstandingonmaterialis":
            this.runIfIsStandingOnMaterialIs = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "target":
            if (value instanceof MemorySection ms) {
               value = ms.getValues(false);
            }

            this.scriptTargets = new ScriptTargetsBlueprint((Map<?, ?>)value, this.scriptName, this.filename);
            break;
         default:
            Logger.warn("Failed to read key " + key + " for script " + this.scriptName);
      }
   }

   private void processMapList(Map<?, ?> entries) {
      for (Entry<?, ?> entry : entries.entrySet()) {
         this.processKeyAndValue(entry.getKey().toString(), entry.getValue());
      }
   }

   @Generated
   public Boolean getIsAlive() {
      return this.isAlive;
   }

   @Generated
   public Boolean getRunIfIsAliveIs() {
      return this.runIfIsAliveIs;
   }

   @Generated
   public List<String> getHasTags() {
      return this.hasTags;
   }

   @Generated
   public Boolean getRunIfHasTagIs() {
      return this.runIfHasTagIs;
   }

   @Generated
   public Boolean getLocationIsAir() {
      return this.locationIsAir;
   }

   @Generated
   public Boolean getRunIfLocationIsAirIs() {
      return this.runIfLocationIsAirIs;
   }

   @Generated
   public Boolean getIsOnFloor() {
      return this.isOnFloor;
   }

   @Generated
   public Boolean getRunIfIsOnFloorIs() {
      return this.runIfIsOnFloorIs;
   }

   @Generated
   public List<String> getDoesNotHaveTags() {
      return this.doesNotHaveTags;
   }

   @Generated
   public Boolean getRunIfDoesNotHaveTagIs() {
      return this.runIfDoesNotHaveTagIs;
   }

   @Generated
   public ScriptTargetsBlueprint getScriptTargets() {
      return this.scriptTargets;
   }

   @Generated
   public Boolean getRunIfScriptTargetsIs() {
      return this.runIfScriptTargetsIs;
   }

   @Generated
   public Double getRandomChance() {
      return this.randomChance;
   }

   @Generated
   public Boolean getRunIfRandomChanceIs() {
      return this.runIfRandomChanceIs;
   }

   @Generated
   public ConditionType getConditionType() {
      return this.conditionType;
   }

   @Generated
   public void setConditionType(ConditionType conditionType) {
      this.conditionType = conditionType;
   }

   @Generated
   public Boolean getRunIfConditionTypeIs() {
      return this.runIfConditionTypeIs;
   }

   @Generated
   public Integer getTargetCountLowerThan() {
      return this.targetCountLowerThan;
   }

   @Generated
   public void setTargetCountLowerThan(Integer targetCountLowerThan) {
      this.targetCountLowerThan = targetCountLowerThan;
   }

   @Generated
   public Boolean getRunIfTargetCountLowerThanIs() {
      return this.runIfTargetCountLowerThanIs;
   }

   @Generated
   public Integer getTargetCountGreaterThan() {
      return this.targetCountGreaterThan;
   }

   @Generated
   public void setTargetCountGreaterThan(Integer targetCountGreaterThan) {
      this.targetCountGreaterThan = targetCountGreaterThan;
   }

   @Generated
   public Boolean getRunIfTargetCountGreaterThanIs() {
      return this.runIfTargetCountGreaterThanIs;
   }

   @Generated
   public Material getIsStandingOnMaterial() {
      return this.isStandingOnMaterial;
   }

   @Generated
   public void setIsStandingOnMaterial(Material isStandingOnMaterial) {
      this.isStandingOnMaterial = isStandingOnMaterial;
   }

   @Generated
   public Boolean getRunIfIsStandingOnMaterialIs() {
      return this.runIfIsStandingOnMaterialIs;
   }
}
