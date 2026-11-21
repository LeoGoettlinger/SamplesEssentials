package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Generated;

public class MobCombatSettingsConfig extends ConfigurationFile {
   private static boolean doNaturalMobSpawning;
   private static boolean doSpawnersSpawnEliteMobs;
   private static double aggressiveMobConversionPercentage;
   private static int naturalEliteMobLevelCap;
   private static boolean doEliteArmor;
   private static boolean doEliteHelmets;
   private static boolean enableVisualEffectsForNaturalMobs;
   private static boolean disableVisualEffectsForSpawnerMobs;
   private static boolean enableWarningVisualEffects;
   private static boolean enableDeathMessages;
   private static boolean displayDamageOnHit;
   private static boolean useDistanceBasedNaturalEliteLevels;
   private static List<String> distanceBasedNaturalEliteLevelWorlds;
   private static double blockDistanceBetweenLevelIncrements;
   @Deprecated
   private static boolean increaseDifficultyWithSpawnDistance;
   @Deprecated
   private static double distanceToIncrement;
   @Deprecated
   private static double levelToIncrement;
   private static boolean obfuscateMobPowers;
   private static double damageToEliteMultiplier;
   private static double damageToPlayerMultiplier;
   private static boolean showCustomBossLocation;
   private static String bossLocationMessage;
   private static List<String> commandsOnDeath;
   private static String bossKillParticipationMessage;
   private static boolean regenerateCustomBossHealthOnCombatEnd;
   private static String defaultOtherWorldBossLocationMessage;
   private static String weakTextColor;
   private static String resistTextColor;
   private static String weakText;
   private static String resistText;
   private static boolean doWeakEffect;
   private static boolean doResistEffect;
   private static double normalizedBaselineDamage;
   private static double normalizedBaselineHealth;
   private static double normalizedDamageToEliteMultiplier;
   private static double normalizedDamageToPlayerMultiplier;
   private static boolean normalizeRegionalBosses;
   private static boolean useScaledCombatForNaturalElites;
   private static double scaledDamageToEliteMultiplier;
   private static double scaledDamageToPlayerMultiplier;
   private static boolean useRecommendedHealthScaling;
   private static String scaledStandardLevelSymbol;
   private static String scaledHighThreatLevelSymbol;
   private static String fullHealMessage;
   private static double resistanceDamageMultiplier;
   private static double strengthDamageMultiplier;
   private static double weaknessDamageMultiplier;
   private static double blockingDamageReduction;
   private static boolean displayVisualHealthBars;
   private static boolean displayNumericHealth;
   private static boolean displayBossBarForHighMultiplier;
   private static double bossBarHealthMultiplierThreshold;
   private static int combatDisplayTimeoutSeconds;
   private static boolean useFixedHealthBarSize;
   private static String healPopupFormat;
   private static String xpPopupFormat;
   private static String healthDisplaySeparator;
   private static String invulnerablePopupText;
   private static String fallbackBossBarName;
   private static boolean autoclickerThrottleEnabled;
   private static int autoclickerThrottleMaxHitsPerSecond;
   private static int autoclickerThrottlePenaltySeconds;
   private static String autoclickerThrottleMessage;
   private static int damageIndicatorParticleCap;
   private static double maximumCriticalStrikeChance = 0.4;
   private static MobCombatSettingsConfig instance;

   public MobCombatSettingsConfig() {
      super("MobCombatSettings.yml");
   }

   public MobCombatSettingsConfig(File file) {
      super(file);
   }

   public static void toggleNaturalMobSpawning(boolean enabled) {
      doNaturalMobSpawning = enabled;
      instance.fileConfiguration.set("doNaturalEliteMobSpawning", enabled);
      ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
   }

   public static boolean isUseDistanceBasedNaturalEliteLevelsForWorld(String worldName) {
      return useDistanceBasedNaturalEliteLevels && isWorldAllowedForDistanceBasedNaturalEliteLevels(worldName, distanceBasedNaturalEliteLevelWorlds);
   }

   static boolean isWorldAllowedForDistanceBasedNaturalEliteLevels(String worldName, List<String> configuredWorlds) {
      if (configuredWorlds == null || configuredWorlds.isEmpty()) {
         return true;
      } else {
         return worldName != null && !worldName.isBlank()
            ? configuredWorlds.stream()
               .filter(Objects::nonNull)
               .map(String::trim)
               .anyMatch(configuredWorld -> !configuredWorld.isEmpty() && configuredWorld.equalsIgnoreCase(worldName))
            : false;
      }
   }

   @Override
   public void initializeValues() {
      instance = this;
      this.fileConfiguration.set("proximityBossBarHealthMultiplierThreshold", null);
      doNaturalMobSpawning = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if naturally spawned elites will spawn. Note: event mobs like the zombie king are not naturally spawned elites! You will have to disable events if you want to disable event bosses."
         ),
         this.fileConfiguration,
         "doNaturalEliteMobSpawning",
         true
      );
      doSpawnersSpawnEliteMobs = ConfigurationEngine.setBoolean(
         List.of("Sets if spawns spawned from mob spawners can be converted to elites. Not recommended!"),
         this.fileConfiguration,
         "doSpawnersSpawnEliteMobs",
         false
      );
      aggressiveMobConversionPercentage = ConfigurationEngine.setDouble(
         List.of("Sets the percentage of naturally spawned mobs that get converted to elite mobs."), this.fileConfiguration, "eliteMobsSpawnPercentage", 0.05
      );
      naturalEliteMobLevelCap = ConfigurationEngine.setInt(
         List.of(
            "Sets the maximum level elites can spawn at.",
            "Note: elite mob level is based on player skill levels and gear. The skill system scales up to level 100."
         ),
         this.fileConfiguration,
         "naturalEliteMobsLevelCap",
         120
      );
      doEliteArmor = ConfigurationEngine.setBoolean(
         List.of("Sets if elites will wear armor based on their level. This is for visual purposes only and does not affect combat."),
         this.fileConfiguration,
         "doElitesWearArmor",
         true
      );
      doEliteHelmets = ConfigurationEngine.setBoolean(
         List.of("Sets if elites will wear helmets based on their level. This will prevent them from easily burning away during the daytime."),
         this.fileConfiguration,
         "doElitesWearHelmets",
         true
      );
      enableVisualEffectsForNaturalMobs = ConfigurationEngine.setBoolean(
         List.of("Sets if elites will have visual trails around them warning players about what players they have."),
         this.fileConfiguration,
         "doNaturalEliteMobVisualEffects",
         true
      );
      disableVisualEffectsForSpawnerMobs = ConfigurationEngine.setBoolean(
         List.of("Sets if elites spawned from spawners will do visual effects."), this.fileConfiguration, "doSpawnerEliteMobVisualEffects", false
      );
      enableWarningVisualEffects = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if some powers will do the warning phase of the power. This is very important as warning phases usually mean the power can be dodged, and the visual lets players know where to dodge to."
         ),
         this.fileConfiguration,
         "doPowerBuildupVisualEffects",
         true
      );
      enableDeathMessages = ConfigurationEngine.setBoolean(
         List.of("Sets if custom death messages will be used when players die from elites."), this.fileConfiguration, "doCustomEliteMobsDeathMessages", true
      );
      displayDamageOnHit = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will show damage indicators for damage done to elites."), this.fileConfiguration, "doDisplayMobDamageOnHit", true
      );
      useDistanceBasedNaturalEliteLevels = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if naturally spawned elite mobs use fixed distance-based levels instead of EliteMobs' normal player-aware scaling.",
            "This is disabled by default because EliteMobs scales much better to players without this feature.",
            "When enabled, natural elite levels are based only on their block distance from the world's spawn and the blockDistanceBetweenLevelIncrements setting.",
            "This only affects naturally spawned elites. Event mobs, custom bosses, regional bosses, instanced bosses and spawner elites do not use this setting.",
            "Consequences of enabling this:",
            "- Natural elites stop scaling to player progression in the normal EliteMobs way.",
            "- Scaled combat for natural elites no longer applies to these mobs because fixed world level matters again.",
            "- High-level players can farm predictable high-level regions.",
            "- Low-level players can be locked out of most of the map.",
            "- Players can outgrow their base area and be forced to move because nearby mobs stop giving useful progress.",
            "- Building location becomes a combat-balance decision.",
            "- Random teleport systems can place players in impossible or highly farmable level bands.",
            "- Moving world spawn reshuffles the level map.",
            "- Each world uses its own spawn point, so Nether, End and custom worlds can have surprising level rings.",
            "- The natural elite level cap creates a far-distance plateau where everything past the cap radius has the capped level.",
            "- WorldGuard min/max level flags can still clamp the calculated level.",
            "- Loot, XP and economy rewards tied to mob level become easier to target by coordinates.",
            "- Anti-farm protections still help but do not remove the incentive to farm fixed high-level regions."
         ),
         this.fileConfiguration,
         "useDistanceBasedNaturalEliteLevels",
         false
      );
      distanceBasedNaturalEliteLevelWorlds = ConfigurationEngine.setList(
         List.of(
            "Sets which worlds use distance-based natural elite levels when useDistanceBasedNaturalEliteLevels is enabled.",
            "Leave this empty to apply distance-based natural elite levels to all valid worlds.",
            "Add exact world names here to limit the feature to only those worlds. Matching is case-insensitive.",
            "This still only affects naturally spawned elites. Event mobs, custom bosses, regional bosses, instanced bosses and spawner elites do not use this setting.",
            "Example:",
            "- world"
         ),
         this.file,
         this.fileConfiguration,
         "distanceBasedNaturalEliteLevelWorlds",
         Collections.emptyList(),
         false
      );
      blockDistanceBetweenLevelIncrements = Math.max(
         1.0,
         ConfigurationEngine.setDouble(
            List.of(
               "Sets the block distance between level increments for fixed distance-based natural elite levels.",
               "If this is 100, natural elite level increases by 1 every 100 blocks from the world's spawn.",
               "With the default of 100, a naturally spawned elite 10,000 blocks from spawn is level 100 before the natural elite level cap and WorldGuard limits are applied."
            ),
            this.fileConfiguration,
            "blockDistanceBetweenLevelIncrements",
            100.0
         )
      );
      increaseDifficultyWithSpawnDistance = useDistanceBasedNaturalEliteLevels;
      distanceToIncrement = blockDistanceBetweenLevelIncrements;
      levelToIncrement = 1.0;
      obfuscateMobPowers = ConfigurationEngine.setBoolean(
         List.of("Sets if the powers of elites will be hidden until they enter combat. This is recommended for performance reasons."),
         this.fileConfiguration,
         "hideEliteMobPowersUntilAggro",
         true
      );
      damageToEliteMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the multiplier for the damage dealt to all bosses spawned by EliteMobs, except those using the normalized damage system (regional dungeon bosses). Higher values increase the damage dealt, making bosses easier to kill.",
            "2.0 = 200%, 0.5 = 50%"
         ),
         this.fileConfiguration,
         "damageToEliteMobMultiplierV2",
         1.0
      );
      damageToPlayerMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the multiplier for the damage dealt to players by elites. Higher values increase the amount of damage dealt by bosses, except those using the normalized damage system (regional dungeon bosses), making bosses hit harder.",
            "2.0 = 200%, 0.5 = 50%"
         ),
         this.fileConfiguration,
         "damageToPlayerMultiplierV2",
         1.0
      );
      showCustomBossLocation = ConfigurationEngine.setBoolean(
         List.of("Sets if special bosses can be tracked."), this.fileConfiguration, "showCustomBossLocation", true
      );
      bossLocationMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players to track a boss location."),
         this.file,
         this.fileConfiguration,
         "bossLocationMessage",
         "&7[EM] &2[Click to track!]",
         true
      );
      commandsOnDeath = ConfigurationEngine.setList(
         List.of(
            "Sets the commands that run when an elite dies. Valid placeholders are:",
            "$level for the level of the boss",
            "$name for the name of the boss",
            "$players will make the command run for each player that participated in the kill. As an example, if Bob and Steve killed a boss, 'broadcast $players killed the boss!' will run 'bob killed the boss' and 'steve killed the boss!'"
         ),
         this.file,
         this.fileConfiguration,
         "commandsOnEliteMobDeath",
         Collections.emptyList(),
         false
      );
      bossKillParticipationMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players that participate in big boss kills."),
         this.file,
         this.fileConfiguration,
         "bossKillParticipationMessage",
         "&eYour damage: &2$playerDamage",
         true
      );
      regenerateCustomBossHealthOnCombatEnd = ConfigurationEngine.setBoolean(
         List.of("Sets if bosses regenerate health when they go out of combat. Strongly recommended."),
         this.fileConfiguration,
         "regenerateCustomBossHealthOnCombatEnd",
         true
      );
      defaultOtherWorldBossLocationMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players that are trying to track bosses currently in a different world."),
         this.file,
         this.fileConfiguration,
         "defaultOtherWorldBossLocationMessage",
         "$name: In different world!",
         true
      );
      weakTextColor = ConfigurationEngine.setString(
         List.of("Sets the prefix added to damage indicators when players hit a boss with something that boss is weak against."),
         this.file,
         this.fileConfiguration,
         "weakTextColor",
         "&9",
         false
      );
      resistTextColor = ConfigurationEngine.setString(
         List.of("Sets the prefix added to damage indicators when players hit a boss with something that boss is strong against."),
         this.file,
         this.fileConfiguration,
         "resistTextColor",
         "&c",
         false
      );
      weakText = ConfigurationEngine.setString(
         List.of("Sets the message that appears when players hit the boss with something that boss is weak against."),
         this.file,
         this.fileConfiguration,
         "weakText",
         "&9&lWeak!",
         true
      );
      resistText = ConfigurationEngine.setString(
         List.of("Sets the message that appears when players hit the boss with something that boss is strong against."),
         this.file,
         this.fileConfiguration,
         "resistText",
         "&c&lResist!",
         true
      );
      doWeakEffect = ConfigurationEngine.setBoolean(
         List.of("Sets if visuals will be used to show that a boss is weak against an attack."), this.fileConfiguration, "doWeakEffect", true
      );
      doResistEffect = ConfigurationEngine.setBoolean(
         List.of("Sets if visuals will be used to show that a boss is strong against an attack."), this.fileConfiguration, "doResistEffect", true
      );
      normalizedDamageToEliteMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the multiplier for the damage dealt to bosses using the normalized damage system (regional dungeon bosses). Higher values increase the damage dealt, making bosses easier to kill.",
            "2.0 = 200%, 0.5 = 50%"
         ),
         this.fileConfiguration,
         "normalizedDamageToEliteMultiplier",
         1.0
      );
      normalizedDamageToPlayerMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the multiplier for the damage dealt to players by bosses using the normalized damage system (regional dungeon bosses). Higher values increase the amount of damage dealt by bosses, making bosses hit harder.",
            "2.0 = 200%, 0.5 = 50%"
         ),
         this.fileConfiguration,
         "normalizedDamageToPlayerMultiplier",
         1.0
      );
      normalizedBaselineDamage = ConfigurationEngine.setDouble(
         List.of("Sets the baseline damage for custom bosses using the normalized damage (usually regional bosses)."),
         this.fileConfiguration,
         "normalizedBaselineDamageV3",
         3.0
      );
      normalizedBaselineHealth = ConfigurationEngine.setDouble(
         List.of("Sets the baseline health for custom bosses using the normalized health (usually regional bosses)."),
         this.fileConfiguration,
         "normalizedRegionalBossBaselineHealthV3",
         4.0
      );
      normalizeRegionalBosses = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if regional bosses will used the normalized combat system.",
            "This is very strongly recommended, and premade content will not be balanced properly if modified."
         ),
         this.fileConfiguration,
         "normalizeRegionalBosses",
         true
      );
      useScaledCombatForNaturalElites = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if naturally spawned elite mobs use scaled combat.",
            "Scaled combat simulates the boss at the player's level, so gear still matters but level doesn't.",
            "This makes overworld elites feel equally fair for all players regardless of progression.",
            "Dungeon bosses use normalized combat and are unaffected by this setting."
         ),
         this.fileConfiguration,
         "useScaledCombatForNaturalElites",
         true
      );
      scaledDamageToEliteMultiplier = ConfigurationEngine.setDouble(
         List.of("Sets the multiplier for player damage dealt to bosses using scaled combat.", "2.0 = 200%, 0.5 = 50%"),
         this.fileConfiguration,
         "scaledDamageToEliteMultiplier",
         1.0
      );
      scaledDamageToPlayerMultiplier = ConfigurationEngine.setDouble(
         List.of("Sets the multiplier for damage dealt to players by bosses using scaled combat.", "2.0 = 200%, 0.5 = 50%"),
         this.fileConfiguration,
         "scaledDamageToPlayerMultiplier",
         1.0
      );
      useRecommendedHealthScaling = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether naturally spawned elite mobs use the recommended EliteMobs 10 health scaling values.",
            "When true, natural elites use the current recommended scaling model. This is strongly recommended.",
            "When false, natural elite HP and player damage numbers against natural elites are side-loaded to use much smaller legacy-style values closer to EliteMobs 9.",
            "This exists for backwards compatibility with servers that built integrations around older EliteMobs combat numbers.",
            "False is no longer recommended because it makes player progression feel flatter and players will feel their power growth much less clearly.",
            "This setting does not change normalized regional boss combat."
         ),
         this.fileConfiguration,
         "useRecommendedHealthScaling",
         true
      );
      scaledStandardLevelSymbol = ConfigurationEngine.setString(
         List.of(
            "Sets the symbol shown in place of a numeric level for scaled entities with no custom health multiplier.",
            "This is only used for scaled entities.",
            "Default: 「⚔」"
         ),
         this.file,
         this.fileConfiguration,
         "scaledStandardLevelSymbol",
         "「⚔」",
         true
      );
      scaledHighThreatLevelSymbol = ConfigurationEngine.setString(
         List.of(
            "Sets the symbol shown in place of a numeric level for scaled entities with a health multiplier above 1.",
            "This is only used for scaled entities and is intended to signal higher event-style threat.",
            "Default: ☠"
         ),
         this.file,
         this.fileConfiguration,
         "scaledHighThreatLevelSymbol",
         "☠",
         true
      );
      fullHealMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a boss heals from going out of combat."),
         this.file,
         this.fileConfiguration,
         "fullHealMessage",
         "&2FULL HEAL!",
         true
      );
      resistanceDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the damage reduction per resistance level applied to elite mob damage.",
            "Resistance I = (1 * this value), Resistance II = (2 * this value), etc.",
            "Default of 0.2 matches vanilla: Resistance I = 20% reduction, Resistance V = 100% reduction (immune)."
         ),
         this.fileConfiguration,
         "resistanceDamageMultiplierV2",
         0.2
      );
      strengthDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the outgoing damage bonus per strength level for players and elites.",
            "Strength I = + (1 * this value), Strength II = + (2 * this value), etc.",
            "Default of 0.2 means Strength I = +20% damage."
         ),
         this.fileConfiguration,
         "strengthDamageMultiplierV2",
         0.2
      );
      weaknessDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the outgoing damage penalty per weakness level for players and elites.",
            "Weakness I = - (1 * this value), Weakness II = - (2 * this value), etc.",
            "Default of 0.2 means Weakness I = -20% damage."
         ),
         this.fileConfiguration,
         "weaknessDamageMultiplierV2",
         0.2
      );
      blockingDamageReduction = ConfigurationEngine.setDouble(
         List.of("Sets the multiplier applied to damage reduction when a player is holding up a shield for melee attacks (powers excluded)."),
         this.fileConfiguration,
         "blockingDamageReduction",
         0.8
      );
      displayVisualHealthBars = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if visual health bars (using bar characters) will be displayed above elite mobs during combat.",
            "The number of bars scales with the health multiplier of the boss."
         ),
         this.fileConfiguration,
         "displayVisualHealthBars",
         true
      );
      displayNumericHealth = ConfigurationEngine.setBoolean(
         List.of("Sets if numeric health values (current/max) will be displayed above the visual health bars."),
         this.fileConfiguration,
         "displayNumericHealth",
         true
      );
      displayBossBarForHighMultiplier = ConfigurationEngine.setBoolean(
         List.of("Sets if boss bars (at the top of the screen) will be displayed while eligible bosses actively target a player."),
         this.fileConfiguration,
         "displayBossBarForHighMultiplier",
         true
      );
      bossBarHealthMultiplierThreshold = ConfigurationEngine.setDouble(
         List.of(
            "Sets the health multiplier threshold above which boss bars will be displayed when entering combat.",
            "Bosses with a health multiplier greater than this value show a boss bar only to their current target."
         ),
         this.fileConfiguration,
         "bossBarHealthMultiplierThreshold",
         1.0
      );
      combatDisplayTimeoutSeconds = ConfigurationEngine.setInt(
         List.of(
            "Sets how many seconds after the last combat action the overhead visual and numeric health displays remain visible.",
            "Top-of-screen boss bars instead follow the boss's current target."
         ),
         this.fileConfiguration,
         "combatDisplayTimeoutSeconds",
         30
      );
      useFixedHealthBarSize = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if the health bar should always be a fixed size (10 bars) regardless of boss health multiplier.",
            "When false (default), the health bar scales with the boss health multiplier, adding more bars and rows for tankier bosses.",
            "When true, the health bar is always a single row of 10 bars."
         ),
         this.fileConfiguration,
         "useFixedHealthBarSize",
         false
      );
      healPopupFormat = ConfigurationEngine.setString(
         List.of("Format for the heal popup text when a boss partially heals.", "Use $amount for the heal amount. Color is applied automatically."),
         this.file,
         this.fileConfiguration,
         "healPopupFormat",
         "+$amount HP",
         true
      );
      xpPopupFormat = ConfigurationEngine.setString(
         List.of("Format for the XP gain popup text.", "Use $amount for the XP amount."),
         this.file,
         this.fileConfiguration,
         "xpPopupFormat",
         "+$amount ✦XP",
         true
      );
      healthDisplaySeparator = ConfigurationEngine.setString(
         List.of("Separator between current and max health in the numeric health display."),
         this.file,
         this.fileConfiguration,
         "healthDisplaySeparator",
         " &7/ ",
         true
      );
      invulnerablePopupText = ConfigurationEngine.setString(
         List.of("Text shown when a player tries to damage an elite while leash-return protection is active."),
         this.file,
         this.fileConfiguration,
         "invulnerablePopupText",
         "&9&lINVULNERABLE",
         true
      );
      fallbackBossBarName = ConfigurationEngine.setString(
         List.of("Fallback boss-bar name used when an elite has no configured display name."),
         this.file,
         this.fileConfiguration,
         "fallbackBossBarName",
         "Elite Boss",
         true
      );
      autoclickerThrottleEnabled = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if EliteMobs will penalize players who hit elites faster than legitimate clicking allows (autoclickers).",
            "When a player lands more than autoclickerThrottleMaxHitsPerSecond melee hits on elites within one second, they are locked out of dealing damage to elites for autoclickerThrottlePenaltySeconds seconds and warned in chat."
         ),
         this.fileConfiguration,
         "autoclickerThrottleEnabled",
         true
      );
      autoclickerThrottleMaxHitsPerSecond = ConfigurationEngine.setInt(
         List.of("Sets the maximum number of melee hits a player can land on elites within a one-second sliding window before the throttle fires."),
         this.fileConfiguration,
         "autoclickerThrottleMaxHitsPerSecond",
         5
      );
      autoclickerThrottlePenaltySeconds = ConfigurationEngine.setInt(
         List.of("Sets how many seconds a player is locked out of dealing damage to elites after tripping the autoclicker throttle."),
         this.fileConfiguration,
         "autoclickerThrottlePenaltySeconds",
         5
      );
      autoclickerThrottleMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when the autoclicker throttle fires. Placeholder: $seconds for the remaining penalty duration."),
         this.file,
         this.fileConfiguration,
         "autoclickerThrottleMessage",
         "&c[EliteMobs] &7You are attacking too fast. Damage to elites disabled for &e$seconds&7s.",
         true
      );
      damageIndicatorParticleCap = ConfigurationEngine.setInt(
         List.of(
            "Caps the number of red 'damage indicator' particles the server is allowed to send the client per melee hit.",
            "Vanilla spawns one particle per 2 HP of damage dealt, packed into a single packet. With EliteMobs scaling player damage into the millions at high levels, that single packet asks the client to instantiate hundreds of thousands of particles locally, which freezes the client.",
            "Set to a small positive value (default 16) to collapse the burst into a normal-looking puff. The cap is purely cosmetic: damage, sounds, health bars, and every other game state are unaffected.",
            "Set to 0 (or any negative value) to disable the cap and let vanilla decide."
         ),
         this.fileConfiguration,
         "damageIndicatorParticleCap",
         16
      );
      maximumCriticalStrikeChance = ConfigurationEngine.setDouble(
         List.of(
            "Sets the highest critical strike chance a player can reach, from 0.0 to 1.0.",
            "Critical strike chance adds up across held and equipped items, so without a cap it is possible to crit on every single hit.",
            "Past 0.5 the critical hit becomes the typical hit, so it stops standing out as a critical strike at all while still adding damage.",
            "Values above 0.5 are not recommended for that reason."
         ),
         this.fileConfiguration,
         "maximumCriticalStrikeChance",
         0.4
      );
      NMSAdapter adapter = NMSManager.getAdapter();
      if (adapter != null) {
         adapter.setDamageIndicatorParticleCap(damageIndicatorParticleCap);
      }
   }

   @Generated
   public static boolean isDoNaturalMobSpawning() {
      return doNaturalMobSpawning;
   }

   @Generated
   public static boolean isDoSpawnersSpawnEliteMobs() {
      return doSpawnersSpawnEliteMobs;
   }

   @Generated
   public static double getAggressiveMobConversionPercentage() {
      return aggressiveMobConversionPercentage;
   }

   @Generated
   public static int getNaturalEliteMobLevelCap() {
      return naturalEliteMobLevelCap;
   }

   @Generated
   public static boolean isDoEliteArmor() {
      return doEliteArmor;
   }

   @Generated
   public static boolean isDoEliteHelmets() {
      return doEliteHelmets;
   }

   @Generated
   public static boolean isEnableVisualEffectsForNaturalMobs() {
      return enableVisualEffectsForNaturalMobs;
   }

   @Generated
   public static boolean isDisableVisualEffectsForSpawnerMobs() {
      return disableVisualEffectsForSpawnerMobs;
   }

   @Generated
   public static boolean isEnableWarningVisualEffects() {
      return enableWarningVisualEffects;
   }

   @Generated
   public static boolean isEnableDeathMessages() {
      return enableDeathMessages;
   }

   @Generated
   public static boolean isDisplayDamageOnHit() {
      return displayDamageOnHit;
   }

   @Generated
   public static boolean isUseDistanceBasedNaturalEliteLevels() {
      return useDistanceBasedNaturalEliteLevels;
   }

   @Generated
   public static List<String> getDistanceBasedNaturalEliteLevelWorlds() {
      return distanceBasedNaturalEliteLevelWorlds;
   }

   @Generated
   public static double getBlockDistanceBetweenLevelIncrements() {
      return blockDistanceBetweenLevelIncrements;
   }

   @Deprecated
   @Generated
   public static boolean isIncreaseDifficultyWithSpawnDistance() {
      return increaseDifficultyWithSpawnDistance;
   }

   @Deprecated
   @Generated
   public static double getDistanceToIncrement() {
      return distanceToIncrement;
   }

   @Deprecated
   @Generated
   public static double getLevelToIncrement() {
      return levelToIncrement;
   }

   @Generated
   public static boolean isObfuscateMobPowers() {
      return obfuscateMobPowers;
   }

   @Generated
   public static double getDamageToEliteMultiplier() {
      return damageToEliteMultiplier;
   }

   @Generated
   public static double getDamageToPlayerMultiplier() {
      return damageToPlayerMultiplier;
   }

   @Generated
   public static boolean isShowCustomBossLocation() {
      return showCustomBossLocation;
   }

   @Generated
   public static String getBossLocationMessage() {
      return bossLocationMessage;
   }

   @Generated
   public static List<String> getCommandsOnDeath() {
      return commandsOnDeath;
   }

   @Generated
   public static String getBossKillParticipationMessage() {
      return bossKillParticipationMessage;
   }

   @Generated
   public static boolean isRegenerateCustomBossHealthOnCombatEnd() {
      return regenerateCustomBossHealthOnCombatEnd;
   }

   @Generated
   public static String getDefaultOtherWorldBossLocationMessage() {
      return defaultOtherWorldBossLocationMessage;
   }

   @Generated
   public static String getWeakTextColor() {
      return weakTextColor;
   }

   @Generated
   public static String getResistTextColor() {
      return resistTextColor;
   }

   @Generated
   public static String getWeakText() {
      return weakText;
   }

   @Generated
   public static String getResistText() {
      return resistText;
   }

   @Generated
   public static boolean isDoWeakEffect() {
      return doWeakEffect;
   }

   @Generated
   public static boolean isDoResistEffect() {
      return doResistEffect;
   }

   @Generated
   public static double getNormalizedBaselineDamage() {
      return normalizedBaselineDamage;
   }

   @Generated
   public static double getNormalizedBaselineHealth() {
      return normalizedBaselineHealth;
   }

   @Generated
   public static double getNormalizedDamageToEliteMultiplier() {
      return normalizedDamageToEliteMultiplier;
   }

   @Generated
   public static double getNormalizedDamageToPlayerMultiplier() {
      return normalizedDamageToPlayerMultiplier;
   }

   @Generated
   public static boolean isNormalizeRegionalBosses() {
      return normalizeRegionalBosses;
   }

   @Generated
   public static boolean isUseScaledCombatForNaturalElites() {
      return useScaledCombatForNaturalElites;
   }

   @Generated
   public static double getScaledDamageToEliteMultiplier() {
      return scaledDamageToEliteMultiplier;
   }

   @Generated
   public static double getScaledDamageToPlayerMultiplier() {
      return scaledDamageToPlayerMultiplier;
   }

   @Generated
   public static boolean isUseRecommendedHealthScaling() {
      return useRecommendedHealthScaling;
   }

   @Generated
   public static String getScaledStandardLevelSymbol() {
      return scaledStandardLevelSymbol;
   }

   @Generated
   public static String getScaledHighThreatLevelSymbol() {
      return scaledHighThreatLevelSymbol;
   }

   @Generated
   public static String getFullHealMessage() {
      return fullHealMessage;
   }

   @Generated
   public static double getResistanceDamageMultiplier() {
      return resistanceDamageMultiplier;
   }

   @Generated
   public static double getStrengthDamageMultiplier() {
      return strengthDamageMultiplier;
   }

   @Generated
   public static double getWeaknessDamageMultiplier() {
      return weaknessDamageMultiplier;
   }

   @Generated
   public static double getBlockingDamageReduction() {
      return blockingDamageReduction;
   }

   @Generated
   public static boolean isDisplayVisualHealthBars() {
      return displayVisualHealthBars;
   }

   @Generated
   public static boolean isDisplayNumericHealth() {
      return displayNumericHealth;
   }

   @Generated
   public static boolean isDisplayBossBarForHighMultiplier() {
      return displayBossBarForHighMultiplier;
   }

   @Generated
   public static double getBossBarHealthMultiplierThreshold() {
      return bossBarHealthMultiplierThreshold;
   }

   @Generated
   public static int getCombatDisplayTimeoutSeconds() {
      return combatDisplayTimeoutSeconds;
   }

   @Generated
   public static boolean isUseFixedHealthBarSize() {
      return useFixedHealthBarSize;
   }

   @Generated
   public static String getHealPopupFormat() {
      return healPopupFormat;
   }

   @Generated
   public static String getXpPopupFormat() {
      return xpPopupFormat;
   }

   @Generated
   public static String getHealthDisplaySeparator() {
      return healthDisplaySeparator;
   }

   @Generated
   public static String getInvulnerablePopupText() {
      return invulnerablePopupText;
   }

   @Generated
   public static String getFallbackBossBarName() {
      return fallbackBossBarName;
   }

   @Generated
   public static boolean isAutoclickerThrottleEnabled() {
      return autoclickerThrottleEnabled;
   }

   @Generated
   public static int getAutoclickerThrottleMaxHitsPerSecond() {
      return autoclickerThrottleMaxHitsPerSecond;
   }

   @Generated
   public static int getAutoclickerThrottlePenaltySeconds() {
      return autoclickerThrottlePenaltySeconds;
   }

   @Generated
   public static String getAutoclickerThrottleMessage() {
      return autoclickerThrottleMessage;
   }

   @Generated
   public static int getDamageIndicatorParticleCap() {
      return damageIndicatorParticleCap;
   }

   @Generated
   public static double getMaximumCriticalStrikeChance() {
      return maximumCriticalStrikeChance;
   }
}
