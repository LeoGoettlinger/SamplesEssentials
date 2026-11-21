package de.theredend2000.advancedhunt;

import de.theredend2000.advancedhunt.commands.AdvancedHuntCommand;
import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.data.SqlRepository;
import de.theredend2000.advancedhunt.data.YamlRepository;
import de.theredend2000.advancedhunt.lib.bstats.bukkit.Metrics;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.CloudBukkitCapabilities;
import de.theredend2000.advancedhunt.lib.cloud.execution.ExecutionCoordinator;
import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.MinecraftHelp;
import de.theredend2000.advancedhunt.lib.cloud.paper.LegacyPaperCommandManager;
import de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.BukkitAudiences;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.theredend2000.advancedhunt.listeners.ChatInputListener;
import de.theredend2000.advancedhunt.listeners.ItemsAdderIntegrationListener;
import de.theredend2000.advancedhunt.listeners.MenuListener;
import de.theredend2000.advancedhunt.listeners.PlaceModeListener;
import de.theredend2000.advancedhunt.listeners.PlayerInteractListener;
import de.theredend2000.advancedhunt.listeners.PlayerProtectionListener;
import de.theredend2000.advancedhunt.listeners.TreasureProtectListener;
import de.theredend2000.advancedhunt.managers.CollectionDeletionCleanupManager;
import de.theredend2000.advancedhunt.managers.CollectionManager;
import de.theredend2000.advancedhunt.managers.FireworkManager;
import de.theredend2000.advancedhunt.managers.HintManager;
import de.theredend2000.advancedhunt.managers.LeaderboardManager;
import de.theredend2000.advancedhunt.managers.MessageManager;
import de.theredend2000.advancedhunt.managers.MigrationService;
import de.theredend2000.advancedhunt.managers.ParticleManager;
import de.theredend2000.advancedhunt.managers.PlaceItemManager;
import de.theredend2000.advancedhunt.managers.PlaceModeManager;
import de.theredend2000.advancedhunt.managers.PlayerManager;
import de.theredend2000.advancedhunt.managers.ProximityManager;
import de.theredend2000.advancedhunt.managers.RewardManager;
import de.theredend2000.advancedhunt.managers.RewardPresetManager;
import de.theredend2000.advancedhunt.managers.ScanManager;
import de.theredend2000.advancedhunt.managers.SoundManager;
import de.theredend2000.advancedhunt.managers.TreasureInteractionHandler;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.managers.TreasureVisibilityManager;
import de.theredend2000.advancedhunt.managers.minigame.MinigameManager;
import de.theredend2000.advancedhunt.migration.legacy.LegacyDataMigrator;
import de.theredend2000.advancedhunt.migration.legacy.LegacyMigrationConfig;
import de.theredend2000.advancedhunt.placeholder.AdvancedHuntExpansion;
import de.theredend2000.advancedhunt.util.ConfigMigrationHandler;
import de.theredend2000.advancedhunt.util.ConfigUpdater;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.updater.PluginUpdater;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
   private int persistentDataStore;
   private LegacyPaperCommandManager<CommandSender> commandManager;
   private MinecraftHelp<CommandSender> minecraftHelp;
   private BukkitAudiences adventure;
   private DataRepository dataRepository;
   private TreasureManager treasureManager;
   private PlayerManager playerManager;
   private LeaderboardManager leaderboardManager;
   private CollectionManager collectionManager;
   private RewardManager rewardManager;
   private PlaceModeManager placeModeManager;
   private RewardPresetManager rewardPresetManager;
   private PlaceItemManager placeItemManager;
   private ParticleManager particleManager;
   private MigrationService migrationService;
   private MessageManager messageManager;
   private PluginUpdater pluginUpdater;
   private ChatInputListener chatInputListener;
   private MinigameManager minigameManager;
   private SoundManager soundManager;
   private ProximityManager proximityManager;
   private ScanManager scanManager;
   private HintManager hintManager;
   private FireworkManager fireworkManager;
   private TreasureVisibilityManager treasureVisibilityManager;
   private CollectionDeletionCleanupManager collectionDeletionCleanupManager;
   private Random random;
   private String currentStorageType;

   private static String normalizeStorageType(String rawType) {
      if (rawType == null) {
         return "YAML";
      } else {
         String normalized = rawType.trim().toUpperCase(Locale.ROOT);
         return !"MYSQL".equals(normalized) && !"SQLITE".equals(normalized) && !"YAML".equals(normalized) ? "YAML" : normalized;
      }
   }

   private Main.RepoSetup createRepositoryFromType(String normalizedType) {
      if (normalizedType.equals("MYSQL")) {
         return new Main.RepoSetup(
            new SqlRepository(
               this,
               this.getConfig().getString("storage.mysql.host"),
               this.getConfig().getInt("storage.mysql.port"),
               this.getConfig().getString("storage.mysql.database"),
               this.getConfig().getString("storage.mysql.username"),
               this.getConfig().getString("storage.mysql.password"),
               false
            ),
            "MYSQL"
         );
      } else {
         return normalizedType.equals("SQLITE")
            ? new Main.RepoSetup(new SqlRepository(this, null, 0, null, null, null, true), "SQLITE")
            : new Main.RepoSetup(new YamlRepository(this), "YAML");
      }
   }

   private Main.RepoSetup createRepositoryFromConfig() {
      String desiredType = normalizeStorageType(this.getConfig().getString("storage.type", "YAML"));
      return this.createRepositoryFromType(desiredType);
   }

   private void initRepository(DataRepository repository) {
      repository.init();
      if (repository instanceof YamlRepository) {
         ((YamlRepository)repository).startPeriodicFlush(60);
      }
   }

   private void rebuildRepositoryDependentManagers() {
      this.rewardPresetManager = new RewardPresetManager(this, this.dataRepository);
      this.placeItemManager = new PlaceItemManager(this, this.dataRepository);
      this.treasureManager = new TreasureManager(this.dataRepository);
      this.playerManager = new PlayerManager(this, this.dataRepository);
      this.leaderboardManager = new LeaderboardManager(this, this.dataRepository);
      this.collectionManager = new CollectionManager(this, this.dataRepository, this.treasureManager, this.playerManager, this.rewardManager);
      this.particleManager = new ParticleManager(this, this.treasureManager, this.playerManager, this.collectionManager);
      this.hintManager = new HintManager(this, this.treasureManager, this.collectionManager, this.playerManager, this.messageManager, this.particleManager);
      this.proximityManager = new ProximityManager(this, this.treasureManager, this.playerManager);
      this.placeModeManager = new PlaceModeManager(this);
      this.scanManager = new ScanManager(this, this.collectionManager, this.proximityManager, this.particleManager, this.placeModeManager);
      this.treasureVisibilityManager = new TreasureVisibilityManager(this, this.treasureManager, this.collectionManager);
      this.collectionDeletionCleanupManager = new CollectionDeletionCleanupManager(this);
      this.particleManager.start();
      this.scanManager.start();
   }

   public void onEnable() {
      this.persistentDataStore = this.setupPersistentDataStore();
      this.random = new Random();
      if (this.getConfig().getBoolean("updater.enabled", true)) {
         this.pluginUpdater = new PluginUpdater(this);
         this.pluginUpdater.trackPlugin(this.getDescription().getName(), this.getDescription().getVersion(), Collections.singletonMap("Modrinth", "8ZWWJnYu"));
         this.trackDependency("PacketEvents", "HYKaKraK");
         this.trackDependency("NBTAPI", "nfGCP9fk");
         this.trackDependency("PlaceholderAPI", "lKEzGugV");
         this.getServer().getScheduler().runTaskAsynchronously(this, () -> this.pluginUpdater.checkForUpdates());
      }

      if (!this.checkRequiredDependencies()) {
         Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
      } else {
         File configFile = new File(this.getDataFolder(), "config.yml");
         YamlConfiguration previousConfig = YamlConfiguration.loadConfiguration(configFile);
         int previousConfigVersion = previousConfig.getInt("config-version", 0);
         LegacyMigrationConfig legacyCfg = LegacyMigrationConfig.create(this.getDataFolder());
         legacyCfg = this.maybeAutoEnableLegacyMigration(previousConfigVersion, legacyCfg);
         if (legacyCfg.enabled()) {
            this.runLegacyMigrationThenStartup(configFile, previousConfig, legacyCfg);
         } else {
            this.normalStartup(configFile);
         }
      }
   }

   private void runLegacyMigrationThenStartup(File configFile, YamlConfiguration previousConfig, LegacyMigrationConfig legacyCfg) {
      this.getLogger().warning("Legacy migration detected. Running BEFORE config update to preserve backup...");
      this.adventure = BukkitAudiences.create(this);
      this.migrationService = new MigrationService(this.getLogger());
      String storageType = normalizeStorageType(previousConfig.getString("storage.type", "YAML"));
      Main.RepoSetup repoSetup = this.createRepositoryFromType(storageType);
      this.dataRepository = repoSetup.repository();
      this.currentStorageType = repoSetup.storageType();
      this.initRepository(this.dataRepository);
      LegacyDataMigrator migrator = new LegacyDataMigrator(this, this.dataRepository, legacyCfg);
      migrator.run()
         .whenComplete(
            (result, ex) -> {
               if (ex != null) {
                  this.getLogger().severe("Legacy migration failed: " + ex.getMessage());
                  if (legacyCfg.failFast()) {
                     Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
                     return;
                  }
               } else if (result != null) {
                  this.getLogger()
                     .info(
                        "Legacy migration complete: collections="
                           + result.collectionsImported()
                           + ", treasures="
                           + result.treasuresImported()
                           + ", players="
                           + result.playersImported()
                           + ", rewardPresets="
                           + result.rewardPresetsImported()
                           + ", placePresets="
                           + result.placePresetsImported()
                           + ", links="
                           + result.playerFoundLinks()
                           + ", missing-links="
                           + result.missingTreasureLinks()
                     );
               }

               Bukkit.getScheduler().runTask(this, () -> {
                  this.saveDefaultConfig();
                  ConfigUpdater.update(this, "config.yml", configFile, ConfigMigrationHandler::migrateConfig);
                  this.reloadConfig();
                  this.getConfig().set("storage.type", "YAML");
                  this.saveConfig();
                  this.getLogger().info("Storage type set to YAML after legacy migration.");
                  this.messageManager = new MessageManager(this);
                  this.soundManager = new SoundManager(this);
                  this.finishStartup();
               });
            }
         );
   }

   private void normalStartup(File configFile) {
      this.saveDefaultConfig();
      ConfigUpdater.update(this, "config.yml", configFile, ConfigMigrationHandler::migrateConfig);
      this.reloadConfig();
      this.adventure = BukkitAudiences.create(this);
      this.migrationService = new MigrationService(this.getLogger());
      this.messageManager = new MessageManager(this);
      this.soundManager = new SoundManager(this);
      Main.RepoSetup repoSetup = this.createRepositoryFromConfig();
      this.dataRepository = repoSetup.repository();
      this.currentStorageType = repoSetup.storageType();
      this.initRepository(this.dataRepository);
      this.finishStartup();
   }

   private LegacyMigrationConfig maybeAutoEnableLegacyMigration(int previousConfigVersion, LegacyMigrationConfig legacyCfg) {
      if (legacyCfg.enabled()) {
         return legacyCfg;
      } else if (previousConfigVersion <= 0 || previousConfigVersion >= 4) {
         return legacyCfg;
      } else if (looksLikeLegacyDataFolder(legacyCfg.sourceFolder())) {
         this.getLogger().warning("Auto-detected legacy data (upgrade from config-version " + previousConfigVersion + ")");
         this.getLogger().warning("Running legacy migration automatically.");
         return legacyCfg.withEnabled(true);
      } else {
         return legacyCfg;
      }
   }

   private static boolean looksLikeLegacyDataFolder(File folder) {
      if (folder != null && folder.exists() && folder.isDirectory()) {
         File eggs = new File(folder, "eggs");
         return eggs.exists() && eggs.isDirectory();
      } else {
         return false;
      }
   }

   private void finishStartup() {
      this.rewardManager = new RewardManager(this);
      this.placeModeManager = new PlaceModeManager(this);
      this.rebuildRepositoryDependentManagers();
      this.rewardPresetManager.reloadPresets();
      this.placeItemManager.reloadItems();
      this.treasureManager.loadTreasures();
      this.treasureVisibilityManager.start();
      Bukkit.getScheduler().runTaskLater(this, () -> this.particleManager.reapplyGlobalClaimVisibility(), 60L);
      if (this.collectionDeletionCleanupManager != null) {
         this.collectionDeletionCleanupManager.start();
      }

      this.minigameManager = new MinigameManager(this);
      this.fireworkManager = new FireworkManager(this);
      this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlaceModeListener(this), this);
      if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null && ItemsAdderAdapter.isEnabled()) {
         this.getServer().getPluginManager().registerEvents(new ItemsAdderIntegrationListener(this), this);
      }

      this.getServer().getPluginManager().registerEvents(new TreasureProtectListener(this), this);
      this.getServer().getPluginManager().registerEvents(new MenuListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerProtectionListener(this), this);
      this.chatInputListener = new ChatInputListener(this);
      this.getServer().getPluginManager().registerEvents(this.chatInputListener, this);
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         new AdvancedHuntExpansion(this).register();
      }

      this.setupCommands();
      new Metrics(this, 19495);
   }

   private void trackDependency(String name, String modrinthId) {
      if (this.getConfig().getBoolean("updater.dependencies." + name, false)) {
         Plugin dep = Bukkit.getPluginManager().getPlugin(name);
         String version = dep != null ? dep.getDescription().getVersion() : "0.0.0";
         this.pluginUpdater.trackPlugin(name, version, Collections.singletonMap("Modrinth", modrinthId));
      }
   }

   private boolean checkRequiredDependencies() {
      boolean allPresent = true;
      allPresent &= this.isRequiredDependencyPresent(
         "PacketEvents", "HYKaKraK", "Holograms and client-side particle/glow effects", "https://modrinth.com/plugin/packetevents", "packetevents"
      );
      return allPresent
         & this.isRequiredDependencyPresent("NBTAPI", "nfGCP9fk", "Custom NBT data on treasures and rewards", "https://modrinth.com/plugin/nbtapi");
   }

   private boolean isRequiredDependencyPresent(String pluginName, String modrinthId, String description, String modrinthUrl, String... aliases) {
      boolean present = Bukkit.getPluginManager().getPlugin(pluginName) != null;
      if (!present) {
         for (String alias : aliases) {
            if (Bukkit.getPluginManager().getPlugin(alias) != null) {
               present = true;
               break;
            }
         }
      }

      if (present) {
         return true;
      } else {
         String configKey = "updater.dependencies." + pluginName;
         boolean autoDownload = this.getConfig().getBoolean("updater.enabled", true) && this.getConfig().getBoolean(configKey, true);
         this.getLogger().severe("================================================================");
         this.getLogger().severe("[REQUIRED] " + pluginName + " is not installed!");
         this.getLogger().severe(description + " requires it to function.");
         if (autoDownload) {
            this.getLogger().severe("Auto-download is ENABLED - " + pluginName + " will be downloaded now.");
            this.getLogger().severe("Please RESTART the server once the download completes.");
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
               PluginUpdater tempUpdater = new PluginUpdater(this);
               tempUpdater.trackPlugin(pluginName, "0.0.0", Collections.singletonMap("Modrinth", modrinthId));
               tempUpdater.checkForUpdates();
            });
         } else {
            this.getLogger().severe("Download it from: " + modrinthUrl);
            this.getLogger().severe("Or enable auto-download in config.yml: " + configKey + ": true");
         }

         this.getLogger().severe("Disabling AdvancedHunt until " + pluginName + " is installed.");
         this.getLogger().severe("================================================================");
         return false;
      }
   }

   public PlaceItemManager getPlaceItemManager() {
      return this.placeItemManager;
   }

   public synchronized boolean reloadStorageBackend() {
      String desiredType = normalizeStorageType(this.getConfig().getString("storage.type", "YAML"));
      String existingType = normalizeStorageType(this.currentStorageType);
      if (desiredType.equals(existingType)) {
         if (this.dataRepository != null) {
            this.dataRepository.reload();
         }

         return false;
      } else {
         try {
            if (this.hintManager != null) {
               this.hintManager.stop();
            }
         } catch (Exception var4) {
         }

         if (this.scanManager != null) {
            this.scanManager.stop();
         }

         if (this.treasureVisibilityManager != null) {
            this.treasureVisibilityManager.stop();
         }

         if (this.collectionDeletionCleanupManager != null) {
            this.collectionDeletionCleanupManager.stop();
         }

         if (this.particleManager != null) {
            this.particleManager.stop();
         }

         if (this.leaderboardManager != null) {
            this.leaderboardManager.stop();
         }

         if (this.collectionManager != null) {
            this.collectionManager.stop();
         }

         if (this.playerManager != null) {
            this.playerManager.stop();
         }

         if (this.dataRepository != null) {
            this.dataRepository.shutdown();
         }

         Main.RepoSetup repoSetup = this.createRepositoryFromType(desiredType);
         this.dataRepository = repoSetup.repository();
         this.currentStorageType = repoSetup.storageType();
         this.initRepository(this.dataRepository);
         this.rebuildRepositoryDependentManagers();
         if (this.treasureVisibilityManager != null) {
            this.treasureVisibilityManager.start();
         }

         if (this.collectionDeletionCleanupManager != null) {
            this.collectionDeletionCleanupManager.start();
         }

         return true;
      }
   }

   public synchronized void applyMigratedStorageConfig(String targetType) {
      String normalizedType = normalizeStorageType(targetType);
      this.getConfig().set("storage.type", normalizedType);
      if (normalizedType.equals("MYSQL")) {
         this.getConfig().set("storage.mysql.host", this.getConfig().getString("migration.target.host", "localhost"));
         this.getConfig().set("storage.mysql.port", this.getConfig().getInt("migration.target.port", 3306));
         this.getConfig().set("storage.mysql.database", this.getConfig().getString("migration.target.database", "advancedhunt_target"));
         this.getConfig().set("storage.mysql.username", this.getConfig().getString("migration.target.username", "root"));
         this.getConfig().set("storage.mysql.password", this.getConfig().getString("migration.target.password", "password"));
      }

      this.saveConfig();
   }

   private void setupCommands() {
      ExecutionCoordinator<CommandSender> executionCoordinator = ExecutionCoordinator.simpleCoordinator();
      this.commandManager = LegacyPaperCommandManager.createNative(this, executionCoordinator);
      if (this.getConfig().getBoolean("command.brigadier-integration", true) && this.commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         this.commandManager.registerBrigadier();
      } else if (this.commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         this.commandManager.registerAsynchronousCompletions();
      }

      String[] parts = this.getConfig().getString("command.name", "advancedhunt").split("\\|");
      this.minecraftHelp = MinecraftHelp.<CommandSender>builder()
         .commandManager(this.commandManager)
         .audienceProvider(this.adventure::sender)
         .commandPrefix("/" + parts[0] + " help")
         .colors(MinecraftHelp.helpColors(NamedTextColor.GOLD, NamedTextColor.YELLOW, NamedTextColor.AQUA, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY))
         .messageProvider((sender, key, args) -> {
            String raw = this.getMessageManager().getMessage("command.help.minecraft." + key, false);
            return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
         })
         .build();
      new AdvancedHuntCommand(this).register(this.commandManager, this.isDebugMode());
   }

   public void onDisable() {
      TreasureInteractionHandler.reset();
      if (this.hintManager != null) {
         this.hintManager.stop();
      }

      if (this.leaderboardManager != null) {
         this.leaderboardManager.stop();
      }

      if (this.treasureVisibilityManager != null) {
         this.treasureVisibilityManager.stop();
      }

      if (this.collectionDeletionCleanupManager != null) {
         this.collectionDeletionCleanupManager.stop();
      }

      if (this.playerManager != null) {
         this.playerManager.stop();
      }

      if (this.scanManager != null) {
         this.scanManager.stop();
      }

      if (this.particleManager != null) {
         this.particleManager.stop();
      }

      if (this.dataRepository != null) {
         this.dataRepository.shutdown();
      }

      if (this.adventure != null) {
         this.adventure.close();
      }
   }

   public TreasureManager getTreasureManager() {
      return this.treasureManager;
   }

   public PlayerManager getPlayerManager() {
      return this.playerManager;
   }

   public LeaderboardManager getLeaderboardManager() {
      return this.leaderboardManager;
   }

   public CollectionManager getCollectionManager() {
      return this.collectionManager;
   }

   public RewardPresetManager getRewardPresetManager() {
      return this.rewardPresetManager;
   }

   public RewardManager getRewardManager() {
      return this.rewardManager;
   }

   public PlaceModeManager getPlaceModeManager() {
      return this.placeModeManager;
   }

   public ParticleManager getParticleManager() {
      return this.particleManager;
   }

   public MessageManager getMessageManager() {
      return this.messageManager;
   }

   public MigrationService getMigrationService() {
      return this.migrationService;
   }

   public DataRepository getDataRepository() {
      return this.dataRepository;
   }

   public ChatInputListener getChatInputListener() {
      return this.chatInputListener;
   }

   public MinigameManager getMinigameManager() {
      return this.minigameManager;
   }

   public SoundManager getSoundManager() {
      return this.soundManager;
   }

   public ProximityManager getProximityManager() {
      return this.proximityManager;
   }

   public ScanManager getScanManager() {
      return this.scanManager;
   }

   public HintManager getHintManager() {
      return this.hintManager;
   }

   public Random getRandom() {
      return this.random;
   }

   public FireworkManager getFireworkManager() {
      return this.fireworkManager;
   }

   public TreasureVisibilityManager getTreasureVisibilityManager() {
      return this.treasureVisibilityManager;
   }

   public CollectionDeletionCleanupManager getCollectionDeletionCleanupManager() {
      return this.collectionDeletionCleanupManager;
   }

   public MinecraftHelp<CommandSender> getMinecraftHelp() {
      return this.minecraftHelp;
   }

   public BukkitAudiences getAdventure() {
      return this.adventure;
   }

   public boolean isDebugMode() {
      return this.getConfig().getBoolean("dev-mode", false);
   }

   private static final class RepoSetup {
      private final DataRepository repository;
      private final String storageType;

      private RepoSetup(DataRepository repository, String storageType) {
         this.repository = repository;
         this.storageType = storageType;
      }

      private DataRepository repository() {
         return this.repository;
      }

      private String storageType() {
         return this.storageType;
      }
   }
}
