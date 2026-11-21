package com.magmaguy.elitemobs.config.contentpackages;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonObjective;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.util.Vector;

public class ContentPackagesConfigFields extends CustomConfigFields {
   private DifficultyResolver difficultyResolver;
   private String name;
   protected ContentPackagesConfigFields.ContentType contentType = null;
   private List<String> customInfo = null;
   private List<String> relativeBossLocations = null;
   private List<String> relativeTreasureChestLocations = null;
   private String downloadLink = "";
   private ContentPackagesConfigFields.DungeonSizeCategory dungeonSizeCategory = ContentPackagesConfigFields.DungeonSizeCategory.LAIR;
   private String worldName;
   private String wormholeWorldName;
   private String schematicName = null;
   private Environment environment = Environment.NORMAL;
   private boolean protect = true;
   private Location anchorPoint;
   private String defaultSchematicRotationString = null;
   private Integer calculatedRotation = 0;
   private Vector corner1 = new Vector(0, 0, 0);
   private Vector corner2 = new Vector(0, 0, 0);
   private int dungeonVersion = 0;
   private String playerInfo = null;
   private String regionEnterMessage = null;
   private String regionLeaveMessage = null;
   private List<String> worldGuardFlags = null;
   private boolean hasCustomModels = false;
   private boolean defaultDungeon = false;
   private Location teleportLocation = null;
   private String teleportLocationString = null;
   private String teleportLocationOffsetString = null;
   private Location teleportLocationOffset = null;
   private String permission = null;
   private int minPlayerCount = 1;
   private int maxPlayerCount = 5;
   private List<String> rawDungeonObjectives = null;
   private String startLocationString = null;
   private ContentPackagesConfigFields.InstanceDefinition instanceDefinition;
   private String dungeonConfigFolderName;
   private int contentLevel;
   private List<Map<String, Object>> difficulties;
   private boolean enchantmentChallenge = false;
   private boolean allowExplosions;
   private boolean listedInTeleports = true;
   private String song = null;
   private boolean allowLiquidFlow = true;
   private boolean allowElytra = false;
   private List<String> containedPackages;
   private boolean metaPackage = false;
   private List<String> customItemFilenames;
   private List<String> customEventFilenames;
   private List<String> modelNames;
   private List<String> setupMenuDescription = new ArrayList<>();
   private int dungeonLockoutMinutes = 4320;
   private String nightbreakSlug = null;

   public void setRawDungeonObjectives(List<String> objectives) {
      this.rawDungeonObjectives = objectives == null ? null : List.copyOf(objectives);
      this.instanceDefinition = null;
   }

   public synchronized void prepareInstanceDefinition() {
      if (this.instanceDefinition == null) {
         Location start = ConfigurationLocation.serialize(this.startLocationString, true);
         if (start != null
            && Double.isFinite(start.getX())
            && Double.isFinite(start.getY())
            && Double.isFinite(start.getZ())
            && Float.isFinite(start.getYaw())
            && Float.isFinite(start.getPitch())) {
            start.setWorld(null);
            List<DungeonObjective.TargetDefinition> objectives = new ArrayList<>();
            if (this.rawDungeonObjectives != null) {
               for (String raw : this.rawDungeonObjectives) {
                  try {
                     objectives.add(DungeonObjective.parse(raw));
                  } catch (IllegalArgumentException var6) {
                     throw new IllegalArgumentException(this.filename + ": " + var6.getMessage(), var6);
                  }
               }
            }

            this.instanceDefinition = new ContentPackagesConfigFields.InstanceDefinition(start, List.copyOf(objectives));
         } else {
            throw new IllegalArgumentException(this.filename + " requires finite startLocation coordinates");
         }
      }
   }

   public synchronized Location instanceStartLocation(World world) {
      this.prepareInstanceDefinition();
      Location start = this.instanceDefinition.start.clone();
      start.setWorld(world);
      return start;
   }

   public synchronized List<DungeonObjective.TargetDefinition> instanceObjectives() {
      this.prepareInstanceDefinition();
      return this.instanceDefinition.objectives;
   }

   public ContentPackagesConfigFields(String fileName, boolean isEnabled) {
      super(fileName, isEnabled);
   }

   public ContentPackagesConfigFields(
      String filename, boolean isEnabled, String name, List<String> customInfo, String downloadLink, List<String> containedPackages
   ) {
      super(filename, isEnabled);
      this.contentType = ContentPackagesConfigFields.ContentType.META_PACKAGE;
      this.name = name;
      this.customInfo = customInfo;
      this.downloadLink = downloadLink;
      this.containedPackages = containedPackages;
      this.defaultDungeon = true;
   }

   public ContentPackagesConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> customInfo,
      String downloadLink,
      ContentPackagesConfigFields.DungeonSizeCategory dungeonSizeCategory,
      String worldName,
      Environment environment,
      Boolean protect,
      String teleportLocationString,
      int dungeonVersion,
      String playerInfo,
      String regionEnterMessage,
      String regionLeaveMessage,
      String dungeonConfigFolderName,
      boolean allowExplosions
   ) {
      super(filename, isEnabled);
      this.contentType = ContentPackagesConfigFields.ContentType.OPEN_DUNGEON;
      this.name = name;
      this.customInfo = customInfo;
      this.downloadLink = downloadLink;
      this.dungeonSizeCategory = dungeonSizeCategory;
      this.worldName = worldName;
      this.environment = environment;
      this.protect = protect;
      this.teleportLocationString = teleportLocationString;
      this.dungeonVersion = dungeonVersion;
      this.playerInfo = playerInfo;
      this.regionEnterMessage = regionEnterMessage;
      this.regionLeaveMessage = regionLeaveMessage;
      this.dungeonConfigFolderName = dungeonConfigFolderName;
      this.allowExplosions = allowExplosions;
      this.defaultDungeon = true;
   }

   public ContentPackagesConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> customInfo,
      String downloadLink,
      ContentPackagesConfigFields.DungeonSizeCategory dungeonSizeCategory,
      String worldName,
      Environment environment,
      Boolean protect,
      String teleportLocationString,
      String startLocationString,
      int dungeonVersion,
      String playerInfo,
      String regionEnterMessage,
      String regionLeaveMessage,
      List<String> rawDungeonObjectives,
      String dungeonConfigFolderName,
      int contentLevel,
      boolean allowExplosions
   ) {
      super(filename, isEnabled);
      this.contentType = ContentPackagesConfigFields.ContentType.INSTANCED_DUNGEON;
      this.name = name;
      this.customInfo = customInfo;
      this.downloadLink = downloadLink;
      this.dungeonSizeCategory = dungeonSizeCategory;
      this.worldName = worldName;
      this.environment = environment;
      this.protect = protect;
      this.teleportLocationString = teleportLocationString;
      this.startLocationString = startLocationString;
      this.dungeonVersion = dungeonVersion;
      this.playerInfo = playerInfo;
      this.regionEnterMessage = regionEnterMessage;
      this.regionLeaveMessage = regionLeaveMessage;
      this.rawDungeonObjectives = rawDungeonObjectives;
      this.dungeonConfigFolderName = dungeonConfigFolderName;
      this.contentLevel = contentLevel;
      this.allowExplosions = allowExplosions;
      this.defaultDungeon = true;
   }

   public ContentPackagesConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> customInfo,
      String downloadLink,
      String worldName,
      Environment environment,
      Boolean protect,
      String teleportLocationString,
      int dungeonVersion,
      String playerInfo,
      String regionEnterMessage,
      String regionLeaveMessage,
      String dungeonConfigFolderName,
      boolean allowExplosions
   ) {
      super(filename, isEnabled);
      this.contentType = ContentPackagesConfigFields.ContentType.HUB;
      this.name = name;
      this.customInfo = customInfo;
      this.downloadLink = downloadLink;
      this.worldName = worldName;
      this.environment = environment;
      this.protect = protect;
      this.teleportLocationString = teleportLocationString;
      this.dungeonVersion = dungeonVersion;
      this.playerInfo = playerInfo;
      this.regionEnterMessage = regionEnterMessage;
      this.regionLeaveMessage = regionLeaveMessage;
      this.dungeonConfigFolderName = dungeonConfigFolderName;
      this.allowExplosions = allowExplosions;
      this.defaultDungeon = true;
   }

   @Override
   public void processConfigFields() {
      this.instanceDefinition = null;
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, false, true);
      this.name = this.translatable(this.filename, "name", this.processString("name", this.name, null, true));
      this.downloadLink = this.processString("downloadLink", this.downloadLink, this.downloadLink, false);
      this.customInfo = this.translatable(this.filename, "customInfo", this.processStringList("customInfo", this.customInfo, null, true));
      this.relativeBossLocations = this.processStringList("relativeBossLocations", this.relativeBossLocations, null, false);
      this.relativeTreasureChestLocations = this.processStringList("relativeTreasureChestLocations", this.relativeTreasureChestLocations, null, false);
      this.dungeonSizeCategory = this.processEnum(
         "dungeonSizeCategory", this.dungeonSizeCategory, null, ContentPackagesConfigFields.DungeonSizeCategory.class, false
      );
      if (this.dungeonSizeCategory == null) {
         Logger.warn("File " + this.filename + " does not have a valid dungeonSizeCategory!");
         this.fileConfiguration = null;
      } else {
         this.worldName = this.processString("worldName", this.worldName, null, false);
         this.wormholeWorldName = this.processString("wormholeWorldName", this.wormholeWorldName, null, false);
         this.schematicName = this.processString("schematicName", this.schematicName, null, false);
         this.environment = this.processEnum("environment", this.environment, null, Environment.class, false);
         this.protect = this.processBoolean("protect", this.protect, true, true);
         this.anchorPoint = this.processLocation("anchorPoint", this.anchorPoint, null, false);
         this.defaultSchematicRotationString = this.processString("defaultSchematicRotation", this.defaultSchematicRotationString, null, false);
         this.calculatedRotation = this.processInt("calculatedRotation", this.calculatedRotation, 0, false);
         this.corner1 = this.processVector("corner1", this.corner1, null, false);
         this.corner2 = this.processVector("corner2", this.corner2, null, false);
         this.dungeonVersion = this.processInt("dungeonVersion", this.dungeonVersion, 0, false);
         this.playerInfo = this.translatable(this.filename, "playerInfo", this.processString("playerInfo", this.playerInfo, null, false));
         this.regionEnterMessage = this.translatable(
            this.filename, "regionEnterMessage", this.processString("regionEnterMessage", this.regionEnterMessage, null, false)
         );
         this.regionLeaveMessage = this.translatable(
            this.filename, "regionLeaveMessage", this.processString("regionLeaveMessage", this.regionLeaveMessage, null, false)
         );
         this.hasCustomModels = this.processBoolean("hasCustomModels", this.hasCustomModels, false, false);
         this.startLocationString = this.processString("startLocation", this.startLocationString, null, false);
         this.teleportLocationString = this.processString("teleportLocation", this.teleportLocationString, null, false);
         this.teleportLocationOffsetString = this.processString("teleportLocationOffset", this.teleportLocationOffsetString, null, false);
         if (this.teleportLocationOffsetString != null && !this.teleportLocationOffsetString.isEmpty()) {
            this.teleportLocationOffset = ConfigurationLocation.serialize(this.teleportLocationOffsetString);
         }

         this.permission = this.processString("permission", this.permission, null, false);
         this.minPlayerCount = this.processInt("minPlayerCount", this.minPlayerCount, 1, false);
         this.maxPlayerCount = this.processInt("maxPlayerCount", this.maxPlayerCount, 5, false);
         this.rawDungeonObjectives = this.processStringList("dungeonObjectives", this.rawDungeonObjectives, null, false);
         this.contentType = this.processEnum("contentType", this.contentType, null, ContentPackagesConfigFields.ContentType.class, true);
         this.dungeonConfigFolderName = this.processString("dungeonConfigFolderName", this.dungeonConfigFolderName, null, false);
         this.contentLevel = this.processInt("contentLevel", this.contentLevel, 0, false);
         if (this.fileConfiguration.contains("difficulties")) {
            this.difficulties = this.fileConfiguration.getList("difficulties");
         } else {
            this.fileConfiguration.addDefault("difficulties", this.difficulties);
         }

         this.difficultyResolver = new DifficultyResolver(this.filename, this.difficulties);
         if (this.difficulties != null) {
            List<Map<String, Object>> mutableDifficulties = new ArrayList<>();

            for (int i = 0; i < this.difficulties.size(); i++) {
               Map<String, Object> difficulty = new HashMap<>(this.difficulties.get(i));
               if (difficulty.get("id") == null) {
                  difficulty.put("id", this.difficultyResolver.resolve(difficulty.get("name")));
               }

               if (difficulty.containsKey("name")) {
                  difficulty.put("name", TranslationsConfig.add(this.filename, "difficulties." + i + ".name", (String)difficulty.get("name")));
               }

               mutableDifficulties.add(difficulty);
            }

            this.difficulties = mutableDifficulties;
         }

         this.enchantmentChallenge = this.processBoolean("enchantmentChallenge", this.enchantmentChallenge, false, false);
         this.allowExplosions = this.processBoolean("allowExplosionBlockDamage", this.allowExplosions, false, false);
         this.listedInTeleports = this.processBoolean("listedInTeleports", this.listedInTeleports, true, false);
         this.song = this.processString("song", this.song, null, false);
         this.allowLiquidFlow = this.processBoolean("allowLiquidFlow", this.allowLiquidFlow, this.allowLiquidFlow, false);
         this.allowElytra = this.processBoolean("allowElytra", this.allowElytra, false, false);
         this.containedPackages = this.processStringList("containedPackages", this.containedPackages, null, false);
         this.customItemFilenames = this.processStringList("customItemFilenames", this.customItemFilenames, null, false);
         if (this.customItemFilenames != null) {
            this.contentType = ContentPackagesConfigFields.ContentType.ITEMS_PACKAGE;
         }

         this.customEventFilenames = this.processStringList("customEventFilenames", this.customEventFilenames, null, false);
         if (this.customEventFilenames != null) {
            this.contentType = ContentPackagesConfigFields.ContentType.EVENTS_PACKAGE;
         }

         this.modelNames = this.processStringList("modelNames", this.modelNames, null, false);
         if (this.modelNames != null) {
            this.contentType = ContentPackagesConfigFields.ContentType.MODELS_PACKAGE;
         }

         this.setupMenuDescription = this.processStringList("setupMenuDescription", this.setupMenuDescription, new ArrayList<>(), false);
         this.dungeonLockoutMinutes = this.processInt("dungeonLockoutMinutes", this.dungeonLockoutMinutes, 4320, true);
         this.processAdditionalFields();
         if (this.contentType == ContentPackagesConfigFields.ContentType.INSTANCED_DUNGEON
            || this.contentType == ContentPackagesConfigFields.ContentType.DYNAMIC_DUNGEON) {
            this.prepareInstanceDefinition();
         }
      }
   }

   public void processAdditionalFields() {
   }

   public void simpleInstall() {
      this.isEnabled = true;
      ConfigurationEngine.writeValue(true, this.file, this.fileConfiguration, "isEnabled");
   }

   public void installWorld() {
      this.isEnabled = true;
      ConfigurationEngine.writeValue(true, this.file, this.fileConfiguration, "isEnabled");
      this.teleportLocation = this.processLocation("teleportLocation", this.teleportLocation, null, false);
      ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
   }

   public void initializeWorld() {
      this.teleportLocation = this.processLocation("teleportLocation", this.teleportLocation, null, false);
   }

   public void simpleUninstall() {
      this.isEnabled = false;
      ConfigurationEngine.writeValue(false, this.file, this.fileConfiguration, "isEnabled");
   }

   public void uninstallWorld() {
      this.isEnabled = false;
      ConfigurationEngine.writeValue(false, this.file, this.fileConfiguration, "isEnabled");
      this.teleportLocation = null;
      ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
   }

   @Generated
   public DifficultyResolver getDifficultyResolver() {
      return this.difficultyResolver;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public ContentPackagesConfigFields.ContentType getContentType() {
      return this.contentType;
   }

   @Generated
   public void setContentType(ContentPackagesConfigFields.ContentType contentType) {
      this.contentType = contentType;
   }

   @Generated
   public List<String> getCustomInfo() {
      return this.customInfo;
   }

   @Generated
   public void setCustomInfo(List<String> customInfo) {
      this.customInfo = customInfo;
   }

   @Generated
   public List<String> getRelativeBossLocations() {
      return this.relativeBossLocations;
   }

   @Generated
   public List<String> getRelativeTreasureChestLocations() {
      return this.relativeTreasureChestLocations;
   }

   @Generated
   public String getDownloadLink() {
      return this.downloadLink;
   }

   @Generated
   public void setDownloadLink(String downloadLink) {
      this.downloadLink = downloadLink;
   }

   @Generated
   public ContentPackagesConfigFields.DungeonSizeCategory getDungeonSizeCategory() {
      return this.dungeonSizeCategory;
   }

   @Generated
   public String getWorldName() {
      return this.worldName;
   }

   @Generated
   public String getWormholeWorldName() {
      return this.wormholeWorldName;
   }

   @Generated
   public void setWormholeWorldName(String wormholeWorldName) {
      this.wormholeWorldName = wormholeWorldName;
   }

   @Generated
   public String getSchematicName() {
      return this.schematicName;
   }

   @Generated
   public Environment getEnvironment() {
      return this.environment;
   }

   @Generated
   public boolean isProtect() {
      return this.protect;
   }

   @Generated
   public Location getAnchorPoint() {
      return this.anchorPoint;
   }

   @Generated
   public String getDefaultSchematicRotationString() {
      return this.defaultSchematicRotationString;
   }

   @Generated
   public Integer getCalculatedRotation() {
      return this.calculatedRotation;
   }

   @Generated
   public void setCalculatedRotation(Integer calculatedRotation) {
      this.calculatedRotation = calculatedRotation;
   }

   @Generated
   public Vector getCorner1() {
      return this.corner1;
   }

   @Generated
   public Vector getCorner2() {
      return this.corner2;
   }

   @Generated
   public int getDungeonVersion() {
      return this.dungeonVersion;
   }

   @Generated
   public String getPlayerInfo() {
      return this.playerInfo;
   }

   @Generated
   public String getRegionEnterMessage() {
      return this.regionEnterMessage;
   }

   @Generated
   public String getRegionLeaveMessage() {
      return this.regionLeaveMessage;
   }

   @Generated
   public List<String> getWorldGuardFlags() {
      return this.worldGuardFlags;
   }

   @Generated
   public void setWorldGuardFlags(List<String> worldGuardFlags) {
      this.worldGuardFlags = worldGuardFlags;
   }

   @Generated
   public boolean isHasCustomModels() {
      return this.hasCustomModels;
   }

   @Generated
   public void setHasCustomModels(boolean hasCustomModels) {
      this.hasCustomModels = hasCustomModels;
   }

   @Generated
   public boolean isDefaultDungeon() {
      return this.defaultDungeon;
   }

   @Generated
   public void setDefaultDungeon(boolean defaultDungeon) {
      this.defaultDungeon = defaultDungeon;
   }

   @Generated
   public Location getTeleportLocation() {
      return this.teleportLocation;
   }

   @Generated
   public String getTeleportLocationString() {
      return this.teleportLocationString;
   }

   @Generated
   public String getTeleportLocationOffsetString() {
      return this.teleportLocationOffsetString;
   }

   @Generated
   public Location getTeleportLocationOffset() {
      return this.teleportLocationOffset;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public int getMinPlayerCount() {
      return this.minPlayerCount;
   }

   @Generated
   public void setMinPlayerCount(int minPlayerCount) {
      this.minPlayerCount = minPlayerCount;
   }

   @Generated
   public int getMaxPlayerCount() {
      return this.maxPlayerCount;
   }

   @Generated
   public void setMaxPlayerCount(int maxPlayerCount) {
      this.maxPlayerCount = maxPlayerCount;
   }

   @Generated
   public List<String> getRawDungeonObjectives() {
      return this.rawDungeonObjectives;
   }

   @Generated
   public String getStartLocationString() {
      return this.startLocationString;
   }

   @Generated
   public String getDungeonConfigFolderName() {
      return this.dungeonConfigFolderName;
   }

   @Generated
   public int getContentLevel() {
      return this.contentLevel;
   }

   @Generated
   public void setContentLevel(int contentLevel) {
      this.contentLevel = contentLevel;
   }

   @Generated
   public List<Map<String, Object>> getDifficulties() {
      return this.difficulties;
   }

   @Generated
   public void setDifficulties(List<Map<String, Object>> difficulties) {
      this.difficulties = difficulties;
   }

   @Generated
   public boolean isEnchantmentChallenge() {
      return this.enchantmentChallenge;
   }

   @Generated
   public void setEnchantmentChallenge(boolean enchantmentChallenge) {
      this.enchantmentChallenge = enchantmentChallenge;
   }

   @Generated
   public boolean isAllowExplosions() {
      return this.allowExplosions;
   }

   @Generated
   public void setAllowExplosions(boolean allowExplosions) {
      this.allowExplosions = allowExplosions;
   }

   @Generated
   public boolean isListedInTeleports() {
      return this.listedInTeleports;
   }

   @Generated
   public void setListedInTeleports(boolean listedInTeleports) {
      this.listedInTeleports = listedInTeleports;
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
   public boolean isAllowLiquidFlow() {
      return this.allowLiquidFlow;
   }

   @Generated
   public void setAllowLiquidFlow(boolean allowLiquidFlow) {
      this.allowLiquidFlow = allowLiquidFlow;
   }

   @Generated
   public boolean isAllowElytra() {
      return this.allowElytra;
   }

   @Generated
   public void setAllowElytra(boolean allowElytra) {
      this.allowElytra = allowElytra;
   }

   @Generated
   public List<String> getContainedPackages() {
      return this.containedPackages;
   }

   @Generated
   public void setContainedPackages(List<String> containedPackages) {
      this.containedPackages = containedPackages;
   }

   @Generated
   public boolean isMetaPackage() {
      return this.metaPackage;
   }

   @Generated
   public void setMetaPackage(boolean metaPackage) {
      this.metaPackage = metaPackage;
   }

   @Generated
   public List<String> getCustomItemFilenames() {
      return this.customItemFilenames;
   }

   @Generated
   public void setCustomItemFilenames(List<String> customItemFilenames) {
      this.customItemFilenames = customItemFilenames;
   }

   @Generated
   public List<String> getCustomEventFilenames() {
      return this.customEventFilenames;
   }

   @Generated
   public void setCustomEventFilenames(List<String> customEventFilenames) {
      this.customEventFilenames = customEventFilenames;
   }

   @Generated
   public List<String> getModelNames() {
      return this.modelNames;
   }

   @Generated
   public void setModelNames(List<String> modelNames) {
      this.modelNames = modelNames;
   }

   @Generated
   public List<String> getSetupMenuDescription() {
      return this.setupMenuDescription;
   }

   @Generated
   public void setSetupMenuDescription(List<String> setupMenuDescription) {
      this.setupMenuDescription = setupMenuDescription;
   }

   @Generated
   public int getDungeonLockoutMinutes() {
      return this.dungeonLockoutMinutes;
   }

   @Generated
   public void setDungeonLockoutMinutes(int dungeonLockoutMinutes) {
      this.dungeonLockoutMinutes = dungeonLockoutMinutes;
   }

   @Generated
   public String getNightbreakSlug() {
      return this.nightbreakSlug;
   }

   @Generated
   public void setNightbreakSlug(String nightbreakSlug) {
      this.nightbreakSlug = nightbreakSlug;
   }

   public static enum ContentType {
      OPEN_DUNGEON,
      INSTANCED_DUNGEON,
      DYNAMIC_DUNGEON,
      HUB,
      SCHEMATIC_DUNGEON,
      META_PACKAGE,
      ITEMS_PACKAGE,
      EVENTS_PACKAGE,
      MODELS_PACKAGE;
   }

   public static enum DungeonLocationType {
      WORLD,
      SCHEMATIC,
      INSTANCED;
   }

   public static enum DungeonSizeCategory {
      LAIR,
      SANCTUM,
      MINIDUNGEON,
      DUNGEON,
      RAID,
      ADVENTURE,
      OTHER,
      ARENA,
      REALM;
   }

   private record InstanceDefinition(Location start, List<DungeonObjective.TargetDefinition> objectives) {
   }
}
