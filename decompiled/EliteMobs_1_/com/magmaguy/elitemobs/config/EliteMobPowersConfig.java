package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Generated;
import org.bukkit.entity.EntityType;

public class EliteMobPowersConfig extends ConfigurationFile {
   private static List<String> defensivePowers = new ArrayList<>();
   private static List<String> offensivePowers = new ArrayList<>();
   private static List<String> miscellaneousPowers = new ArrayList<>();
   private static HashSet<PowersConfigFields> defensivePowerFields = new HashSet<>();
   private static HashSet<PowersConfigFields> offensivePowerFields = new HashSet<>();
   private static HashSet<PowersConfigFields> miscellaneousPowerFields = new HashSet<>();
   private static final Map<EntityType, HashSet<PowersConfigFields>> majorPowerFields = new EnumMap<>(EntityType.class);
   private static final Map<EntityType, HashSet<PowersConfigFields>> disabledPowerFields = new EnumMap<>(EntityType.class);

   public EliteMobPowersConfig() {
      super("EliteMobPowers.yml");
   }

   @Override
   public void initializeValues() {
      defensivePowers = this.powerList(
         "defensivePowers",
         List.of("Sets the defensive powers natural elites can randomly roll.", "Both .lua filenames and legacy .yml aliases are accepted."),
         defaultPowers(PowersConfigFields.PowerType.DEFENSIVE)
      );
      offensivePowers = this.powerList(
         "offensivePowers",
         List.of("Sets the offensive powers natural elites can randomly roll.", "Both .lua filenames and legacy .yml aliases are accepted."),
         defaultPowers(PowersConfigFields.PowerType.OFFENSIVE)
      );
      miscellaneousPowers = this.powerList(
         "miscellaneousPowers",
         List.of("Sets the miscellaneous powers natural elites can randomly roll.", "Both .lua filenames and legacy .yml aliases are accepted."),
         defaultPowers(PowersConfigFields.PowerType.MISCELLANEOUS)
      );
      defensivePowerFields = resolvePowerList(defensivePowers, "defensivePowers");
      offensivePowerFields = resolvePowerList(offensivePowers, "offensivePowers");
      miscellaneousPowerFields = resolvePowerList(miscellaneousPowers, "miscellaneousPowers");
      majorPowerFields.clear();
      disabledPowerFields.clear();

      for (EntityType entityType : sortedEliteEntityTypes()) {
         List<String> majorPowers = this.powerList(
            "majorPowers." + entityType.name(),
            List.of("Sets the major powers " + entityType.name() + " elites can randomly roll."),
            defaultMajorPowers(entityType)
         );
         majorPowerFields.put(entityType, resolvePowerList(majorPowers, "majorPowers." + entityType.name()));
         List<String> disabledPowers = this.powerList(
            "disabledPowers." + entityType.name(),
            List.of("Removes these powers from " + entityType.name() + " elites after the shared pools are loaded."),
            defaultDisabledPowers(entityType)
         );
         disabledPowerFields.put(entityType, resolvePowerList(disabledPowers, "disabledPowers." + entityType.name()));
      }
   }

   public static HashSet<PowersConfigFields> getDefensivePowerFields() {
      return new HashSet<>(defensivePowerFields);
   }

   public static HashSet<PowersConfigFields> getOffensivePowerFields() {
      return new HashSet<>(offensivePowerFields);
   }

   public static HashSet<PowersConfigFields> getMiscellaneousPowerFields() {
      return new HashSet<>(miscellaneousPowerFields);
   }

   public static HashSet<PowersConfigFields> getMajorPowerFields(EntityType entityType) {
      return new HashSet<>(majorPowerFields.getOrDefault(entityType, new HashSet<>()));
   }

   public static HashSet<PowersConfigFields> getDisabledPowerFields(EntityType entityType) {
      return new HashSet<>(disabledPowerFields.getOrDefault(entityType, new HashSet<>()));
   }

   private List<String> powerList(String path, List<String> comments, List<String> defaults) {
      List<?> configuredList = ConfigurationEngine.setList(comments, this.file, this.fileConfiguration, path, defaults, false);
      List<String> sanitizedList = new ArrayList<>();
      if (configuredList == null) {
         return sanitizedList;
      } else {
         for (Object entry : configuredList) {
            if (entry != null) {
               String value = entry.toString().trim();
               if (!value.isEmpty()) {
                  sanitizedList.add(value);
               }
            }
         }

         return sanitizedList;
      }
   }

   private static List<EntityType> sortedEliteEntityTypes() {
      return MobPropertiesConfig.getMobProperties().keySet().stream().sorted((first, second) -> first.name().compareToIgnoreCase(second.name())).toList();
   }

   private static List<String> defaultPowers(PowersConfigFields.PowerType powerType) {
      Set<String> defaults = new LinkedHashSet<>();
      PowersConfig.getPowers()
         .values()
         .stream()
         .filter(power -> power.getPowerType() == powerType)
         .map(EliteMobPowersConfig::configFilename)
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .forEach(defaults::add);
      return new ArrayList<>(defaults);
   }

   private static String configFilename(PowersConfigFields powersConfigFields) {
      String filename = powersConfigFields.getFilename();
      if (filename == null) {
         return "";
      } else {
         String lowerCaseFilename = filename.toLowerCase(Locale.ROOT);
         return lowerCaseFilename.endsWith(".lua") ? filename.substring(0, filename.length() - 4) + ".yml" : filename;
      }
   }

   private static List<String> defaultMajorPowers(EntityType entityType) {
      return (List<String>)(switch (entityType) {
         case BLAZE -> List.of("tracking_fireball.yml");
         case SKELETON -> List.of("skeleton_pillar.yml", "skeleton_tracking_arrow.yml");
         case ZOMBIE -> List.of("zombie_bloat.yml", "zombie_friends.yml", "zombie_necronomicon.yml", "zombie_parents.yml");
         default -> new ArrayList();
      });
   }

   private static List<String> defaultDisabledPowers(EntityType entityType) {
      return (List<String>)(switch (entityType) {
         case CREEPER -> List.of("invisibility.yml");
         case PHANTOM -> List.of("attack_lightning.yml", "ground_pound.yml");
         case VEX -> List.of("ground_pound.yml");
         default -> new ArrayList();
      });
   }

   private static HashSet<PowersConfigFields> resolvePowerList(List<String> powerNames, String path) {
      HashSet<PowersConfigFields> resolvedPowers = new HashSet<>();

      for (String powerName : powerNames) {
         PowersConfigFields powersConfigFields = PowersConfig.getPower(powerName);
         if (powersConfigFields == null) {
            Logger.warn("Invalid power " + powerName + " in EliteMobPowers.yml at " + path + ". This power will be ignored.");
         } else {
            resolvedPowers.add(powersConfigFields);
         }
      }

      return resolvedPowers;
   }

   @Generated
   public static List<String> getDefensivePowers() {
      return defensivePowers;
   }

   @Generated
   public static List<String> getOffensivePowers() {
      return offensivePowers;
   }

   @Generated
   public static List<String> getMiscellaneousPowers() {
      return miscellaneousPowers;
   }
}
