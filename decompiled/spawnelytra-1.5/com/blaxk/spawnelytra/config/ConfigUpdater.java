package com.blaxk.spawnelytra.config;

import com.blaxk.spawnelytra.util.BackupUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum ConfigUpdater {
   private static final String CURRENT_CONFIG_VERSION = "1.5";
   private static final Pattern VERSION_LABEL = Pattern.compile("(?m)^# Plugin Version: (.+?)\\s*$");
   private static final Pattern LANGUAGES_LABEL = Pattern.compile("(?m)^# Available languages: .+$");
   private static final String LANGUAGES_LINE = "# Available languages: en, de, es, fr, pl";

   public static boolean updateConfig(JavaPlugin plugin) {
      File configFile = new File(plugin.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         plugin.saveDefaultConfig();
         return false;
      } else {
         FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         ConfigUpdater.ConfigVersion version = detectConfigVersion(config);
         boolean migratedFromV13 = false;
         switch (version) {
            case V1_2:
               plugin.getLogger().info("Detected v1.2 config. Migrating to v1.5...");
               migrateFromV12(plugin, config, configFile);
               break;
            case V1_3:
               plugin.getLogger().info("Detected v1.3 config. Migrating to v1.5...");
               migrateFromV13(plugin, config, configFile);
               migratedFromV13 = true;
               break;
            case MODERN:
               updateModernConfig(plugin, config, configFile);
               break;
            case UNKNOWN:
               plugin.getLogger().warning("Unknown config format. Creating backup and generating new config.");
               createBackupAndGenerateNew(plugin, configFile);
         }

         plugin.reloadConfig();
         return migratedFromV13;
      }
   }

   private static ConfigUpdater.ConfigVersion detectConfigVersion(FileConfiguration config) {
      if (config.contains("worlds")) {
         return ConfigUpdater.ConfigVersion.MODERN;
      } else if (config.contains("boost_enabled") || config.contains("disable_fireworks_in_spawn_elytra")) {
         return ConfigUpdater.ConfigVersion.V1_3;
      } else {
         return !config.contains("activation_mode") && !config.contains("radius") && !config.contains("world")
            ? ConfigUpdater.ConfigVersion.UNKNOWN
            : ConfigUpdater.ConfigVersion.V1_2;
      }
   }

   private static void migrateFromV12(JavaPlugin plugin, FileConfiguration oldConfig, File configFile) {
      createBackup(plugin, configFile);
      String activationMode = oldConfig.getString("activation_mode", "double_jump");
      int radius = oldConfig.getInt("radius", 100);
      int strength = oldConfig.getInt("strength", 2);
      String world = oldConfig.getString("world", "world");
      String language = oldConfig.getString("language", "de");
      String mode = oldConfig.getString("mode", "auto");
      String boostDirection = oldConfig.getString("boost_direction", "forward");
      String boostSound = oldConfig.getString("boost_sound", "ENTITY_BAT_TAKEOFF");
      boolean disableInCreative = oldConfig.getBoolean("disable_in_creative", true);
      boolean disableInAdventure = oldConfig.getBoolean("disable_in_adventure", false);
      int spawnX = 0;
      int spawnY = 64;
      int spawnZ = 0;
      int spawnX2 = 0;
      int spawnY2 = 0;
      int spawnZ2 = 0;
      if (oldConfig.contains("spawn")) {
         ConfigurationSection spawnSection = oldConfig.getConfigurationSection("spawn");
         if (spawnSection != null) {
            spawnX = spawnSection.getInt("x", 0);
            spawnY = spawnSection.getInt("y", 64);
            spawnZ = spawnSection.getInt("z", 0);
            spawnX2 = spawnSection.getInt("x2", 0);
            spawnY2 = spawnSection.getInt("y2", 0);
            spawnZ2 = spawnSection.getInt("z2", 0);
         }
      }

      boolean showPressToBoost = true;
      boolean showBoostActivated = true;
      String customPressMessage = "&aPress &a&l{key} &ato boost yourself.";
      String customBoostMessage = "&aBoost activated!";
      if (oldConfig.contains("messages")) {
         ConfigurationSection msgSection = oldConfig.getConfigurationSection("messages");
         if (msgSection != null) {
            showPressToBoost = msgSection.getBoolean("show_press_to_boost", true);
            showBoostActivated = msgSection.getBoolean("show_boost_activated", true);
            customPressMessage = msgSection.getString("press_to_boost", customPressMessage);
            customBoostMessage = msgSection.getString("boost_activated", customBoostMessage);
         }
      }

      generateModernConfig(
         plugin,
         configFile,
         language,
         activationMode,
         radius,
         strength,
         boostDirection,
         world,
         mode,
         spawnX,
         spawnY,
         spawnZ,
         spawnX2,
         spawnY2,
         spawnZ2,
         boostSound,
         disableInCreative,
         disableInAdventure,
         showPressToBoost,
         showBoostActivated,
         true,
         false,
         1.5
      );
   }

   private static void migrateFromV13(JavaPlugin plugin, FileConfiguration oldConfig, File configFile) {
      createBackup(plugin, configFile);
      String activationMode = oldConfig.getString("activation_mode", "double_jump");
      int radius = oldConfig.getInt("radius", 100);
      int strength = oldConfig.getInt("strength", 2);
      String world = oldConfig.getString("world", "world");
      String language = oldConfig.getString("language", "de");
      String boostDirection = oldConfig.getString("boost_direction", "forward");
      String boostSound = oldConfig.getString("boost_sound", "ENTITY_BAT_TAKEOFF");
      boolean disableInCreative = oldConfig.getBoolean("disable_in_creative", true);
      boolean disableInAdventure = oldConfig.getBoolean("disable_in_adventure", false);
      boolean boostEnabled = oldConfig.getBoolean("boost_enabled", true);
      boolean disableFireworks = oldConfig.getBoolean("disable_fireworks_in_spawn_elytra", false);
      double fKeyLaunchStrength = oldConfig.getDouble("f_key_launch_strength", 1.5);
      String mode = "auto";
      int spawnX = 0;
      int spawnY = 64;
      int spawnZ = 0;
      int spawnX2 = 0;
      int spawnY2 = 0;
      int spawnZ2 = 0;
      if (oldConfig.contains("spawn")) {
         ConfigurationSection spawnSection = oldConfig.getConfigurationSection("spawn");
         if (spawnSection != null) {
            mode = spawnSection.getString("mode", "advanced");
            spawnX = spawnSection.getInt("x", 0);
            spawnY = spawnSection.getInt("y", 64);
            spawnZ = spawnSection.getInt("z", 0);
            spawnX2 = spawnSection.getInt("x2", 0);
            spawnY2 = spawnSection.getInt("y2", 0);
            spawnZ2 = spawnSection.getInt("z2", 0);
         }
      }

      boolean showPressToBoost = true;
      boolean showBoostActivated = true;
      if (oldConfig.contains("messages")) {
         ConfigurationSection msgSection = oldConfig.getConfigurationSection("messages");
         if (msgSection != null) {
            showPressToBoost = msgSection.getBoolean("show_press_to_boost", true);
            showBoostActivated = msgSection.getBoolean("show_boost_activated", true);
         }
      }

      generateModernConfig(
         plugin,
         configFile,
         language,
         activationMode,
         radius,
         strength,
         boostDirection,
         world,
         mode,
         spawnX,
         spawnY,
         spawnZ,
         spawnX2,
         spawnY2,
         spawnZ2,
         boostSound,
         disableInCreative,
         disableInAdventure,
         showPressToBoost,
         showBoostActivated,
         boostEnabled,
         disableFireworks,
         fKeyLaunchStrength
      );
   }

   private static void updateModernConfig(JavaPlugin plugin, FileConfiguration config, File configFile) {
      boolean needsUpdate = false;
      if (hasStaleVersionLabels(configFile)) {
         needsUpdate = true;
      }

      if (!config.contains("language")) {
         config.set("language", "en");
         needsUpdate = true;
      }

      if (!config.contains("game_modes")) {
         config.createSection("game_modes");
         needsUpdate = true;
      }

      if (!config.contains("fireworks")) {
         config.createSection("fireworks");
         needsUpdate = true;
      }

      if (!config.contains("messages")) {
         config.createSection("messages");
         needsUpdate = true;
      }

      if (!config.contains("messages.show_creative_disabled")) {
         config.set("messages.show_creative_disabled", false);
         needsUpdate = true;
      }

      if (!config.contains("hunger_consumption")) {
         config.createSection("hunger_consumption");
         needsUpdate = true;
      }

      if (!config.contains("bedrock")) {
         config.set("bedrock.enabled", true);
         config.setComments(
            "bedrock",
            List.of(
               "Bedrock (Geyser/Floodgate) support",
               "Bedrock Edition cannot glide without a real elytra equipped. When enabled,",
               "Bedrock players inside the spawn area receive a temporary elytra. They use",
               "it like a normal elytra: jump, then press jump again while falling. Since",
               "Bedrock has no offhand/F key, Bedrock players always boost by pressing sneak",
               "while gliding."
            )
         );
         needsUpdate = true;
      }

      if (config.contains("bedrock.sneak_to_boost")) {
         config.set("bedrock.sneak_to_boost", null);
         needsUpdate = true;
      }

      if (!config.contains("worlds")) {
         config.createSection("worlds");
         needsUpdate = true;
      }

      ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
      if (worldsSection != null) {
         for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection != null) {
               if (!worldSection.contains("boost.max_boosts")) {
                  worldSection.set("boost.max_boosts", 1);
                  worldSection.setComments("boost.max_boosts", List.of("Maximum number of boosts allowed per elytra flight (1 = single boost)"));
                  needsUpdate = true;
               }

               if (!worldSection.contains("boost.boost_cooldown")) {
                  worldSection.set("boost.boost_cooldown", 0);
                  worldSection.setComments(
                     "boost.boost_cooldown", List.of("Cooldown in seconds between boosts (0 = no cooldown, only applies when max_boosts > 1)")
                  );
                  needsUpdate = true;
               }
            }
         }
      }

      if (needsUpdate) {
         try {
            BackupUtil.backupFile(plugin, configFile, "config/config.yml");
            config.save(configFile);
            refreshVersionLabels(configFile);
            plugin.getLogger().info("Updated v1.5 config with missing fields.");
         } catch (IOException var8) {
            plugin.getLogger().severe("Failed to save v1.5 config: " + var8.getMessage());
         }
      }
   }

   private static boolean hasStaleVersionLabels(File configFile) {
      try {
         String content = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
         Matcher version = VERSION_LABEL.matcher(content);
         if (version.find() && !"1.5".equals(version.group(1))) {
            return true;
         } else {
            Matcher languages = LANGUAGES_LABEL.matcher(content);
            return languages.find() && !"# Available languages: en, de, es, fr, pl".equals(languages.group());
         }
      } catch (IOException var4) {
         return false;
      }
   }

   private static void refreshVersionLabels(File configFile) throws IOException {
      String content = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
      content = VERSION_LABEL.matcher(content).replaceFirst("# Plugin Version: 1.5");
      content = LANGUAGES_LABEL.matcher(content).replaceFirst(Matcher.quoteReplacement("# Available languages: en, de, es, fr, pl"));
      Files.writeString(configFile.toPath(), content, StandardCharsets.UTF_8);
   }

   private static void generateModernConfig(
      JavaPlugin plugin,
      File configFile,
      String language,
      String activationMode,
      int radius,
      int strength,
      String boostDirection,
      String worldName,
      String spawnMode,
      int spawnX,
      int spawnY,
      int spawnZ,
      int spawnX2,
      int spawnY2,
      int spawnZ2,
      String boostSound,
      boolean disableInCreative,
      boolean disableInAdventure,
      boolean showPressToBoost,
      boolean showBoostActivated,
      boolean boostEnabled,
      boolean disableFireworks,
      double fKeyLaunchStrength
   ) {
      try {
         List<String> lines = new ArrayList<>();
         lines.add("# Spawn Elytra Plugin by blaxk");
         lines.add("# Plugin Version: 1.5");
         lines.add("# Modrinth: https://modrinth.com/plugin/spawn-elytra");
         lines.add("");
         lines.add("# ==========================================");
         lines.add("# GLOBAL SETTINGS");
         lines.add("# ==========================================");
         lines.add("");
         lines.add("# Available languages: en, de, es, fr, pl");
         lines.add("language: " + language);
         lines.add("");
         lines.add("# Game mode restrictions");
         lines.add("game_modes:");
         lines.add("  # Automatically disable elytra when player enters creative mode (This prevents buggy flying in Creative)");
         lines.add("  disable_in_creative: " + disableInCreative);
         lines.add("  # If you don't want to disable elytra in adventure mode, set this to false");
         lines.add("  disable_in_adventure: " + disableInAdventure);
         lines.add("");
         lines.add("# Fireworks settings");
         lines.add("fireworks:");
         lines.add("  # Disable fireworks when using spawn elytra (players can still use fireworks if they have a real elytra equipped)");
         lines.add("  disable_in_spawn_elytra: " + disableFireworks);
         lines.add("");
         lines.add("# Bedrock (Geyser/Floodgate) support");
         lines.add("bedrock:");
         lines.add("  # Bedrock Edition cannot glide without a real elytra equipped. When enabled,");
         lines.add("  # Bedrock players inside the spawn area receive a temporary elytra. They use");
         lines.add("  # it like a normal elytra: jump, then press jump again while falling. Since");
         lines.add("  # Bedrock has no offhand/F key, Bedrock players always boost by pressing sneak");
         lines.add("  # while gliding.");
         lines.add("  enabled: true");
         lines.add("");
         lines.add("# Message settings");
         lines.add("messages:");
         lines.add("  # Set to false to disable the \"press to boost\" message");
         lines.add("  show_press_to_boost: " + showPressToBoost);
         lines.add("  # Set to false to disable the \"boost activated\" message");
         lines.add("  show_boost_activated: " + showBoostActivated);
         lines.add("  # Set to true to show an actionbar when Elytra is disabled in Creative mode");
         lines.add("  show_creative_disabled: false");
         lines.add("  # Message style: classic or small_caps");
         lines.add("  style: classic");
         lines.add("");
         lines.add("# Hunger consumption settings (global defaults, can be overridden per-world)");
         lines.add("hunger_consumption:");
         lines.add("  # Enable hunger consumption while using the spawn elytra features");
         lines.add("  enabled: false");
         lines.add("  # How hunger should be consumed: activation, distance, or time");
         lines.add("  mode: activation");
         lines.add("  # Minimum food level to keep (players will never drop below this value)");
         lines.add("  minimum_food_level: 0");
         lines.add("");
         lines.add("  activation:");
         lines.add("    # Hunger consumed each time the elytra activates");
         lines.add("    hunger_cost: 1");
         lines.add("");
         lines.add("  distance:");
         lines.add("    # Blocks travelled while gliding before hunger is consumed");
         lines.add("    blocks_per_point: 50.0");
         lines.add("    # Hunger consumed every time the distance threshold is reached");
         lines.add("    hunger_cost: 1");
         lines.add("");
         lines.add("  time:");
         lines.add("    # Seconds of gliding before hunger is consumed");
         lines.add("    seconds_per_point: 30");
         lines.add("    # Hunger consumed each time the timer elapses");
         lines.add("    hunger_cost: 1");
         lines.add("");
         lines.add("# ==========================================");
         lines.add("# WORLD-SPECIFIC SETTINGS");
         lines.add("# ==========================================");
         lines.add("");
         lines.add("# Configure elytra settings per world");
         lines.add("worlds:");
         lines.add("  # If you want to add another world, copy the entire '" + worldName + "' section and change the name and preferences");
         lines.add("  " + worldName + ":");
         lines.add("    # Enable spawn elytra in this world");
         lines.add("    enabled: true");
         lines.add("    ");
         lines.add("    # Activation mode for elytra:");
         lines.add("    # double_jump: Player needs to double-press space to activate elytra");
         lines.add("    # auto: Automatically activates elytra when player has air below and is in spawn area");
         lines.add("    # sneak_jump: Player needs to sneak while jumping to activate elytra");
         lines.add("    # f_key: Player needs to press F (swap hands) to activate elytra, this also boosts a player upwards on activation");
         lines.add("    activation_mode: " + activationMode);
         lines.add("    ");
         lines.add("    # The radius around spawn where elytra boosting is enabled");
         lines.add("    # (only used when area_mode is 'circular' or spawn coords x2/y2/z2 are all 0)");
         lines.add("    radius: " + radius);
         lines.add("    ");
         lines.add("    # Spawn area configuration");
         lines.add("    spawn_area:");
         lines.add("      # Mode options: 'auto' or 'advanced'");
         lines.add("      # auto: Uses the world spawn point with radius");
         lines.add("      # advanced: Uses custom spawn coordinates defined below");
         lines.add("      mode: " + spawnMode);
         lines.add("      ");
         lines.add("      # Area type: 'circular' or 'rectangular'");
         lines.add("      area_type: " + (spawnX2 == 0 && spawnY2 == 0 && spawnZ2 == 0 ? "circular" : "rectangular"));
         lines.add("      ");
         lines.add("      # Primary spawn coordinates (center for circular, first corner for rectangular)");
         lines.add("      x: " + spawnX);
         lines.add("      y: " + spawnY);
         lines.add("      z: " + spawnZ);
         lines.add("      ");
         lines.add("      # Secondary coordinates (only used for rectangular areas)");
         lines.add("      # Setting all to 0 uses circular area with radius instead");
         lines.add("      x2: " + spawnX2);
         lines.add("      y2: " + spawnY2);
         lines.add("      z2: " + spawnZ2);
         lines.add("    ");
         lines.add("    # Boost settings");
         lines.add("    boost:");
         lines.add("      # Enable boost functionality");
         lines.add("      enabled: " + boostEnabled);
         lines.add("      # The strength of the boost when pressing the boost key");
         lines.add("      strength: " + strength);
         lines.add("      # Boost direction: 'forward' or 'upward'");
         lines.add("      # forward: Boosts player in the direction they are looking");
         lines.add("      # upward: Boosts player straight up");
         lines.add("      direction: " + boostDirection);
         lines.add("      # Maximum number of boosts allowed per elytra flight (1 = single boost)");
         lines.add("      max_boosts: 1");
         lines.add("      # Cooldown in seconds between boosts (0 = no cooldown, only applies when max_boosts > 1)");
         lines.add("      boost_cooldown: 0");
         lines.add("      # Boost sound effect - can be any sound from https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
         lines.add("      # Examples: ENTITY_BAT_TAKEOFF, ENTITY_FIREWORK_ROCKET_BLAST, ITEM_ELYTRA_FLYING");
         lines.add("      sound: " + boostSound);
         lines.add("    ");
         lines.add("    # F-key specific settings (only used when activation_mode: f_key)");
         lines.add("    f_key:");
         lines.add("      # Launch strength when pressing F key (1.5 = ~14-15 blocks upward)");
         lines.add("      launch_strength: " + fKeyLaunchStrength);
         String content = String.join(System.lineSeparator(), lines) + System.lineSeparator();
         Files.writeString(configFile.toPath(), content, StandardCharsets.UTF_8);
         plugin.getLogger().info("Generated v1.5 config.");
      } catch (IOException var26) {
         plugin.getLogger().severe("Failed to generate v1.5 config: " + var26.getMessage());
      }
   }

   private static void createBackup(JavaPlugin plugin, File configFile) {
      BackupUtil.backupFile(plugin, configFile, "config/config.yml");
   }

   private static void createBackupAndGenerateNew(JavaPlugin plugin, File configFile) {
      createBackup(plugin, configFile);
      generateModernConfig(
         plugin,
         configFile,
         "en",
         "double_jump",
         100,
         2,
         "forward",
         "world",
         "auto",
         0,
         64,
         0,
         0,
         0,
         0,
         "ENTITY_BAT_TAKEOFF",
         true,
         false,
         true,
         true,
         true,
         false,
         1.5
      );
   }

   private static enum ConfigVersion {
      V1_2,
      V1_3,
      MODERN,
      UNKNOWN;
   }
}
