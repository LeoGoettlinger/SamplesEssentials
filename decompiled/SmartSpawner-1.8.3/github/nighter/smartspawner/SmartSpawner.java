package github.nighter.smartspawner;

import github.nighter.bukkit.Metrics;
import github.nighter.charts.AdvancedPie;
import github.nighter.charts.SimplePie;
import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerAPIImpl;
import github.nighter.smartspawner.api.SmartSpawnerPlugin;
import github.nighter.smartspawner.api.gui.ExternalGuiLayoutLoader;
import github.nighter.smartspawner.api.gui.GuiLayoutRegistryImpl;
import github.nighter.smartspawner.commands.BrigadierCommandManager;
import github.nighter.smartspawner.commands.editloot.LootEditorDialogs;
import github.nighter.smartspawner.commands.editloot.LootEditorHandler;
import github.nighter.smartspawner.commands.editloot.LootEditorService;
import github.nighter.smartspawner.commands.editloot.LootEditorUI;
import github.nighter.smartspawner.commands.list.ListSubCommand;
import github.nighter.smartspawner.commands.list.gui.list.SpawnerListGUI;
import github.nighter.smartspawner.commands.list.gui.list.UserPreferenceCache;
import github.nighter.smartspawner.commands.list.gui.management.SpawnerManagementHandler;
import github.nighter.smartspawner.commands.list.gui.serverselection.ServerSelectionHandler;
import github.nighter.smartspawner.commands.near.NearResultGUI;
import github.nighter.smartspawner.commands.near.SpawnerHighlightManager;
import github.nighter.smartspawner.commands.prices.PricesGUI;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.extras.HopperConfig;
import github.nighter.smartspawner.extras.HopperService;
import github.nighter.smartspawner.hooks.IntegrationManager;
import github.nighter.smartspawner.hooks.economy.ItemPriceManager;
import github.nighter.smartspawner.hooks.economy.SellIntegrationConfigUpdater;
import github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus.SpawnerProvider;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.logging.ActivityLogConfigUpdater;
import github.nighter.smartspawner.logging.LoggingConfig;
import github.nighter.smartspawner.logging.SpawnerActionLogger;
import github.nighter.smartspawner.logging.SpawnerAuditListener;
import github.nighter.smartspawner.migration.SpawnerDataMigration;
import github.nighter.smartspawner.spawner.config.ItemSpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.config.SpawnerNames;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.data.WorldEventHandler;
import github.nighter.smartspawner.spawner.data.database.DatabaseManager;
import github.nighter.smartspawner.spawner.data.database.SpawnerDatabaseHandler;
import github.nighter.smartspawner.spawner.data.database.SqliteToMySqlMigration;
import github.nighter.smartspawner.spawner.data.database.YamlToDatabaseMigration;
import github.nighter.smartspawner.spawner.data.storage.SpawnerStorage;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.gui.layout.GuiButtonInteractionService;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayoutConfig;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuAction;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuUI;
import github.nighter.smartspawner.spawner.gui.sell.SpawnerSellConfirmListener;
import github.nighter.smartspawner.spawner.gui.sell.SpawnerSellConfirmUI;
import github.nighter.smartspawner.spawner.gui.storage.SpawnerStorageAction;
import github.nighter.smartspawner.spawner.gui.storage.SpawnerStorageUI;
import github.nighter.smartspawner.spawner.gui.storage.filter.FilterConfigUI;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.interactions.click.SpawnerClickManager;
import github.nighter.smartspawner.spawner.interactions.destroy.SpawnerBreakListener;
import github.nighter.smartspawner.spawner.interactions.destroy.SpawnerExplosionListener;
import github.nighter.smartspawner.spawner.interactions.destroy.SpawnerRemovalService;
import github.nighter.smartspawner.spawner.interactions.place.SpawnerPlaceListener;
import github.nighter.smartspawner.spawner.interactions.stack.SpawnerStackHandler;
import github.nighter.smartspawner.spawner.interactions.type.SpawnEggHandler;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import github.nighter.smartspawner.spawner.lootgen.SpawnerLootGenerator;
import github.nighter.smartspawner.spawner.lootgen.SpawnerRangeChecker;
import github.nighter.smartspawner.spawner.natural.NaturalSpawnerListener;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.sell.SpawnerSellManager;
import github.nighter.smartspawner.spawner.utils.SpawnerTypeChecker;
import github.nighter.smartspawner.updates.ConfigUpdater;
import github.nighter.smartspawner.updates.LanguageUpdater;
import github.nighter.smartspawner.updates.UpdateChecker;
import github.nighter.smartspawner.utils.TimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SmartSpawner extends JavaPlugin implements SmartSpawnerPlugin {
   private static SmartSpawner instance;
   public final int DATA_VERSION = 3;
   private IntegrationManager integrationManager;
   private TimeFormatter timeFormatter;
   private ConfigUpdater configUpdater;
   private LanguageManager languageManager;
   private LanguageUpdater languageUpdater;
   private MessageService messageService;
   private SpawnerSettingsConfig spawnerSettingsConfig;
   private ItemSpawnerSettingsConfig itemSpawnerSettingsConfig;
   private SpawnerItemFactory spawnerItemFactory;
   private ExternalGuiLayoutLoader guiLayoutLoader;
   private GuiLayoutRegistryImpl guiLayoutRegistry;
   private GuiLayoutConfig guiLayoutConfig;
   private GuiButtonInteractionService guiButtonInteractionService;
   private SpawnerMenuUI spawnerMenuUI;
   private SpawnerStorageUI spawnerStorageUI;
   private FilterConfigUI filterConfigUI;
   private SpawnerSellConfirmUI spawnerSellConfirmUI;
   private SpawnEggHandler spawnEggHandler;
   private SpawnerClickManager spawnerClickManager;
   private SpawnerStackHandler spawnerStackHandler;
   private SpawnerMenuAction spawnerMenuAction;
   private SpawnerStorageAction spawnerStorageAction;
   private SpawnerSellManager spawnerSellManager;
   private SpawnerSellConfirmListener spawnerSellConfirmListener;
   private SpawnerStorage spawnerStorage;
   private DatabaseManager databaseManager;
   private SpawnerManager spawnerManager;
   private HopperService hopperService;
   private HopperConfig hopperConfig;
   private SpawnerRemovalService spawnerRemovalService;
   private NaturalSpawnerListener naturalSpawnerListener;
   private SpawnerLootGenerator spawnerLootGenerator;
   private SpawnerRangeChecker rangeChecker;
   private SpawnerGuiViewManager spawnerGuiViewManager;
   private SpawnerExplosionListener spawnerExplosionListener;
   private SpawnerBreakListener spawnerBreakListener;
   private SpawnerPlaceListener spawnerPlaceListener;
   private WorldEventHandler worldEventHandler;
   private ItemPriceManager itemPriceManager;
   private UpdateChecker updateChecker;
   private BrigadierCommandManager brigadierCommandManager;
   private ListSubCommand listSubCommand;
   private UserPreferenceCache userPreferenceCache;
   private SpawnerListGUI spawnerListGUI;
   private SpawnerManagementHandler spawnerManagementHandler;
   private ServerSelectionHandler serverSelectionHandler;
   private PricesGUI pricesGUI;
   private LootEditorService lootEditorService;
   private LootEditorUI lootEditorUI;
   private LootEditorDialogs lootEditorDialogs;
   private LootEditorHandler lootEditorHandler;
   private SpawnerActionLogger spawnerActionLogger;
   private SpawnerAuditListener spawnerAuditListener;
   private LoggingConfig loggingConfig;
   private SpawnerHighlightManager spawnerHighlightManager;
   private NearResultGUI nearResultGUI;
   private SmartSpawnerAPIImpl apiImpl;

   public void onEnable() {
      long startTime = System.currentTimeMillis();
      instance = this;
      Config.load(this);
      this.integrationManager = new IntegrationManager(this);
      this.integrationManager.initializeIntegrations();
      this.migrateDataIfNeeded();
      if (!this.initializeComponents()) {
         this.getLogger()
            .severe("SmartSpawner could not open its spawner storage and will not enable. Fix the database settings in config.yml, then restart the server.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.setupCommand();
         this.setupBtatsMetrics();
         this.registerListeners();
         if (this.worldEventHandler != null) {
            this.worldEventHandler.attemptInitialSpawnerLoad();
         }

         long loadTime = System.currentTimeMillis() - startTime;
         this.getLogger().info("SmartSpawner has been enabled! (Loaded in " + loadTime + "ms)");
      }
   }

   @Override
   public SmartSpawnerAPI getAPI() {
      return this.apiImpl;
   }

   private void migrateDataIfNeeded() {
      SpawnerDataMigration migration = new SpawnerDataMigration(this);
      if (migration.checkAndMigrateData()) {
         this.getLogger().info("Data migration completed. Loading with new format...");
      }
   }

   private boolean initializeComponents() {
      this.initializeServices();
      this.initializeEconomyComponents();
      if (!this.initializeCoreComponents()) {
         return false;
      } else {
         this.initializeHandlers();
         this.initializeUIAndActions();
         this.setUpHopperHandler();
         this.initializeListeners();
         this.apiImpl = new SmartSpawnerAPIImpl(this);
         this.updateChecker = new UpdateChecker(this);
         return true;
      }
   }

   private void initializeServices() {
      SpawnerTypeChecker.init(this);
      this.timeFormatter = new TimeFormatter(this);
      this.configUpdater = new ConfigUpdater(this);
      this.configUpdater.checkAndUpdateConfig();
      this.languageManager = new LanguageManager(this);
      this.languageUpdater = new LanguageUpdater(this);
      this.messageService = new MessageService(this, this.languageManager);
      this.spawnerSettingsConfig = new SpawnerSettingsConfig(this);
      this.itemSpawnerSettingsConfig = new ItemSpawnerSettingsConfig(this);
      new SellIntegrationConfigUpdater(this).checkAndUpdate();
      new ActivityLogConfigUpdater(this).checkAndUpdate();
      this.loggingConfig = new LoggingConfig(this);
      this.spawnerActionLogger = new SpawnerActionLogger(this, this.loggingConfig);
      this.spawnerAuditListener = new SpawnerAuditListener(this.spawnerActionLogger);
   }

   private void initializeEconomyComponents() {
      this.itemPriceManager = new ItemPriceManager(this);
      this.itemPriceManager.init();
      if (this.spawnerSettingsConfig != null) {
         this.spawnerSettingsConfig.load();
      }

      if (this.itemSpawnerSettingsConfig != null) {
         this.itemSpawnerSettingsConfig.load();
      }

      SpawnerNames.warnConflicts(this);
      SpawnerMobHeadTexture.prewarmCache();
      this.spawnerItemFactory = new SpawnerItemFactory(this);
   }

   private boolean initializeCoreComponents() {
      if (!this.initializeStorage()) {
         return false;
      } else {
         this.spawnerManager = new SpawnerManager(this);
         this.spawnerRemovalService = new SpawnerRemovalService(this);
         this.spawnerManager.reloadAllHolograms();
         this.guiLayoutLoader = new ExternalGuiLayoutLoader(this);
         this.guiLayoutRegistry = new GuiLayoutRegistryImpl(this.guiLayoutLoader, this.getLogger());
         this.guiLayoutConfig = new GuiLayoutConfig(this, this.guiLayoutLoader, this.guiLayoutRegistry);
         this.guiButtonInteractionService = new GuiButtonInteractionService(this);
         this.spawnerStorageUI = new SpawnerStorageUI(this);
         this.filterConfigUI = new FilterConfigUI(this);
         this.spawnerMenuUI = new SpawnerMenuUI(this);
         this.spawnerSellConfirmUI = new SpawnerSellConfirmUI(this);
         this.spawnerGuiViewManager = new SpawnerGuiViewManager(this);
         this.spawnerLootGenerator = new SpawnerLootGenerator(this);
         this.spawnerSellManager = new SpawnerSellManager(this);
         this.rangeChecker = new SpawnerRangeChecker(this);
         return true;
      }
   }

   private boolean initializeStorage() {
      String modeStr = this.getConfig().getString("database.type", "SQLITE");
      StorageMode mode = StorageMode.fromConfig(modeStr);
      if (!mode.name().equalsIgnoreCase(modeStr == null ? "" : modeStr.trim())) {
         this.getLogger().warning("Storage mode '" + modeStr + "' is not available, using " + mode + " instead.");
      }

      String dbType = mode == StorageMode.MYSQL ? "MySQL/MariaDB" : "SQLite";
      this.getLogger().info("Initializing " + dbType + " database storage mode...");
      this.databaseManager = new DatabaseManager(this, mode);
      if (!this.databaseManager.initialize()) {
         this.getLogger().severe("Failed to initialize the " + dbType + " connection. Spawner data cannot be loaded or saved.");
         this.databaseManager.shutdown();
         this.databaseManager = null;
         return false;
      } else {
         SpawnerDatabaseHandler dbHandler = new SpawnerDatabaseHandler(this, this.databaseManager);
         if (!dbHandler.initialize()) {
            this.getLogger().severe("Failed to initialize the " + dbType + " storage handler. Spawner data cannot be loaded or saved.");
            this.databaseManager.shutdown();
            this.databaseManager = null;
            return false;
         } else {
            this.spawnerStorage = dbHandler;
            boolean migrateFromLocal = this.getConfig().getBoolean("database.migrate-from-local", true);
            if (migrateFromLocal) {
               YamlToDatabaseMigration yamlMigration = new YamlToDatabaseMigration(this, this.databaseManager);
               if (yamlMigration.needsMigration()) {
                  this.getLogger().info("YAML data detected, starting migration to " + dbType + "...");
                  if (yamlMigration.migrate()) {
                     this.getLogger().info("YAML migration completed successfully!");
                  } else {
                     this.getLogger().warning("YAML migration completed with some errors. Check logs for details.");
                  }
               }

               if (mode == StorageMode.MYSQL) {
                  SqliteToMySqlMigration sqliteMigration = new SqliteToMySqlMigration(this, this.databaseManager);
                  if (sqliteMigration.needsMigration()) {
                     this.getLogger().info("SQLite data detected, starting migration to MySQL...");
                     if (sqliteMigration.migrate()) {
                        this.getLogger().info("SQLite to MySQL migration completed successfully!");
                     } else {
                        this.getLogger().warning("SQLite migration completed with some errors. Check logs for details.");
                     }
                  }
               }
            }

            this.getLogger().info(dbType + " database storage initialized successfully.");
            return true;
         }
      }
   }

   private void initializeHandlers() {
      this.spawnEggHandler = new SpawnEggHandler(this);
      this.spawnerStackHandler = new SpawnerStackHandler(this);
      this.spawnerClickManager = new SpawnerClickManager(this);
      this.spawnerHighlightManager = new SpawnerHighlightManager(this);
      this.nearResultGUI = new NearResultGUI(this, this.spawnerHighlightManager);
   }

   private void initializeUIAndActions() {
      this.spawnerMenuAction = new SpawnerMenuAction(this);
      this.spawnerStorageAction = new SpawnerStorageAction(this);
      this.spawnerSellConfirmListener = new SpawnerSellConfirmListener(this);
   }

   private void initializeListeners() {
      this.naturalSpawnerListener = new NaturalSpawnerListener(this);
      this.spawnerExplosionListener = new SpawnerExplosionListener(this);
      this.spawnerBreakListener = new SpawnerBreakListener(this);
      this.spawnerPlaceListener = new SpawnerPlaceListener(this);
      this.worldEventHandler = new WorldEventHandler(this);
   }

   public void setUpHopperHandler() {
      this.hopperConfig = new HopperConfig(this);
      if (this.hopperService != null) {
         this.hopperService.cleanup();
      }

      if (this.hopperConfig.isHopperEnabled()) {
         this.hopperService = new HopperService(this);
      }
   }

   private void registerListeners() {
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(this.naturalSpawnerListener, this);
      pm.registerEvents(this.spawnerBreakListener, this);
      pm.registerEvents(this.spawnerPlaceListener, this);
      pm.registerEvents(this.spawnerStorageAction, this);
      pm.registerEvents(this.spawnerExplosionListener, this);
      pm.registerEvents(this.spawnerClickManager, this);
      pm.registerEvents(this.spawnerMenuAction, this);
      pm.registerEvents(this.worldEventHandler, this);
      pm.registerEvents(this.spawnerListGUI, this);
      pm.registerEvents(this.spawnerManagementHandler, this);
      pm.registerEvents(this.serverSelectionHandler, this);
      pm.registerEvents(this.pricesGUI, this);
      pm.registerEvents(this.lootEditorHandler, this);
      pm.registerEvents(this.spawnerSellConfirmListener, this);
      pm.registerEvents(this.guiButtonInteractionService, this);
      if (this.spawnerHighlightManager != null) {
         pm.registerEvents(this.spawnerHighlightManager, this);
      }

      if (this.nearResultGUI != null) {
         pm.registerEvents(this.nearResultGUI, this);
      }

      if (this.spawnerAuditListener != null) {
         pm.registerEvents(this.spawnerAuditListener, this);
      }
   }

   private void setupCommand() {
      this.lootEditorService = new LootEditorService(this);
      this.lootEditorUI = new LootEditorUI(this, this.lootEditorService);
      this.lootEditorDialogs = new LootEditorDialogs(this, this.lootEditorService, this.lootEditorUI);
      this.lootEditorHandler = new LootEditorHandler(this, this.lootEditorService, this.lootEditorUI, this.lootEditorDialogs);
      this.brigadierCommandManager = new BrigadierCommandManager(this);
      this.brigadierCommandManager.registerCommands();
      this.userPreferenceCache = new UserPreferenceCache(this);
      this.listSubCommand = new ListSubCommand(this);
      this.spawnerListGUI = new SpawnerListGUI(this);
      this.spawnerManagementHandler = new SpawnerManagementHandler(this, this.listSubCommand);
      this.serverSelectionHandler = new ServerSelectionHandler(this, this.listSubCommand);
      this.pricesGUI = new PricesGUI(this);
   }

   private void setupBtatsMetrics() {
      Metrics metrics = new Metrics(this, 24822);
      metrics.addCustomChart(new SimplePie("holograms", () -> String.valueOf(this.getConfig().getBoolean("hologram.enabled", false))));
      metrics.addCustomChart(new SimplePie("hoppers", () -> String.valueOf(this.getConfig().getBoolean("hopper.enabled", false))));
      metrics.addCustomChart(new SimplePie("spawner_blocks", () -> bucketSpawnerCount(this.spawnerManager.getTotalSpawners())));
      metrics.addCustomChart(new SimplePie("stacked_spawners", () -> {
         long totalStacked = this.spawnerManager.getAllSpawners().stream().mapToLong(SpawnerData::getStackSize).sum();
         return bucketSpawnerCount(totalStacked);
      }));
      metrics.addCustomChart(new SimplePie("storage_mode", () -> this.getConfig().getString("database.type", "SQLITE")));
      metrics.addCustomChart(new SimplePie("language", () -> this.getConfig().getString("language", "en_US")));
      metrics.addCustomChart(new SimplePie("gui_layout", () -> this.getConfig().getString("gui_layout", "default")));
      metrics.addCustomChart(new AdvancedPie("protection_plugins", () -> {
         Map<String, Integer> map = new HashMap<>();
         if (this.integrationManager.isHasWorldGuard()) {
            map.put("WorldGuard", 1);
         }

         if (this.integrationManager.isHasTowny()) {
            map.put("Towny", 1);
         }

         if (this.integrationManager.isHasLands()) {
            map.put("Lands", 1);
         }

         if (this.integrationManager.isHasGriefPrevention()) {
            map.put("GriefPrevention", 1);
         }

         if (this.integrationManager.isHasSuperiorSkyblock2()) {
            map.put("SuperiorSkyblock2", 1);
         }

         if (this.integrationManager.isHasBentoBox()) {
            map.put("BentoBox", 1);
         }

         if (this.integrationManager.isHasIridiumSkyblock()) {
            map.put("IridiumSkyblock", 1);
         }

         if (this.integrationManager.isHasPlotSquared()) {
            map.put("PlotSquared", 1);
         }

         if (this.integrationManager.isHasResidence()) {
            map.put("Residence", 1);
         }

         if (this.integrationManager.isHasMinePlots()) {
            map.put("MinePlots", 1);
         }

         if (this.integrationManager.isHasSimpleClaimSystem()) {
            map.put("SimpleClaimSystem", 1);
         }

         if (map.isEmpty()) {
            map.put("None", 1);
         }

         return map;
      }));
   }

   private static String bucketSpawnerCount(long value) {
      if (value == 0L) {
         return "0";
      } else if (value <= 100L) {
         return "1-100";
      } else if (value <= 500L) {
         return "101-500";
      } else if (value <= 1000L) {
         return "501-1K";
      } else if (value <= 5000L) {
         return "1K-5K";
      } else if (value <= 10000L) {
         return "5K-10K";
      } else if (value <= 50000L) {
         return "10K-50K";
      } else if (value <= 100000L) {
         return "50K-100K";
      } else if (value <= 500000L) {
         return "100K-500K";
      } else if (value <= 1000000L) {
         return "500K-1M";
      } else if (value <= 5000000L) {
         return "1M-5M";
      } else if (value <= 10000000L) {
         return "5M-10M";
      } else if (value <= 50000000L) {
         return "10M-50M";
      } else {
         return value <= 100000000L ? "50M-100M" : "100M+";
      }
   }

   public void reload() {
      this.guiLayoutConfig.reloadLayouts();
      if (this.spawnerMenuUI != null) {
         this.spawnerMenuUI.clearCache();
      }

      this.spawnerStorageAction.loadConfig();
      this.spawnerStorageUI.reload();
      this.filterConfigUI.reload();
      if (this.spawnerSellConfirmUI != null) {
         this.spawnerSellConfirmUI.reload();
      }

      this.integrationManager.reload();
      this.spawnerMenuAction.reload();
      if (this.spawnerBreakListener != null) {
         this.spawnerBreakListener.loadConfig();
      }

      this.timeFormatter.clearCache();
      if (this.spawnerSettingsConfig != null) {
         this.spawnerSettingsConfig.reload();
         SpawnerMobHeadTexture.clearCache();
      }

      if (this.itemSpawnerSettingsConfig != null) {
         this.itemSpawnerSettingsConfig.reload();
      }

      SpawnerNames.warnConflicts(this);
      if (this.lootEditorService != null) {
         this.lootEditorService.reload();
      }

      this.loggingConfig.loadConfig();
      this.spawnerActionLogger.reloadDiscord();
      if (this.spawnerAuditListener != null) {
         HandlerList.unregisterAll(this.spawnerAuditListener);
      }

      this.spawnerAuditListener = new SpawnerAuditListener(this.spawnerActionLogger);
      this.getServer().getPluginManager().registerEvents(this.spawnerAuditListener, this);
   }

   public void onDisable() {
      this.saveAndCleanup();
      SpawnerMobHeadTexture.clearCache();
      this.getLogger().info("SmartSpawner has been disabled!");
   }

   private void saveAndCleanup() {
      if (this.spawnerManager != null) {
         try {
            if (this.spawnerStorage != null) {
               this.spawnerStorage.shutdown();
            }

            if (this.databaseManager != null) {
               this.databaseManager.shutdown();
            }

            this.spawnerManager.cleanupAllSpawners();
         } catch (Exception var2) {
            this.getLogger().log(Level.SEVERE, "Error saving spawner data during shutdown", (Throwable)var2);
         }
      }

      if (this.itemPriceManager != null) {
         this.itemPriceManager.cleanup();
      }

      if (this.spawnerActionLogger != null) {
         this.spawnerActionLogger.shutdown();
      }

      if (this.spawnerHighlightManager != null) {
         this.spawnerHighlightManager.cleanup();
      }

      this.cleanupResources();
   }

   private void cleanupResources() {
      if (this.rangeChecker != null) {
         this.rangeChecker.cleanup();
      }

      if (this.spawnerGuiViewManager != null) {
         this.spawnerGuiViewManager.cleanup();
      }

      if (this.hopperService != null) {
         this.hopperService.cleanup();
      }

      if (this.spawnerClickManager != null) {
         this.spawnerClickManager.cleanup();
      }

      if (this.spawnerStorageUI != null) {
         this.spawnerStorageUI.cleanup();
      }
   }

   public SpawnerProvider getSpawnerProvider() {
      return new SpawnerProvider(this);
   }

   public boolean hasSellIntegration() {
      return this.itemPriceManager == null ? false : this.itemPriceManager.hasSellIntegration();
   }

   public long getTimeFromConfig(String path, String defaultValue) {
      return this.timeFormatter.getTimeFromConfig(path, defaultValue);
   }

   @Generated
   public int getDATA_VERSION() {
      return 3;
   }

   @Generated
   public IntegrationManager getIntegrationManager() {
      return this.integrationManager;
   }

   @Generated
   public TimeFormatter getTimeFormatter() {
      return this.timeFormatter;
   }

   @Generated
   public ConfigUpdater getConfigUpdater() {
      return this.configUpdater;
   }

   @Generated
   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   @Generated
   public LanguageUpdater getLanguageUpdater() {
      return this.languageUpdater;
   }

   @Generated
   public MessageService getMessageService() {
      return this.messageService;
   }

   @Generated
   public SpawnerSettingsConfig getSpawnerSettingsConfig() {
      return this.spawnerSettingsConfig;
   }

   @Generated
   public ItemSpawnerSettingsConfig getItemSpawnerSettingsConfig() {
      return this.itemSpawnerSettingsConfig;
   }

   @Generated
   public SpawnerItemFactory getSpawnerItemFactory() {
      return this.spawnerItemFactory;
   }

   @Generated
   public ExternalGuiLayoutLoader getGuiLayoutLoader() {
      return this.guiLayoutLoader;
   }

   @Generated
   public GuiLayoutRegistryImpl getGuiLayoutRegistry() {
      return this.guiLayoutRegistry;
   }

   @Generated
   public GuiLayoutConfig getGuiLayoutConfig() {
      return this.guiLayoutConfig;
   }

   @Generated
   public GuiButtonInteractionService getGuiButtonInteractionService() {
      return this.guiButtonInteractionService;
   }

   @Generated
   public SpawnerMenuUI getSpawnerMenuUI() {
      return this.spawnerMenuUI;
   }

   @Generated
   public SpawnerStorageUI getSpawnerStorageUI() {
      return this.spawnerStorageUI;
   }

   @Generated
   public FilterConfigUI getFilterConfigUI() {
      return this.filterConfigUI;
   }

   @Generated
   public SpawnerSellConfirmUI getSpawnerSellConfirmUI() {
      return this.spawnerSellConfirmUI;
   }

   @Generated
   public SpawnEggHandler getSpawnEggHandler() {
      return this.spawnEggHandler;
   }

   @Generated
   public SpawnerClickManager getSpawnerClickManager() {
      return this.spawnerClickManager;
   }

   @Generated
   public SpawnerStackHandler getSpawnerStackHandler() {
      return this.spawnerStackHandler;
   }

   @Generated
   public SpawnerMenuAction getSpawnerMenuAction() {
      return this.spawnerMenuAction;
   }

   @Generated
   public SpawnerStorageAction getSpawnerStorageAction() {
      return this.spawnerStorageAction;
   }

   @Generated
   public SpawnerSellManager getSpawnerSellManager() {
      return this.spawnerSellManager;
   }

   @Generated
   public SpawnerSellConfirmListener getSpawnerSellConfirmListener() {
      return this.spawnerSellConfirmListener;
   }

   @Generated
   public SpawnerStorage getSpawnerStorage() {
      return this.spawnerStorage;
   }

   @Generated
   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   @Generated
   public SpawnerManager getSpawnerManager() {
      return this.spawnerManager;
   }

   @Generated
   public HopperService getHopperService() {
      return this.hopperService;
   }

   @Generated
   public HopperConfig getHopperConfig() {
      return this.hopperConfig;
   }

   @Generated
   public SpawnerRemovalService getSpawnerRemovalService() {
      return this.spawnerRemovalService;
   }

   @Generated
   public NaturalSpawnerListener getNaturalSpawnerListener() {
      return this.naturalSpawnerListener;
   }

   @Generated
   public SpawnerLootGenerator getSpawnerLootGenerator() {
      return this.spawnerLootGenerator;
   }

   @Generated
   public SpawnerRangeChecker getRangeChecker() {
      return this.rangeChecker;
   }

   @Generated
   public SpawnerGuiViewManager getSpawnerGuiViewManager() {
      return this.spawnerGuiViewManager;
   }

   @Generated
   public SpawnerExplosionListener getSpawnerExplosionListener() {
      return this.spawnerExplosionListener;
   }

   @Generated
   public SpawnerBreakListener getSpawnerBreakListener() {
      return this.spawnerBreakListener;
   }

   @Generated
   public SpawnerPlaceListener getSpawnerPlaceListener() {
      return this.spawnerPlaceListener;
   }

   @Generated
   public WorldEventHandler getWorldEventHandler() {
      return this.worldEventHandler;
   }

   @Generated
   public ItemPriceManager getItemPriceManager() {
      return this.itemPriceManager;
   }

   @Generated
   public UpdateChecker getUpdateChecker() {
      return this.updateChecker;
   }

   @Generated
   public BrigadierCommandManager getBrigadierCommandManager() {
      return this.brigadierCommandManager;
   }

   @Generated
   public ListSubCommand getListSubCommand() {
      return this.listSubCommand;
   }

   @Generated
   public UserPreferenceCache getUserPreferenceCache() {
      return this.userPreferenceCache;
   }

   @Generated
   public SpawnerListGUI getSpawnerListGUI() {
      return this.spawnerListGUI;
   }

   @Generated
   public SpawnerManagementHandler getSpawnerManagementHandler() {
      return this.spawnerManagementHandler;
   }

   @Generated
   public ServerSelectionHandler getServerSelectionHandler() {
      return this.serverSelectionHandler;
   }

   @Generated
   public PricesGUI getPricesGUI() {
      return this.pricesGUI;
   }

   @Generated
   public LootEditorService getLootEditorService() {
      return this.lootEditorService;
   }

   @Generated
   public LootEditorUI getLootEditorUI() {
      return this.lootEditorUI;
   }

   @Generated
   public LootEditorDialogs getLootEditorDialogs() {
      return this.lootEditorDialogs;
   }

   @Generated
   public LootEditorHandler getLootEditorHandler() {
      return this.lootEditorHandler;
   }

   @Generated
   public SpawnerAuditListener getSpawnerAuditListener() {
      return this.spawnerAuditListener;
   }

   @Generated
   public LoggingConfig getLoggingConfig() {
      return this.loggingConfig;
   }

   @Generated
   public SpawnerHighlightManager getSpawnerHighlightManager() {
      return this.spawnerHighlightManager;
   }

   @Generated
   public NearResultGUI getNearResultGUI() {
      return this.nearResultGUI;
   }

   @Generated
   public SmartSpawnerAPIImpl getApiImpl() {
      return this.apiImpl;
   }

   @Generated
   public static SmartSpawner getInstance() {
      return instance;
   }

   @Generated
   public SpawnerActionLogger getSpawnerActionLogger() {
      return this.spawnerActionLogger;
   }
}
