package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.loading.ScriptValueNormalizer;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public class EliteScriptBlueprint {
   private static final HashMap<CustomConfigFields, EliteScriptBlueprint> blueprints = new HashMap<>();
   private final CustomConfigFields customConfigFields;
   private final ScriptActionsBlueprint scriptActionsBlueprint;
   private final ScriptConditionsBlueprint scriptConditionsBlueprint;
   private final ScriptZoneBlueprint scriptZoneBlueprint;
   private final ScriptEventsBlueprint scriptEventsBlueprint;
   private final ScriptCooldownsBlueprint scriptCooldownsBlueprint;
   private final String filename;
   private final String scriptName;

   public EliteScriptBlueprint(CustomConfigFields customConfigFields, Map<String, Object> configurationValues, String scriptName) {
      this.filename = customConfigFields.getFilename();
      this.scriptName = scriptName;
      this.customConfigFields = customConfigFields;
      this.scriptEventsBlueprint = new ScriptEventsBlueprint(configurationValues, scriptName, this.filename);
      this.scriptConditionsBlueprint = new ScriptConditionsBlueprint(optionalMap(configurationValues, "Conditions"), scriptName, this.filename);
      this.scriptZoneBlueprint = new ScriptZoneBlueprint(configurationValues, scriptName, this.filename);
      this.scriptActionsBlueprint = new ScriptActionsBlueprint(configurationValues, scriptName, this.filename);
      this.scriptCooldownsBlueprint = new ScriptCooldownsBlueprint(optionalMap(configurationValues, "Cooldowns"), scriptName, this.filename);
      blueprints.put(customConfigFields, this);
   }

   public EliteScriptBlueprint(CustomConfigFields customConfigFields, ConfigurationSection configurationSection, String scriptName) {
      this(customConfigFields, ScriptValueNormalizer.normalizeSection(configurationSection), scriptName);
   }

   public static void shutdown() {
      blueprints.clear();
   }

   private static Map<?, ?> optionalMap(Map<String, Object> values, String key) {
      Object value = values.get(key);
      if (value == null) {
         return null;
      } else if (value instanceof Map<?, ?> map) {
         return map;
      } else {
         throw new IllegalArgumentException(key + " must contain key/value pairs, not " + (value instanceof List ? "a list" : value.getClass().getSimpleName()));
      }
   }

   public static List<EliteScriptBlueprint> parseBossScripts(ConfigurationSection configurationSection, CustomConfigFields customConfigFields) {
      if (configurationSection == null) {
         return Collections.emptyList();
      } else {
         checkLegacyFormat(configurationSection, customConfigFields);
         return parseBossScripts(ScriptValueNormalizer.normalizeSection(configurationSection), customConfigFields);
      }
   }

   public static List<EliteScriptBlueprint> parseBossScripts(Map<String, Object> configurationValues, CustomConfigFields customConfigFields) {
      if (configurationValues != null && !configurationValues.isEmpty()) {
         List<EliteScriptBlueprint> eliteScripts = new ArrayList<>();

         for (Entry<String, Object> entry : configurationValues.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> scriptValues) {
               try {
                  eliteScripts.add(new EliteScriptBlueprint(customConfigFields, ScriptValueNormalizer.normalizeMap(scriptValues), entry.getKey()));
               } catch (RuntimeException var7) {
                  Logger.warn("Skipped script " + entry.getKey() + " in " + customConfigFields.getFilename() + ": " + var7.getMessage());
               }
            } else {
               Logger.warn("Script " + entry.getKey() + " in " + customConfigFields.getFilename() + " is not a valid map!");
            }
         }

         return eliteScripts;
      } else {
         return Collections.emptyList();
      }
   }

   private static void checkLegacyFormat(ConfigurationSection configurationSection, CustomConfigFields customConfigFields) {
      boolean updated = false;

      for (String key : configurationSection.getKeys(true)) {
         String[] splitKeys = key.split("\\.");
         if (splitKeys.length >= 1) {
            String truncatedKey = splitKeys[splitKeys.length - 1];
            if (truncatedKey.equalsIgnoreCase("target") && configurationSection.get(key) instanceof String) {
               updated = true;
               String overallKey = key.replace(".target", "");
               new EliteScriptBlueprint.OldDataContainer(overallKey, configurationSection, overallKey);
            }

            if (truncatedKey.equalsIgnoreCase("Actions")) {
               List<Map<?, ?>> immutableMapList = new ArrayList<>(configurationSection.getMapList(key));
               List<Map<?, ?>> mutableMapList = new ArrayList<>();
               immutableMapList.forEach(mapx -> mutableMapList.add(new HashMap(mapx)));

               for (Map<?, ?> map : mutableMapList) {
                  Map<?, ?> clonedMap = new HashMap<>(map);

                  for (Entry<?, ?> entry : clonedMap.entrySet()) {
                     if (((String)entry.getKey()).equalsIgnoreCase("target") && !(entry.getValue() instanceof Map)) {
                        updated = true;
                        new EliteScriptBlueprint.OldDataContainer(map, customConfigFields.getFilename());
                     }
                  }
               }

               if (updated) {
                  configurationSection.set(key, mutableMapList);
               }
            }
         }
      }

      if (updated) {
         try {
            customConfigFields.getWritableFileConfiguration().set("eliteScript", configurationSection.getValues(false));
            customConfigFields.getWritableFileConfiguration().save(customConfigFields.getFile());
         } catch (Exception var14) {
            Logger.warn("Failed to update old script targets! Report this to the dev.");
         }
      }
   }

   @Generated
   public static HashMap<CustomConfigFields, EliteScriptBlueprint> getBlueprints() {
      return blueprints;
   }

   @Generated
   public CustomConfigFields getCustomConfigFields() {
      return this.customConfigFields;
   }

   @Generated
   public ScriptActionsBlueprint getScriptActionsBlueprint() {
      return this.scriptActionsBlueprint;
   }

   @Generated
   public ScriptConditionsBlueprint getScriptConditionsBlueprint() {
      return this.scriptConditionsBlueprint;
   }

   @Generated
   public ScriptZoneBlueprint getScriptZoneBlueprint() {
      return this.scriptZoneBlueprint;
   }

   @Generated
   public ScriptEventsBlueprint getScriptEventsBlueprint() {
      return this.scriptEventsBlueprint;
   }

   @Generated
   public ScriptCooldownsBlueprint getScriptCooldownsBlueprint() {
      return this.scriptCooldownsBlueprint;
   }

   @Generated
   public String getScriptName() {
      return this.scriptName;
   }

   private static class OldDataContainer {
      String location = null;
      String targetType = null;
      String offset = null;
      List<String> locations = null;
      Double range = null;
      Boolean track = null;
      String newKey;
      String scriptName;

      private OldDataContainer(String overallKey, ConfigurationSection configurationSection, String scriptName) {
         this.processMapList(configurationSection.getConfigurationSection(overallKey).getValues(false).entrySet());
         this.newKey = overallKey + ".Target";
         this.scriptName = scriptName;
         if (this.targetType != null) {
            this.updateKey("targetType", this.targetType, configurationSection);
         }

         if (this.location != null) {
            this.updateKey("location", this.location, configurationSection);
         }

         if (this.locations != null) {
            this.updateKey("locations", this.locations, configurationSection);
         }

         if (this.range != null) {
            this.updateKey("range", this.range, configurationSection);
         }

         if (this.offset != null) {
            this.updateKey("offset", this.offset, configurationSection);
         }

         if (this.track != null) {
            this.updateKey("track", this.track, configurationSection);
         }
      }

      private OldDataContainer(Map<?, ?> map, String scriptName) {
         this.processMapList(map.entrySet());
         this.scriptName = scriptName;
         Map<String, Object> newMap = new HashMap<>();
         if (this.targetType != null) {
            this.updateKey("targetType", this.targetType, (Map<String, Object>)map, newMap);
         }

         if (this.location != null) {
            this.updateKey("location", this.location, (Map<String, Object>)map, newMap);
         }

         if (this.locations != null) {
            this.updateKey("locations", this.locations, (Map<String, Object>)map, newMap);
         }

         if (this.range != null) {
            this.updateKey("range", this.range, (Map<String, Object>)map, newMap);
         }

         if (this.offset != null) {
            this.updateKey("offset", this.offset, (Map<String, Object>)map, newMap);
         }

         if (this.track != null) {
            this.updateKey("track", this.track, (Map<String, Object>)map, newMap);
         }

         ((Map<?, Map<String, Object>>)map).put("Target", newMap);
      }

      private void updateKey(String key, Object value, ConfigurationSection configurationSection) {
         configurationSection.set(this.newKey + "." + key, value);
         configurationSection.set(this.newKey.replace(".Target", ".") + key, null);
         if (key.contains("targetType")) {
            configurationSection.set(this.newKey.replace(".Target", ".") + key.replace("targetType", "target"), null);
         }
      }

      private void updateKey(String key, Object value, Map<String, Object> oldMap, Map<String, Object> newMap) {
         oldMap.remove(key);
         if (key.equals("targetType")) {
            oldMap.remove("target");
         }

         newMap.put(key, value);
      }

      private void processMapList(Set<Entry<String, Object>> entry) {
         for (Entry entrySet : entry) {
            String key = (String)entrySet.getKey();
            this.processKeyAndValue(key, entrySet.getValue());
         }
      }

      protected void processKeyAndValue(String key, Object value) {
         String var3 = key.toLowerCase(Locale.ROOT);
         switch (var3) {
            case "location":
               this.location = MapListInterpreter.parseString(key, value, this.scriptName);
               break;
            case "locations":
               this.locations = MapListInterpreter.parseStringList(key, value, this.scriptName);
               break;
            case "target":
               this.targetType = MapListInterpreter.parseString(key, value, this.scriptName);
               break;
            case "range":
               this.range = MapListInterpreter.parseDouble(key, value, this.scriptName);
               break;
            case "offset":
               this.offset = MapListInterpreter.parseString(key, value, this.scriptName);
               break;
            case "track":
               this.track = MapListInterpreter.parseBoolean(key, value, this.scriptName);
         }
      }
   }
}
