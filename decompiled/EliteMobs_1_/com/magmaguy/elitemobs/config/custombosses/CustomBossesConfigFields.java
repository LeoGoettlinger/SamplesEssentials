package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.BossType;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolRoute;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.powers.scripts.loading.ScriptValueNormalizer;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class CustomBossesConfigFields extends CustomConfigFields {
   private static final Map<String, CustomBossesConfigFields> regionalElites = new HashMap<>();
   private CustomLootTable customLootTable = null;
   private EntityType entityType = EntityType.ZOMBIE;
   private String behavior;
   private String name = "Default Name";
   private String level = "dynamic";
   private boolean isPersistent = false;
   private double healthMultiplier = 1.0;
   private double damageMultiplier = 1.0;
   private boolean frozen = false;
   private boolean reinforcement = false;
   private List<String> onDeathCommands = null;
   private List<String> onSpawnCommands = null;
   private List<String> onCombatEnterCommands = null;
   private List<String> onCombatLeaveCommands = null;
   private List<Object> uniqueLootList = null;
   private List<Object> powers = null;
   private List<String> onDamageMessages = null;
   private List<String> onDamagedMessages = null;
   private List<String> trails = null;
   private List<String> phases = null;
   private String locationMessage = null;
   private String mountedEntity = null;
   private String customModelMountPointID = null;
   private String spawnMessage = null;
   private String deathMessage = null;
   private List<String> deathMessages = null;
   private String escapeMessage = null;
   private boolean baby = false;
   private boolean dropsEliteMobsLoot = true;
   private boolean dropsVanillaLoot = true;
   private boolean dropsRandomLoot = true;
   private boolean dropsSkillXP = true;
   private String disguise = null;
   private String customDisguiseData = null;
   private String customModel = null;
   private boolean customModelExists = false;
   private int announcementPriority = 0;
   private Integer followDistance = 0;
   private int spawnCooldown = 0;
   private int timeout = 0;
   private double leashRadius = 0.0;
   private ItemStack helmet = null;
   private ItemStack chestplate = null;
   private ItemStack leggings = null;
   private ItemStack boots = null;
   private ItemStack mainHand = null;
   private ItemStack offHand = null;
   private boolean regionalBoss = false;
   private boolean cullReinforcements = true;
   private HashMap<Material, Double> damageModifiers = new HashMap<>();
   private boolean normalizedCombat = false;
   private boolean scaledCombat = false;
   private Double movementSpeedAttribute = null;
   private final AtomicLong regionalRevision = new AtomicLong();
   private volatile long savedRegionalRevision;
   private volatile long queuedRegionalRevision = -1L;
   private final AtomicReference<CustomBossesConfigFields.RegionalSave> pendingRegionalSave = new AtomicReference<>();
   private final AtomicBoolean regionalSaveScheduled = new AtomicBoolean();
   private List<String> onSpawnBlockStates = new ArrayList<>();
   private List<String> onRemoveBlockStates = new ArrayList<>();
   private boolean instanced = false;
   private String phaseSpawnLocation = null;
   private ConfigurationSection rawEliteScripts = null;
   private List<EliteScriptBlueprint> eliteScript = new ArrayList<>();
   private String song = null;
   private boolean alert = false;
   private boolean removeAfterDeath = false;
   private int slimeSize = 4;
   private boolean neutral = false;
   private String onKillMessage;
   private BossType bossType = BossType.NORMAL;
   private boolean classLoot;
   private String classLootDifficulty = "AUTO";
   private String classLootRank = "AUTO";
   private double scale = 1.0;
   private boolean silent;
   private boolean ai = true;
   private boolean alwaysShowName = false;
   private List<String> spawnLocations = new ArrayList<>();
   private PatrolRoute patrolRoute;

   public boolean isFilesOutOfSync() {
      return this.regionalRevision.get() != this.savedRegionalRevision;
   }

   public void setFilesOutOfSync(boolean dirty) {
      if (dirty) {
         this.regionalRevision.incrementAndGet();
      } else {
         throw new IllegalArgumentException("Regional state is acknowledged only after successful persistence");
      }
   }

   public void saveRegionalSpawnLocations(List<String> locations, boolean flush) {
      long revision = this.regionalRevision.get();
      if (flush || !this.regionalSaveScheduled.get() || this.queuedRegionalRevision != revision) {
         this.getWritableFileConfiguration().set("spawnLocations", locations);
         this.spawnLocations = List.copyOf(locations);
         this.pendingRegionalSave.set(new CustomBossesConfigFields.RegionalSave(revision, this.getWritableFileConfiguration().saveToString()));
         this.queuedRegionalRevision = revision;
         if (flush) {
            this.writePendingRegionalSave();
         } else if (this.regionalSaveScheduled.compareAndSet(false, true)) {
            try {
               Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
                  try {
                     this.writePendingRegionalSave();
                  } catch (RuntimeException var5) {
                     Logger.warn("Failed to save regional state for " + this.filename + ": " + var5.getMessage());
                  } finally {
                     this.regionalSaveScheduled.set(false);
                  }
               });
            } catch (Error | RuntimeException var6) {
               this.regionalSaveScheduled.set(false);
               throw var6;
            }
         }
      }
   }

   private synchronized void writePendingRegionalSave() {
      CustomBossesConfigFields.RegionalSave snapshot;
      while ((snapshot = this.pendingRegionalSave.getAndSet(null)) != null) {
         try {
            ConfigurationEngine.fileSaverSerialized(snapshot.yaml(), this.file);
            this.savedRegionalRevision = snapshot.revision();
         } catch (Error | RuntimeException var3) {
            this.pendingRegionalSave.compareAndSet(null, snapshot);
            throw var3;
         }
      }
   }

   public synchronized void discardQueuedRegionalSave() {
      this.pendingRegionalSave.set(null);
   }

   public CustomBossesConfigFields(String fileName, EntityType entityType, boolean isEnabled, String name, String level) {
      super(fileName, isEnabled);
      this.entityType = entityType;
      this.name = name;
      this.level = level;
   }

   public CustomBossesConfigFields(String fileName, boolean isEnabled) {
      super(fileName, isEnabled);
   }

   public String getName() {
      return this.name == null ? "Default Name" : this.name;
   }

   public String getCleanName(int level) {
      String cleanNameLevel;
      if (level == 0 && this.level.equalsIgnoreCase("dynamic")) {
         cleanNameLevel = "?";
      } else if (level == 0) {
         cleanNameLevel = level + "";
      } else if (this.level.equalsIgnoreCase("dynamic")) {
         cleanNameLevel = level + "";
      } else {
         cleanNameLevel = this.level;
      }

      return ChatColorConverter.convert(
         this.getName()
            .replace("$level", cleanNameLevel)
            .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + cleanNameLevel + "&2]&f"))
            .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + cleanNameLevel + "&6〗&f"))
            .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + cleanNameLevel + "&4』&f"))
            .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + cleanNameLevel + "&8〕&f")
            .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + cleanNameLevel + "&4」&f"))
      );
   }

   private boolean hasAnnouncementContent() {
      return this.hasText(this.locationMessage)
         || this.hasText(this.spawnMessage)
         || this.hasText(this.deathMessage)
         || this.hasText(this.escapeMessage)
         || this.deathMessages != null && !this.deathMessages.isEmpty();
   }

   private boolean hasText(String value) {
      return value != null && !value.isBlank();
   }

   public void runtimeSetLeashRadius(double leashRadius) {
      this.persistValue("leashRadius", leashRadius);
      this.leashRadius = leashRadius;
   }

   public double getDamageModifier(Material material) {
      return this.damageModifiers.get(material) == null ? 1.0 : this.damageModifiers.get(material);
   }

   public int getLevel() {
      if (this.level.equalsIgnoreCase("dynamic")) {
         return -1;
      } else {
         try {
            return Integer.valueOf(this.level);
         } catch (Exception var2) {
            Logger.warn("Regional Elite Mob level for " + this.getFilename() + " is neither numeric nor dynamic. Fix the configuration for it.");
            return 1;
         }
      }
   }

   public void setOnSpawnBlockStates(List<String> values) {
      List<String> snapshot = List.copyOf(values);
      this.persistValue("onSpawnBlockStates", snapshot);
      this.onSpawnBlockStates = snapshot;
   }

   public void setOnRemoveBlockStates(List<String> values) {
      List<String> snapshot = List.copyOf(values);
      this.persistValue("onRemoveBlockStates", snapshot);
      this.onRemoveBlockStates = snapshot;
   }

   private synchronized void persistValue(String key, Object value) {
      FileConfiguration writable = this.getWritableFileConfiguration();
      Object previous = writable.get(key);
      this.discardQueuedRegionalSave();
      writable.set(key, value);

      try {
         ConfigurationEngine.fileSaverSerialized(writable.saveToString(), this.file);
      } catch (Error | RuntimeException var6) {
         writable.set(key, previous);
         throw var6;
      }
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      boolean configuredEntityType = this.configHas("entityType");
      this.entityType = this.processEnum("entityType", this.entityType, configuredEntityType ? null : EntityType.ZOMBIE, EntityType.class, true);
      if (this.entityType == null && !configuredEntityType) {
         this.entityType = EntityType.ZOMBIE;
      }

      if (EliteMobProperties.getPluginData(this.entityType) == null) {
         Logger.warn("Disabling custom boss " + this.filename + ": entityType " + this.entityType + " is not a spawnable creature.");
         this.isEnabled = false;
      } else {
         this.behavior = this.processString("behavior", this.behavior, null, false);
         this.instanced = this.processBoolean("instanced", this.instanced, false, false);
         this.name = this.translatable(this.filename, "name", this.processString("name", this.name, "Default Name", true));
         this.level = this.processString("level", this.level, "dynamic", true);
         this.isPersistent = this.processBoolean("isPersistent", this.isPersistent, false, false);
         this.healthMultiplier = this.processDouble("healthMultiplier", this.healthMultiplier, 1.0, false);
         this.damageMultiplier = this.processDouble("damageMultiplier", this.damageMultiplier, 1.0, false);
         this.baby = this.processBoolean("isBaby", this.baby, false, false);
         this.dropsEliteMobsLoot = this.processBoolean("dropsEliteMobsLoot", this.dropsEliteMobsLoot, true, false);
         this.dropsVanillaLoot = this.processBoolean("dropsVanillaLoot", this.dropsVanillaLoot, true, false);
         this.dropsRandomLoot = this.processBoolean("dropsRandomLoot", this.dropsRandomLoot, true, false);
         this.dropsSkillXP = this.processBoolean("dropsSkillXP", this.dropsSkillXP, true, false);
         this.frozen = this.processBoolean("frozen", this.frozen, false, false);
         this.reinforcement = this.processBoolean("reinforcement", this.reinforcement, false, false);
         this.onDeathCommands = this.processStringList("onDeathCommands", this.onDeathCommands, new ArrayList<>(), false);
         this.onSpawnCommands = this.processStringList("onSpawnCommands", this.onSpawnCommands, new ArrayList<>(), false);
         this.onCombatEnterCommands = this.processStringList("onCombatEnterCommands", this.onCombatEnterCommands, new ArrayList<>(), false);
         this.onCombatLeaveCommands = this.processStringList("onCombatLeaveCommands", this.onCombatLeaveCommands, new ArrayList<>(), false);
         this.deathMessages = this.translatable(
            this.filename, "deathMessages", this.processStringList("deathMessages", this.deathMessages, new ArrayList<>(), false)
         );
         this.uniqueLootList = this.processList("uniqueLootList", this.uniqueLootList, new ArrayList<>(), false);
         this.customLootTable = new CustomLootTable(this);
         this.powers = this.processList("powers", this.powers, null, false);
         this.onDamageMessages = this.translatable(
            this.filename, "onDamageMessages", this.processStringList("onDamageMessages", this.onDamageMessages, new ArrayList<>(), false)
         );
         this.onDamagedMessages = this.translatable(
            this.filename, "onDamagedMessages", this.processStringList("onDamagedMessages", this.onDamagedMessages, new ArrayList<>(), false)
         );
         this.trails = this.processStringList("trails", this.trails, new ArrayList<>(), false);
         this.phases = this.processStringList("phases", this.phases, new ArrayList<>(), false);
         this.phaseSpawnLocation = this.processString("phaseSpawnLocation", this.phaseSpawnLocation, null, false);
         this.locationMessage = this.translatable(this.filename, "locationMessage", this.processString("locationMessage", this.locationMessage, null, false));
         this.mountedEntity = this.processString("mountedEntity", this.mountedEntity, null, false);
         if (this.mountedEntity != null && this.mountedEntity.equals(this.filename)) {
            Logger.warn(
               "Custom Boss "
                  + this.filename
                  + " has itself for a mount. This makes an infinite loop of the boss mounting itself. The boss mount will not be used for safety reasons."
            );
            this.mountedEntity = null;
         }

         this.customModelMountPointID = this.processString("customModelMountPointID", this.customModelMountPointID, null, false);
         this.spawnMessage = this.translatable(this.filename, "spawnMessage", this.processString("spawnMessage", this.spawnMessage, null, false));
         this.deathMessage = this.translatable(this.filename, "deathMessage", this.processString("deathMessage", this.deathMessage, null, false));
         this.escapeMessage = this.translatable(this.filename, "escapeMessage", this.processString("escapeMessage", this.escapeMessage, null, false));
         this.disguise = this.processString("disguise", this.disguise, null, false);
         this.customDisguiseData = this.processString("customDisguiseData", this.customDisguiseData, null, false);
         this.customModel = this.processString("customModel", this.customModel, null, false);
         this.announcementPriority = this.processInt("announcementPriority", this.announcementPriority, this.hasAnnouncementContent() ? 1 : 0, false);
         this.followDistance = this.processInt("followDistance", this.followDistance, 0, false);
         this.spawnCooldown = this.processInt("spawnCooldown", this.spawnCooldown, 0, false);
         this.timeout = this.processInt("timeout", this.timeout, 0, false);
         this.leashRadius = this.processDouble("leashRadius", this.leashRadius, 0.0, false);
         this.helmet = this.processItemStack("helmet", this.helmet, null, false);
         this.chestplate = this.processItemStack("chestplate", this.chestplate, null, false);
         this.leggings = this.processItemStack("leggings", this.leggings, null, false);
         this.boots = this.processItemStack("boots", this.boots, null, false);
         this.mainHand = this.processItemStack("mainHand", this.mainHand, null, false);
         this.offHand = this.processItemStack("offHand", this.offHand, null, false);
         this.regionalBoss = this.processBoolean("isRegionalBoss", this.regionalBoss, false, false);
         this.onSpawnBlockStates = this.processStringList("onSpawnBlockStates", this.onSpawnBlockStates, new ArrayList<>(), false);
         this.onRemoveBlockStates = this.processStringList("onRemoveBlockStates", this.onRemoveBlockStates, new ArrayList<>(), false);
         this.cullReinforcements = this.processBoolean("cullReinforcements", this.cullReinforcements, true, false);
         this.damageModifiers = this.processDamageModifiers("damageModifiers", this.damageModifiers);
         if (!MobCombatSettingsConfig.isNormalizeRegionalBosses() || !this.isRegionalBoss() && !this.isInstanced()) {
            this.normalizedCombat = this.processBoolean("normalizedCombat", this.normalizedCombat, false, false);
         } else {
            this.normalizedCombat = true;
         }

         this.scaledCombat = this.processBoolean("scaledCombat", this.scaledCombat, false, false);
         this.movementSpeedAttribute = this.processDouble("movementSpeedAttribute", this.movementSpeedAttribute, null, false);
         this.rawEliteScripts = this.fileConfiguration.getConfigurationSection("eliteScript");

         try {
            Map<String, Object> normalizedScripts = ScriptValueNormalizer.normalizeSection(this.rawEliteScripts);
            this.eliteScript = (List<EliteScriptBlueprint>)(normalizedScripts.isEmpty()
               ? new ArrayList<>()
               : EliteScriptBlueprint.parseBossScripts(normalizedScripts, this));
         } catch (Exception var6) {
            this.eliteScript = new ArrayList<>();
            Logger.warn("You have a script with invalid data! Script in " + this.filename + " is not valid.");
            var6.printStackTrace();
         }

         this.song = this.processString("song", this.song, null, false);
         this.alert = this.processBoolean("alert", this.alert, false, false);
         this.removeAfterDeath = this.processBoolean("removeAfterDeath", this.removeAfterDeath, false, false);
         this.slimeSize = this.processInt("slimeSize", this.slimeSize, 4, false);
         this.neutral = this.processBoolean("neutral", this.neutral, false, false);
         this.onKillMessage = ConfigurationEngine.setString(
            List.of(
               "Sets the message that is shown when a boss is killed",
               "Placeholders:",
               "$player - display name of the player (with prefix/suffix)",
               "$playerName - plain username of the player (no prefix/suffix)"
            ),
            this.file,
            this.fileConfiguration,
            "onKillMessage",
            null,
            true
         );
         String bossTypeString = ConfigurationEngine.setString(this.file, this.fileConfiguration, "bossType", "NORMAL", false);

         try {
            this.bossType = BossType.valueOf(bossTypeString.toUpperCase(Locale.ROOT));
         } catch (Exception var5) {
            Logger.warn("Boss type for boss " + this.filename + " is not a valid boss type!");
         }

         this.classLoot = this.processBoolean("classLoot", this.bossType != BossType.REINFORCEMENT, false, false);
         this.classLootDifficulty = this.processString("classLootDifficulty", "AUTO", "AUTO", false).toUpperCase(Locale.ROOT);
         this.classLootRank = this.processString("classLootRank", "AUTO", "AUTO", false).toUpperCase(Locale.ROOT);
         if (!Set.of("AUTO", "NORMAL", "HARD", "MYTHIC").contains(this.classLootDifficulty)) {
            Logger.warn("Invalid classLootDifficulty in " + this.filename + "; using AUTO.");
            this.classLootDifficulty = "AUTO";
         }

         if (!Set.of("AUTO", "TRASH", "MINIBOSS", "BOSS").contains(this.classLootRank)) {
            Logger.warn("Invalid classLootRank in " + this.filename + "; using AUTO.");
            this.classLootRank = "AUTO";
         }

         this.scale = this.processDouble("scale", this.scale, 1.0, false);
         this.silent = this.processBoolean("silent", this.silent, false, false);
         this.ai = this.processBoolean("ai", this.ai, true, false);
         this.alwaysShowName = this.processBoolean("alwaysShowName", this.alwaysShowName, false, false);

         try {
            this.patrolRoute = PatrolRoute.parse(this.fileConfiguration);
         } catch (IllegalArgumentException var4) {
            this.patrolRoute = null;
            Logger.warn("Invalid patrol in " + this.filename + ": " + var4.getMessage());
         }
      }
   }

   public boolean isCustomModelExists() {
      return CustomModel.customModelsEnabled() && CustomModel.modelExists(this.customModel) ? (this.customModelExists = true) : false;
   }

   private HashMap<Material, Double> processDamageModifiers(String path, HashMap<Material, Double> pluginDefaults) {
      HashMap<Material, Double> hashMap = new HashMap<>();
      if (!this.fileConfiguration.contains(path) && this.damageModifiers.isEmpty()) {
         return pluginDefaults;
      } else {
         for (String rawDamageModifier : this.fileConfiguration.getStringList(path)) {
            Material material = null;
            Double multiplier = null;
            if (rawDamageModifier.contains(",")) {
               String[] parsedStrings = rawDamageModifier.split(",");

               for (String parsedDamageModifier : parsedStrings) {
                  if (parsedDamageModifier.contains("material:")) {
                     try {
                        material = Material.getMaterial(parsedDamageModifier.replace("material:", ""));
                     } catch (Exception var14) {
                        Logger.warn("Boss " + this.getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                     }
                  } else if (parsedDamageModifier.contains("multiplier:")) {
                     try {
                        multiplier = Double.parseDouble(parsedDamageModifier.replace("multiplier:", ""));
                     } catch (Exception var15) {
                        Logger.warn("Boss " + this.getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                     }
                  } else {
                     Logger.warn("Entry " + parsedDamageModifier + " is invalid for boss file " + this.getFilename() + " !");
                  }
               }
            } else {
               String[] parsedStrings = rawDamageModifier.split(":");

               for (String parsedDamageModifierx : parsedStrings) {
                  if (parsedDamageModifierx.contains("material=")) {
                     try {
                        material = Material.getMaterial(parsedDamageModifierx.replace("material=", ""));
                     } catch (Exception var17) {
                        Logger.warn("Boss " + this.getFilename() + " has invalid entry " + parsedDamageModifierx + " !");
                     }
                  } else if (parsedDamageModifierx.contains("multiplier=")) {
                     try {
                        multiplier = Double.parseDouble(parsedDamageModifierx.replace("multiplier=", ""));
                     } catch (Exception var16) {
                        Logger.warn("Boss " + this.getFilename() + " has invalid entry " + parsedDamageModifierx + " !");
                     }
                  } else {
                     Logger.warn("Entry " + parsedDamageModifierx + " is invalid for boss file " + this.getFilename() + " !");
                  }
               }
            }

            if (material != null && multiplier != null) {
               hashMap.put(material, multiplier);
            }
         }

         if (!hashMap.isEmpty()) {
            this.fileConfiguration.addDefault(path, this.deserializeDamageModifiers(hashMap));
         }

         return hashMap;
      }
   }

   private List<String> deserializeDamageModifiers(HashMap<Material, Double> damageModifiers) {
      List<String> deserializedDamageModifiers = new ArrayList<>();
      damageModifiers.forEach((key, value) -> deserializedDamageModifiers.add("material=" + key.toString() + ":multiplier=" + value));
      return deserializedDamageModifiers;
   }

   protected List<String> majorBossDeathString(String slainLine) {
      return new ArrayList<>(
         List.of(
            "&e&l---------------------------------------------",
            "&4" + slainLine,
            "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
            "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
            "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
            "&aSlayers: $players",
            "&e&l---------------------------------------------"
         )
      );
   }

   public synchronized void saveFile() {
      this.discardQueuedRegionalSave();
      ConfigurationEngine.fileSaverSerialized(this.getWritableFileConfiguration().saveToString(), this.file);
   }

   public void setPatrolRoute(PatrolRoute route) {
      this.patrolRoute = route;
   }

   public boolean reloadPatrolRoute() {
      try {
         this.patrolRoute = PatrolRoute.parse(this.getWritableFileConfiguration());
         return this.patrolRoute != null;
      } catch (IllegalArgumentException var2) {
         this.patrolRoute = null;
         Logger.warn("Invalid patrol in " + this.filename + ": " + var2.getMessage());
         return false;
      }
   }

   @Generated
   public static Map<String, CustomBossesConfigFields> getRegionalElites() {
      return regionalElites;
   }

   @Generated
   public CustomLootTable getCustomLootTable() {
      return this.customLootTable;
   }

   @Generated
   public EntityType getEntityType() {
      return this.entityType;
   }

   @Generated
   public void setEntityType(EntityType entityType) {
      this.entityType = entityType;
   }

   @Generated
   public String getBehavior() {
      return this.behavior;
   }

   @Generated
   public void setBehavior(String behavior) {
      this.behavior = behavior;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setLevel(String level) {
      this.level = level;
   }

   @Generated
   public boolean isPersistent() {
      return this.isPersistent;
   }

   @Generated
   public void setPersistent(boolean isPersistent) {
      this.isPersistent = isPersistent;
   }

   @Generated
   public double getHealthMultiplier() {
      return this.healthMultiplier;
   }

   @Generated
   public void setHealthMultiplier(double healthMultiplier) {
      this.healthMultiplier = healthMultiplier;
   }

   @Generated
   public double getDamageMultiplier() {
      return this.damageMultiplier;
   }

   @Generated
   public void setDamageMultiplier(double damageMultiplier) {
      this.damageMultiplier = damageMultiplier;
   }

   @Generated
   public boolean isFrozen() {
      return this.frozen;
   }

   @Generated
   public void setFrozen(boolean frozen) {
      this.frozen = frozen;
   }

   @Generated
   public boolean isReinforcement() {
      return this.reinforcement;
   }

   @Generated
   public void setReinforcement(boolean reinforcement) {
      this.reinforcement = reinforcement;
   }

   @Generated
   public List<String> getOnDeathCommands() {
      return this.onDeathCommands;
   }

   @Generated
   public void setOnDeathCommands(List<String> onDeathCommands) {
      this.onDeathCommands = onDeathCommands;
   }

   @Generated
   public List<String> getOnSpawnCommands() {
      return this.onSpawnCommands;
   }

   @Generated
   public void setOnSpawnCommands(List<String> onSpawnCommands) {
      this.onSpawnCommands = onSpawnCommands;
   }

   @Generated
   public List<String> getOnCombatEnterCommands() {
      return this.onCombatEnterCommands;
   }

   @Generated
   public void setOnCombatEnterCommands(List<String> onCombatEnterCommands) {
      this.onCombatEnterCommands = onCombatEnterCommands;
   }

   @Generated
   public List<String> getOnCombatLeaveCommands() {
      return this.onCombatLeaveCommands;
   }

   @Generated
   public void setOnCombatLeaveCommands(List<String> onCombatLeaveCommands) {
      this.onCombatLeaveCommands = onCombatLeaveCommands;
   }

   @Generated
   public List<Object> getUniqueLootList() {
      return this.uniqueLootList;
   }

   @Generated
   public void setUniqueLootList(List<Object> uniqueLootList) {
      this.uniqueLootList = uniqueLootList;
   }

   @Generated
   public List<Object> getPowers() {
      return this.powers;
   }

   @Generated
   public void setPowers(List<Object> powers) {
      this.powers = powers;
   }

   @Generated
   public List<String> getOnDamageMessages() {
      return this.onDamageMessages;
   }

   @Generated
   public void setOnDamageMessages(List<String> onDamageMessages) {
      this.onDamageMessages = onDamageMessages;
   }

   @Generated
   public List<String> getOnDamagedMessages() {
      return this.onDamagedMessages;
   }

   @Generated
   public void setOnDamagedMessages(List<String> onDamagedMessages) {
      this.onDamagedMessages = onDamagedMessages;
   }

   @Generated
   public List<String> getTrails() {
      return this.trails;
   }

   @Generated
   public void setTrails(List<String> trails) {
      this.trails = trails;
   }

   @Generated
   public List<String> getPhases() {
      return this.phases;
   }

   @Generated
   public void setPhases(List<String> phases) {
      this.phases = phases;
   }

   @Generated
   public String getLocationMessage() {
      return this.locationMessage;
   }

   @Generated
   public void setLocationMessage(String locationMessage) {
      this.locationMessage = locationMessage;
   }

   @Generated
   public String getMountedEntity() {
      return this.mountedEntity;
   }

   @Generated
   public void setMountedEntity(String mountedEntity) {
      this.mountedEntity = mountedEntity;
   }

   @Generated
   public String getCustomModelMountPointID() {
      return this.customModelMountPointID;
   }

   @Generated
   public void setCustomModelMountPointID(String customModelMountPointID) {
      this.customModelMountPointID = customModelMountPointID;
   }

   @Generated
   public String getSpawnMessage() {
      return this.spawnMessage;
   }

   @Generated
   public void setSpawnMessage(String spawnMessage) {
      this.spawnMessage = spawnMessage;
   }

   @Generated
   public String getDeathMessage() {
      return this.deathMessage;
   }

   @Generated
   public void setDeathMessage(String deathMessage) {
      this.deathMessage = deathMessage;
   }

   @Generated
   public List<String> getDeathMessages() {
      return this.deathMessages;
   }

   @Generated
   public void setDeathMessages(List<String> deathMessages) {
      this.deathMessages = deathMessages;
   }

   @Generated
   public String getEscapeMessage() {
      return this.escapeMessage;
   }

   @Generated
   public void setEscapeMessage(String escapeMessage) {
      this.escapeMessage = escapeMessage;
   }

   @Generated
   public boolean isBaby() {
      return this.baby;
   }

   @Generated
   public void setBaby(boolean baby) {
      this.baby = baby;
   }

   @Generated
   public boolean isDropsEliteMobsLoot() {
      return this.dropsEliteMobsLoot;
   }

   @Generated
   public void setDropsEliteMobsLoot(boolean dropsEliteMobsLoot) {
      this.dropsEliteMobsLoot = dropsEliteMobsLoot;
   }

   @Generated
   public boolean isDropsVanillaLoot() {
      return this.dropsVanillaLoot;
   }

   @Generated
   public void setDropsVanillaLoot(boolean dropsVanillaLoot) {
      this.dropsVanillaLoot = dropsVanillaLoot;
   }

   @Generated
   public boolean isDropsRandomLoot() {
      return this.dropsRandomLoot;
   }

   @Generated
   public void setDropsRandomLoot(boolean dropsRandomLoot) {
      this.dropsRandomLoot = dropsRandomLoot;
   }

   @Generated
   public boolean isDropsSkillXP() {
      return this.dropsSkillXP;
   }

   @Generated
   public void setDropsSkillXP(boolean dropsSkillXP) {
      this.dropsSkillXP = dropsSkillXP;
   }

   @Generated
   public String getDisguise() {
      return this.disguise;
   }

   @Generated
   public void setDisguise(String disguise) {
      this.disguise = disguise;
   }

   @Generated
   public String getCustomDisguiseData() {
      return this.customDisguiseData;
   }

   @Generated
   public void setCustomDisguiseData(String customDisguiseData) {
      this.customDisguiseData = customDisguiseData;
   }

   @Generated
   public String getCustomModel() {
      return this.customModel;
   }

   @Generated
   public void setCustomModel(String customModel) {
      this.customModel = customModel;
   }

   @Generated
   public int getAnnouncementPriority() {
      return this.announcementPriority;
   }

   @Generated
   public void setAnnouncementPriority(int announcementPriority) {
      this.announcementPriority = announcementPriority;
   }

   @Generated
   public Integer getFollowDistance() {
      return this.followDistance;
   }

   @Generated
   public void setFollowDistance(Integer followDistance) {
      this.followDistance = followDistance;
   }

   @Generated
   public int getSpawnCooldown() {
      return this.spawnCooldown;
   }

   @Generated
   public void setSpawnCooldown(int spawnCooldown) {
      this.spawnCooldown = spawnCooldown;
   }

   @Generated
   public int getTimeout() {
      return this.timeout;
   }

   @Generated
   public void setTimeout(int timeout) {
      this.timeout = timeout;
   }

   @Generated
   public double getLeashRadius() {
      return this.leashRadius;
   }

   @Generated
   public void setLeashRadius(double leashRadius) {
      this.leashRadius = leashRadius;
   }

   @Generated
   public ItemStack getHelmet() {
      return this.helmet;
   }

   @Generated
   public void setHelmet(ItemStack helmet) {
      this.helmet = helmet;
   }

   @Generated
   public ItemStack getChestplate() {
      return this.chestplate;
   }

   @Generated
   public void setChestplate(ItemStack chestplate) {
      this.chestplate = chestplate;
   }

   @Generated
   public ItemStack getLeggings() {
      return this.leggings;
   }

   @Generated
   public void setLeggings(ItemStack leggings) {
      this.leggings = leggings;
   }

   @Generated
   public ItemStack getBoots() {
      return this.boots;
   }

   @Generated
   public void setBoots(ItemStack boots) {
      this.boots = boots;
   }

   @Generated
   public ItemStack getMainHand() {
      return this.mainHand;
   }

   @Generated
   public void setMainHand(ItemStack mainHand) {
      this.mainHand = mainHand;
   }

   @Generated
   public ItemStack getOffHand() {
      return this.offHand;
   }

   @Generated
   public void setOffHand(ItemStack offHand) {
      this.offHand = offHand;
   }

   @Generated
   public boolean isRegionalBoss() {
      return this.regionalBoss;
   }

   @Generated
   public void setRegionalBoss(boolean regionalBoss) {
      this.regionalBoss = regionalBoss;
   }

   @Generated
   public boolean isCullReinforcements() {
      return this.cullReinforcements;
   }

   @Generated
   public void setCullReinforcements(boolean cullReinforcements) {
      this.cullReinforcements = cullReinforcements;
   }

   @Generated
   public HashMap<Material, Double> getDamageModifiers() {
      return this.damageModifiers;
   }

   @Generated
   public void setDamageModifiers(HashMap<Material, Double> damageModifiers) {
      this.damageModifiers = damageModifiers;
   }

   @Generated
   public boolean isNormalizedCombat() {
      return this.normalizedCombat;
   }

   @Generated
   public void setNormalizedCombat(boolean normalizedCombat) {
      this.normalizedCombat = normalizedCombat;
   }

   @Generated
   public boolean isScaledCombat() {
      return this.scaledCombat;
   }

   @Generated
   public void setScaledCombat(boolean scaledCombat) {
      this.scaledCombat = scaledCombat;
   }

   @Generated
   public Double getMovementSpeedAttribute() {
      return this.movementSpeedAttribute;
   }

   @Generated
   public void setMovementSpeedAttribute(Double movementSpeedAttribute) {
      this.movementSpeedAttribute = movementSpeedAttribute;
   }

   @Generated
   public List<String> getOnSpawnBlockStates() {
      return this.onSpawnBlockStates;
   }

   @Generated
   public List<String> getOnRemoveBlockStates() {
      return this.onRemoveBlockStates;
   }

   @Generated
   public boolean isInstanced() {
      return this.instanced;
   }

   @Generated
   public void setInstanced(boolean instanced) {
      this.instanced = instanced;
   }

   @Generated
   public String getPhaseSpawnLocation() {
      return this.phaseSpawnLocation;
   }

   @Generated
   public void setPhaseSpawnLocation(String phaseSpawnLocation) {
      this.phaseSpawnLocation = phaseSpawnLocation;
   }

   @Generated
   public ConfigurationSection getRawEliteScripts() {
      return this.rawEliteScripts;
   }

   @Generated
   public void setRawEliteScripts(ConfigurationSection rawEliteScripts) {
      this.rawEliteScripts = rawEliteScripts;
   }

   @Generated
   public List<EliteScriptBlueprint> getEliteScript() {
      return this.eliteScript;
   }

   @Generated
   public String getSong() {
      return this.song;
   }

   @Generated
   public void setSong(String song) {
      this.song = song;
   }

   @Generated
   public boolean isAlert() {
      return this.alert;
   }

   @Generated
   public void setAlert(boolean alert) {
      this.alert = alert;
   }

   @Generated
   public boolean isRemoveAfterDeath() {
      return this.removeAfterDeath;
   }

   @Generated
   public void setRemoveAfterDeath(boolean removeAfterDeath) {
      this.removeAfterDeath = removeAfterDeath;
   }

   @Generated
   public int getSlimeSize() {
      return this.slimeSize;
   }

   @Generated
   public void setSlimeSize(int slimeSize) {
      this.slimeSize = slimeSize;
   }

   @Generated
   public boolean isNeutral() {
      return this.neutral;
   }

   @Generated
   public void setNeutral(boolean neutral) {
      this.neutral = neutral;
   }

   @Generated
   public String getOnKillMessage() {
      return this.onKillMessage;
   }

   @Generated
   public BossType getBossType() {
      return this.bossType;
   }

   @Generated
   public void setBossType(BossType bossType) {
      this.bossType = bossType;
   }

   @Generated
   public boolean isClassLoot() {
      return this.classLoot;
   }

   @Generated
   public void setClassLoot(boolean classLoot) {
      this.classLoot = classLoot;
   }

   @Generated
   public String getClassLootDifficulty() {
      return this.classLootDifficulty;
   }

   @Generated
   public String getClassLootRank() {
      return this.classLootRank;
   }

   @Generated
   public double getScale() {
      return this.scale;
   }

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   public void setSilent(boolean silent) {
      this.silent = silent;
   }

   @Generated
   public boolean isAi() {
      return this.ai;
   }

   @Generated
   public void setAi(boolean ai) {
      this.ai = ai;
   }

   @Generated
   public boolean isAlwaysShowName() {
      return this.alwaysShowName;
   }

   @Generated
   public void setAlwaysShowName(boolean alwaysShowName) {
      this.alwaysShowName = alwaysShowName;
   }

   @Generated
   public List<String> getSpawnLocations() {
      return this.spawnLocations;
   }

   @Generated
   public void setSpawnLocations(List<String> spawnLocations) {
      this.spawnLocations = spawnLocations;
   }

   @Generated
   public PatrolRoute getPatrolRoute() {
      return this.patrolRoute;
   }

   private record RegionalSave(long revision, String yaml) {
   }
}
