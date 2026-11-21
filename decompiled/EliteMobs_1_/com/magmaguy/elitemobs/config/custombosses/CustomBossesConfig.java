package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.magmacore.config.CustomConfig;
import com.magmaguy.elitemobs.magmacore.config.CustomConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomBossesConfig extends CustomConfig {
   private static HashMap<String, CustomBossesConfigFields> customBosses = new HashMap<>();
   private static CustomBossesConfig instance;
   private static Map<String, Set<String>> itemSources = Map.of();

   public static Set<String> bossesDropping(String itemFilename) {
      return itemSources.getOrDefault(itemFilename, Set.of());
   }

   private static void rebuildItemSources() {
      HashMap<String, Set<String>> index = new HashMap<>();

      for (CustomBossesConfigFields boss : customBosses.values()) {
         if (boss.getCustomLootTable() != null) {
            for (CustomLootEntry entry : boss.getCustomLootTable().getEntries()) {
               if (entry instanceof EliteCustomLootEntry item) {
                  index.computeIfAbsent(item.getFilename(), ignored -> new HashSet<>()).add(boss.getFilename());
               }
            }
         }
      }

      index.replaceAll((itemx, sources) -> Set.copyOf(sources));
      itemSources = Map.copyOf(index);
   }

   public CustomBossesConfig() {
      super("custombosses", "com.magmaguy.elitemobs.config.custombosses.premade", CustomBossesConfigFields.class, EliteMobsConfigInheritance.POLICY);
      instance = this;
      customBosses = new HashMap<>();

      for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
         if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
            CustomBossesConfigFields customBossesConfigFields = (CustomBossesConfigFields)super.getCustomConfigFieldsHashMap().get(key);
            customBosses.put(key, customBossesConfigFields);
         }
      }

      rebuildItemSources();
   }

   public static void initializeBosses() {
      for (CustomBossesConfigFields customBossesConfigFields : customBosses.values()) {
         if (customBossesConfigFields.isRegionalBoss()) {
            if (customBossesConfigFields.isInstanced()) {
               for (String string : customBossesConfigFields.processStringList(
                  "spawnLocations", customBossesConfigFields.getSpawnLocations(), customBossesConfigFields.getSpawnLocations(), false
               )) {
                  InstancedBossEntity.add(string, customBossesConfigFields);
               }
            } else {
               CustomBossesConfigFields.getRegionalElites().put(customBossesConfigFields.getFilename(), customBossesConfigFields);
               if (!customBossesConfigFields.isReinforcement()) {
                  for (String string : customBossesConfigFields.processStringList(
                     "spawnLocations", customBossesConfigFields.getSpawnLocations(), customBossesConfigFields.getSpawnLocations(), false
                  )) {
                     try {
                        new RegionalBossEntity(customBossesConfigFields, string).initialize();
                     } catch (IllegalArgumentException var6) {
                        Logger.warn("Rejected regional boss " + customBossesConfigFields.getFilename() + " at " + string + ": " + var6.getMessage());
                     }
                  }
               }
            }
         }
      }
   }

   public static HashMap<String, ? extends CustomBossesConfigFields> getCustomBosses() {
      return customBosses;
   }

   public static CustomBossesConfigFields getCustomBoss(String fileName) {
      return customBosses.get(fileName);
   }

   public static CustomBossesConfigFields registerRuntimeFile(File file) {
      if (instance == null) {
         throw new IllegalStateException("Custom boss configuration is not initialized");
      } else {
         for (CustomConfigFields loaded : instance.getCustomConfigFieldsHashMap().values()) {
            if (loaded.getFilename().equalsIgnoreCase(file.getName()) && !sameFile(loaded.getFile(), file)) {
               throw new IllegalArgumentException("Another configuration already owns filename " + file.getName());
            }
         }

         CustomBossesConfigFields fields = (CustomBossesConfigFields)instance.registerFile(file);
         if (fields != null && fields.isEnabled()) {
            customBosses.put(fields.getFilename(), fields);
         }

         rebuildItemSources();
         return fields;
      }
   }

   public static void unregisterRuntimeFile(File file) {
      if (instance != null) {
         instance.getCustomConfigFieldsHashMap().entrySet().removeIf(entry -> sameFile(entry.getValue().getFile(), file));
         customBosses.entrySet().removeIf(entry -> sameFile(entry.getValue().getFile(), file));
         rebuildItemSources();
      }
   }

   private static boolean sameFile(File left, File right) {
      return left != null && left.toPath().toAbsolutePath().normalize().equals(right.toPath().toAbsolutePath().normalize());
   }
}
