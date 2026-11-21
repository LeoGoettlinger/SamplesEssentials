package github.nighter.smartspawner.config;

import github.nighter.smartspawner.SmartSpawner;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class Config {
   private static volatile Config instance;
   private static final String DEFAULT_DROP_CHANCE_KEY = "default";
   private final boolean approximateLoot;
   private final int approximationThreshold;
   private final boolean breakEnabled;
   private final boolean directToInventory;
   private final int durabilityLoss;
   private final boolean sneakBreakEnabled;
   private final boolean silkTouchRequired;
   private final int silkTouchLevel;
   private final boolean sellAndXpBreak;
   private final boolean allowExpMending;
   private final boolean sneakStackEnabled;
   private final boolean sneakPlaceEnabled;
   private final boolean naturalBreakable;
   private final boolean convertNaturalToSmartSpawner;
   private final boolean spawnerStackParticlesEnabled;
   private final boolean spawnerActivateParticlesEnabled;
   private final boolean spawnerGenerateLootParticlesEnabled;
   private final Set<Material> requiredTools;
   private final Map<EntityType, Double> naturalSpawnerDropChances;
   private final double naturalSpawnerDefaultDropChance;

   private Config(FileConfiguration config, Logger logger) {
      this.approximateLoot = config.getBoolean("performance.loot_generation.approximate_loot", true);
      this.approximationThreshold = config.getInt("performance.loot_generation.approximation_threshold", 1000);
      this.breakEnabled = config.getBoolean("spawner_break.enabled", true);
      this.directToInventory = config.getBoolean("spawner_break.direct_to_inventory", false);
      this.durabilityLoss = config.getInt("spawner_break.durability_loss", 1);
      this.sneakBreakEnabled = config.getBoolean("spawner_break.sneak_break", true);
      this.silkTouchRequired = config.getBoolean("spawner_break.silk_touch.required", true);
      this.silkTouchLevel = config.getInt("spawner_break.silk_touch.level", 1);
      this.sellAndXpBreak = config.getBoolean("spawner_break.sell_and_xp_break", true);
      this.allowExpMending = config.getBoolean("spawner_properties.default.allow_exp_mending", true);
      this.sneakStackEnabled = config.getBoolean("spawner_properties.default.sneak_stack", true);
      this.sneakPlaceEnabled = config.getBoolean("spawner_properties.default.sneak_place", true);
      this.naturalBreakable = config.getBoolean("natural_spawner.breakable", false);
      this.convertNaturalToSmartSpawner = config.getBoolean("natural_spawner.convert_to_smart_spawner", false);
      this.spawnerStackParticlesEnabled = config.getBoolean("particle.spawner_stack", true);
      this.spawnerActivateParticlesEnabled = config.getBoolean("particle.spawner_activate", true);
      this.spawnerGenerateLootParticlesEnabled = config.getBoolean("particle.spawner_generate_loot", true);
      this.requiredTools = loadRequiredTools(config, logger);
      Config.DropChanceResult dropChanceResult = loadNaturalSpawnerDropChances(config, logger);
      this.naturalSpawnerDefaultDropChance = dropChanceResult.defaultChance();
      this.naturalSpawnerDropChances = dropChanceResult.entityChances();
   }

   public static Config get() {
      return instance;
   }

   public static void reload(SmartSpawner plugin) {
      load(plugin);
   }

   public static void load(SmartSpawner plugin) {
      instance = new Config(plugin.getConfig(), plugin.getLogger());
   }

   public boolean isRequiredTool(Material material) {
      return this.requiredTools.contains(material);
   }

   public double getNaturalSpawnerDropChance(EntityType entityType) {
      return entityType == null
         ? this.naturalSpawnerDefaultDropChance
         : this.naturalSpawnerDropChances.getOrDefault(entityType, this.naturalSpawnerDefaultDropChance);
   }

   private static Set<Material> loadRequiredTools(FileConfiguration config, Logger logger) {
      return config.getStringList("spawner_break.required_tools").stream().map(toolName -> {
         try {
            return Material.valueOf(toolName.toUpperCase());
         } catch (IllegalArgumentException var3) {
            logger.warning("Invalid material in spawner_break.required_tools: " + toolName);
            return null;
         }
      }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
   }

   private static Config.DropChanceResult loadNaturalSpawnerDropChances(FileConfiguration config, Logger logger) {
      ConfigurationSection section = config.getConfigurationSection("natural_spawner.drop_chance");
      double defaultDropChance = 100.0;
      Map<EntityType, Double> loadedDropChances = new EnumMap<>(EntityType.class);
      if (section != null) {
         for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) {
               double chance = section.getDouble(key, 100.0);
               if (chance < 0.0 || chance > 100.0) {
                  logger.warning("Invalid drop chance for natural_spawner.drop_chance." + key + ". Value must be between 0.0 and 100.0; using 100.0");
                  chance = 100.0;
               }

               defaultDropChance = chance;
            } else {
               EntityType entityType;
               try {
                  entityType = EntityType.valueOf(key.toUpperCase());
               } catch (IllegalArgumentException var11) {
                  logger.warning("Invalid entity in natural_spawner.drop_chance: " + key);
                  continue;
               }

               double dropChance = section.getDouble(key, 100.0);
               if (dropChance < 0.0 || dropChance > 100.0) {
                  logger.warning("Invalid drop chance for natural_spawner.drop_chance." + key + ". Value must be between 0.0 and 100.0; using 100.0");
                  dropChance = 100.0;
               }

               loadedDropChances.put(entityType, dropChance);
            }
         }
      }

      return new Config.DropChanceResult(defaultDropChance, Map.copyOf(loadedDropChances));
   }

   @Generated
   public boolean isApproximateLoot() {
      return this.approximateLoot;
   }

   @Generated
   public int getApproximationThreshold() {
      return this.approximationThreshold;
   }

   @Generated
   public boolean isBreakEnabled() {
      return this.breakEnabled;
   }

   @Generated
   public boolean isDirectToInventory() {
      return this.directToInventory;
   }

   @Generated
   public int getDurabilityLoss() {
      return this.durabilityLoss;
   }

   @Generated
   public boolean isSneakBreakEnabled() {
      return this.sneakBreakEnabled;
   }

   @Generated
   public boolean isSilkTouchRequired() {
      return this.silkTouchRequired;
   }

   @Generated
   public int getSilkTouchLevel() {
      return this.silkTouchLevel;
   }

   @Generated
   public boolean isSellAndXpBreak() {
      return this.sellAndXpBreak;
   }

   @Generated
   public boolean isAllowExpMending() {
      return this.allowExpMending;
   }

   @Generated
   public boolean isSneakStackEnabled() {
      return this.sneakStackEnabled;
   }

   @Generated
   public boolean isSneakPlaceEnabled() {
      return this.sneakPlaceEnabled;
   }

   @Generated
   public boolean isNaturalBreakable() {
      return this.naturalBreakable;
   }

   @Generated
   public boolean isConvertNaturalToSmartSpawner() {
      return this.convertNaturalToSmartSpawner;
   }

   @Generated
   public boolean isSpawnerStackParticlesEnabled() {
      return this.spawnerStackParticlesEnabled;
   }

   @Generated
   public boolean isSpawnerActivateParticlesEnabled() {
      return this.spawnerActivateParticlesEnabled;
   }

   @Generated
   public boolean isSpawnerGenerateLootParticlesEnabled() {
      return this.spawnerGenerateLootParticlesEnabled;
   }

   private record DropChanceResult(double defaultChance, Map<EntityType, Double> entityChances) {
   }
}
