package com.ranull.graves;

import com.ranull.graves.bundledlibraries.scheduler.GlobalScheduler;
import com.ranull.graves.command.GravesCommand;
import com.ranull.graves.compatibility.Compatibility;
import com.ranull.graves.compatibility.CompatibilityBlockData;
import com.ranull.graves.compatibility.CompatibilityMaterialData;
import com.ranull.graves.listener.BlockBreakListener;
import com.ranull.graves.listener.BlockBurnAndIgniteListener;
import com.ranull.graves.listener.BlockFromToListener;
import com.ranull.graves.listener.BlockPistonExtendListener;
import com.ranull.graves.listener.BlockPlaceListener;
import com.ranull.graves.listener.CreatureSpawnListener;
import com.ranull.graves.listener.EntityDamageByEntityListener;
import com.ranull.graves.listener.EntityDeathListener;
import com.ranull.graves.listener.ExplosionPrimeListener;
import com.ranull.graves.listener.HangingBreakListener;
import com.ranull.graves.listener.InventoryClickListener;
import com.ranull.graves.listener.InventoryCloseListener;
import com.ranull.graves.listener.InventoryDragListener;
import com.ranull.graves.listener.InventoryOpenListener;
import com.ranull.graves.listener.PlayerBucketListener;
import com.ranull.graves.listener.PlayerDeathListener;
import com.ranull.graves.listener.PlayerDropItemListener;
import com.ranull.graves.listener.PlayerInteractAtEntityListener;
import com.ranull.graves.listener.PlayerInteractEntityListener;
import com.ranull.graves.listener.PlayerInteractListener;
import com.ranull.graves.listener.PlayerJoinListener;
import com.ranull.graves.listener.PlayerMoveListener;
import com.ranull.graves.listener.PlayerQuitListener;
import com.ranull.graves.listener.PlayerRespawnListener;
import com.ranull.graves.listener.PlayerTeleportListener;
import com.ranull.graves.listener.ProjectileHitListener;
import com.ranull.graves.manager.BlockManager;
import com.ranull.graves.manager.CacheManager;
import com.ranull.graves.manager.DataManager;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.manager.EntityManager;
import com.ranull.graves.manager.GUIManager;
import com.ranull.graves.manager.GraveManager;
import com.ranull.graves.manager.HologramManager;
import com.ranull.graves.manager.ImportManager;
import com.ranull.graves.manager.IntegrationManager;
import com.ranull.graves.manager.ItemStackManager;
import com.ranull.graves.manager.LocationManager;
import com.ranull.graves.manager.RecipeManager;
import com.ranull.graves.manager.VersionManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.ResourceUtil;
import com.ranull.graves.util.ServerUtil;
import com.ranull.graves.util.UpdateUtil;
import dev.cwhead.GravesX.addon.GravesXAddon;
import dev.cwhead.GravesX.api.provider.RegisterGraveProviders;
import dev.cwhead.GravesX.command.GxModulesCommand;
import dev.cwhead.GravesX.debug.KeepInventoryDetector;
import dev.cwhead.GravesX.debug.LateEnableHook;
import dev.cwhead.GravesX.exception.GravesXMethodNotSupportedException;
import dev.cwhead.GravesX.listener.BlockEntityExplodeListener;
import dev.cwhead.GravesX.listener.PlayerAfterRespawnListener;
import dev.cwhead.GravesX.manager.ArmorStandManager;
import dev.cwhead.GravesX.manager.ChunkManager;
import dev.cwhead.GravesX.manager.ConfigManager;
import dev.cwhead.GravesX.manager.DebugManager;
import dev.cwhead.GravesX.manager.MetricsManager;
import dev.cwhead.GravesX.manager.ParticleManager;
import dev.cwhead.GravesX.manager.PermissionManager;
import dev.cwhead.GravesX.manager.SafeLocationManager;
import dev.cwhead.GravesX.manager.SchedulerManager;
import dev.cwhead.GravesX.manager.TextDisplayManager;
import dev.cwhead.GravesX.module.GravesXModuleController;
import dev.cwhead.GravesX.module.ModuleManager;
import dev.cwhead.GravesX.module.listener.DependencyEnableListener;
import dev.cwhead.GravesX.module.util.LibbyImporter;
import dev.cwhead.GravesX.module.util.ModuleInfo;
import dev.cwhead.GravesX.util.LibraryLoaderUtil;
import dev.cwhead.GravesX.util.MclogsUtil;
import dev.cwhead.GravesX.util.PastebinUtil;
import dev.cwhead.GravesX.util.ToptalUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public class Graves extends JavaPlugin {
   private VersionManager versionManager;
   private IntegrationManager integrationManager;
   private CacheManager cacheManager;
   private DataManager dataManager;
   private ImportManager importManager;
   private BlockManager blockManager;
   private ItemStackManager itemStackManager;
   private EntityDataManager entityDataManager;
   private HologramManager hologramManager;
   private GUIManager guiManager;
   private EntityManager entityManager;
   private RecipeManager recipeManager;
   private LocationManager locationManager;
   private SafeLocationManager safeLocationManager;
   private GraveManager graveManager;
   private ParticleManager particleManager;
   private DebugManager debugManager;
   private PermissionManager permissionManager;
   private ConfigManager configManager;
   private ChunkManager chunkManager;
   private Compatibility compatibility;
   private FileConfiguration fileConfiguration;
   private boolean isDevelopmentBuild = false;
   private boolean isOutdatedBuild = false;
   private boolean isUnknownBuild = false;
   private SchedulerManager graveScheduler;
   private ModuleManager moduleManager;
   private boolean deferModuleLoad;
   private ArmorStandManager armorStandManager;
   private TextDisplayManager textDisplayManager;
   private final AtomicBoolean reloading = new AtomicBoolean(false);
   private final AtomicBoolean normalDisable = new AtomicBoolean(false);
   private final AtomicBoolean shutdownTasksRan = new AtomicBoolean(false);

   public void onLoad() {
      this.graveScheduler = new SchedulerManager(this);
      this.debugManager = new DebugManager(this);
      this.configManager = new ConfigManager(this);

      try {
         if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
            this.getLogger()
               .severe(
                  "GravesX does not have permission to create or access the plugin data folder "
                     + this.getDataFolder().getAbsolutePath()
                     + ". Ensure the server process user has read/write access to this folder. If you're on Linux, you may need to adjust ownership/permissions (chown/chmod). If you're on Windows, ensure the account running the server has Modify permission."
               );
         }
      } catch (SecurityException var7) {
         this.getLogger()
            .severe(
               "GravesX does not have permission to create or access the plugin data folder "
                  + this.getDataFolder().getAbsolutePath()
                  + ". Ensure the server process user has read/write access to this folder."
            );
         this.logStackTrace((Exception)var7);
      }

      File pluginsDir = this.getDataFolder().getParentFile();
      File gravesDirectory = new File(pluginsDir, "Graves");
      File newGravesDirectory = new File(pluginsDir, "GravesX");
      if (gravesDirectory.exists() && gravesDirectory.isDirectory()) {
         this.getLogger().warning("Your server has legacy version of Graves. Migrating the folder to GravesX for you.");

         try {
            if (gravesDirectory.renameTo(newGravesDirectory)) {
               this.getLogger().info("Successfully renamed legacy folder Graves to GravesX.");
            } else {
               this.getLogger()
                  .severe(
                     "Failed to rename legacy folder Graves to GravesX. Ensure GravesX has permission to rename the folder, and that the destination doesn't already exist."
                  );
            }
         } catch (SecurityException var6) {
            this.getLogger()
               .severe(
                  "GravesX does not have permission to rename the legacy folder. Ensure the server process user has Modify/write permission from "
                     + gravesDirectory.getAbsolutePath()
                     + " to "
                     + newGravesDirectory.getAbsolutePath()
               );
            this.logStackTrace((Exception)var6);
         }
      }

      try {
         if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
            this.getLogger()
               .severe(
                  "GravesX does not have permission to create or access the plugin data folder after migration of "
                     + this.getDataFolder().getAbsolutePath()
                     + ". Ensure the server process user has read/write access to this folder."
               );
         }
      } catch (SecurityException var5) {
         this.getLogger()
            .severe("GravesX does not have permission to create or access the plugin data folder after migration: " + this.getDataFolder().getAbsolutePath());
         this.logStackTrace((Exception)var5);
      }

      if (this.configManager == null) {
         this.configManager = new ConfigManager(this);
      }

      this.configManager.ensureDefaultsExist();
      GravesXAddon.ensureAddonRoot(this);
      this.integrationManager = new IntegrationManager(this);
      this.moduleManager = new ModuleManager(this);
      this.moduleManager.setLibraryImporter(new LibbyImporter(this));
      this.moduleManager.enableAll(GravesXModuleController.LoadPhase.STARTUP);
      this.deferModuleLoad = this.moduleManager.shouldDeferLoadOnExternalPlugins();
      if (!this.deferModuleLoad) {
         this.moduleManager.loadAll();
      }
   }

   public void onEnable() {
      this.graveScheduler = new SchedulerManager(this);
      this.debugManager = new DebugManager(this);
      if (this.configManager == null) {
         this.configManager = new ConfigManager(this);
      }

      this.configManager.ensureDefaultsExist();
      this.configManager.updateIfNeeded(this.isPluginDevelopmentBuild());
      this.configManager.reload();
      this.loadLibraries();
      this.versionManager = new VersionManager();
      this.integrationManager.load();
      this.integrationManager.loadNoReload();
      this.cacheManager = new CacheManager();
      this.dataManager = new DataManager(this);
      this.importManager = new ImportManager(this);
      this.blockManager = new BlockManager(this);
      this.itemStackManager = new ItemStackManager(this);
      this.entityDataManager = new EntityDataManager(this);
      this.hologramManager = new HologramManager(this);
      this.guiManager = new GUIManager(this);
      this.entityManager = new EntityManager(this);
      this.locationManager = new LocationManager(this);
      this.safeLocationManager = new SafeLocationManager(this);
      this.graveManager = new GraveManager(this);
      this.particleManager = new ParticleManager(this);
      this.permissionManager = new PermissionManager(this);
      this.chunkManager = new ChunkManager(this);
      this.getLogger().info("[Modules] Loading GravesX Modules...");
      if (this.moduleManager == null) {
         this.moduleManager = new ModuleManager(this);
         this.moduleManager.setLibraryImporter(new LibbyImporter(this));
         this.deferModuleLoad = this.moduleManager.shouldDeferLoadOnExternalPlugins();
      }

      List<ModuleInfo> detected = this.moduleManager.detectModules();
      if (detected.isEmpty()) {
         this.getLogger().info("[Modules] Detected 0 module(s).");
      } else {
         this.getLogger().info("[Modules] Detected " + detected.size() + " module(s):");

         for (ModuleInfo mi : detected) {
            int libs = mi.libraries() == null ? 0 : mi.libraries().size();
            this.getLogger()
               .info("[Modules] - " + mi.name() + " v" + mi.version() + " (load=" + mi.loadPhase() + ", folia=" + mi.supportsFolia() + ", libs=" + libs + ")");
         }
      }

      if (this.deferModuleLoad) {
         this.moduleManager.loadAll();
      }

      if (this.versionManager.isHasTextDisplays()) {
         this.textDisplayManager = new TextDisplayManager(this);
         this.armorStandManager = new ArmorStandManager(this);
      } else {
         this.armorStandManager = new ArmorStandManager(this);
      }

      this.moduleManager.enableAll(GravesXModuleController.LoadPhase.STARTUP);
      DependencyEnableListener depListener = new DependencyEnableListener(this.moduleManager);
      this.getServer().getPluginManager().registerEvents(depListener, this);
      this.getSchedulerManager().runTask(this.moduleManager::tryEnablePending);
      this.registerCommands();
      this.registerListeners();
      this.registerRecipes();
      this.saveTextFiles();
      this.getSchedulerManager().runTaskLater(() -> {
         KeepInventoryDetector.install(this);
         KeepInventoryDetector.logWorldsWithGameruleKeepInventoryTrue(this);
      }, 1L);
      this.getServer().getPluginManager().registerEvents(new LateEnableHook(), this);
      if (this.getConfig().getBoolean("settings.metrics.enabled", true)) {
         this.getLogger().info("Loading bStats Metrics Manager...");
         MetricsManager metricsManager = new MetricsManager(this);
         metricsManager.registerMetrics();
         metricsManager.registerMetricsLegacy();
      } else {
         this.getLogger().warning("Metrics has been disabled. Metrics will not be sent to bStats.");
      }

      this.moduleManager.enableAll(GravesXModuleController.LoadPhase.POSTWORLD);
      this.moduleManager.tryEnablePending();
      this.getSchedulerManager().runTask(() -> {
         this.compatibilityChecker();
         this.updateChecker();
         this.getConfigManager().updateIfNeeded(this.isPluginDevelopmentBuild());
         this.RegisterSoftCrashHandler();
         this.moduleManager.enableAll(GravesXModuleController.LoadPhase.COMPLETED);
         this.moduleManager.tryEnablePending();
      });
   }

   public void onDisable() {
      this.normalDisable.set(true);
      if (this.moduleManager != null) {
         this.getLogger().info("[Modules] Disabling modules...");
         this.moduleManager.disableAll();
         this.getLogger().info("[Modules] All modules have been disabled. Running remaining shutdown tasks.");
      }

      this.runShutdownTasks();
   }

   private void RegisterSoftCrashHandler() {
      this.getLogger().info("Registering Soft Crash Handler...");

      try {
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!this.normalDisable.get() && !this.shutdownTasksRan.get()) {
               this.getLogger().severe("GravesX detected server went into a crashed state...");
               this.runShutdownTasks();
            }
         }));
         this.getLogger().info("Registered Soft Crash Handler. Server will handle crashes in a separate thread. This does not work on hard crashes.");
      } catch (Exception var2) {
         this.getLogger().severe("Failed to Register Soft Crash Handler. Server will not listen to soft crashed states in a separate thread.");
      }
   }

   private void runShutdownTasks() {
      if (!this.shutdownTasksRan.compareAndSet(false, true)) {
         this.debugMessage("Skipping duplicate shutdown task execution.", 1);
      } else {
         this.getLogger().info("Saving Grave inventories before shutting down...");

         for (Grave grave : this.getCacheManager().getGraveMap().values()) {
            try {
               this.getDataManager().updateGraveMainThread(grave, "inventory", InventoryUtil.inventoryToString(grave.getInventory(), this));
               this.debugMessage("Saved inventory for grave UUID " + grave.getUUID() + " successfully", 2);
            } catch (Exception var10) {
               this.getLogger().severe("Failed to save grave " + grave.getUUID() + " on shutdown: " + var10.getMessage());
               this.logStackTrace(var10);
            }
         }

         this.getLogger().info("Grave inventories saved.");
         this.getLogger().info("Shutting Down GravesX...");
         this.getLogger().info("Unloading and Shutting Down DataManager...");

         try {
            if (this.dataManager != null) {
               this.dataManager.closeConnection();
               this.getLogger().info("Unloaded DataManager Successfully.");
            }
         } catch (Exception var9) {
            this.getLogger().severe("Failed to unload DataManager and shut down Database.");
            this.logStackTrace(var9);
         }

         this.getLogger().info("Unloading GraveManager...");

         try {
            if (this.graveManager != null) {
               this.graveManager.unload();
               this.getLogger().info("Unloaded GraveManager Successfully.");
            }
         } catch (Exception var8) {
            this.getLogger().severe("Failed to unload GraveManager.");
            this.logStackTrace(var8);
         }

         this.getLogger().info("Unloading IntegrationManager...");

         try {
            if (this.integrationManager != null) {
               this.integrationManager.unload();
               this.integrationManager.unloadNoReload();
               this.getLogger().info("Unloaded IntegrationManager Successfully.");
            }
         } catch (Exception var7) {
            this.getLogger().severe("Failed to unload IntegrationManager.");
            this.logStackTrace(var7);
         }

         if (this.recipeManager != null) {
            this.getLogger().info("Unloading RecipeManager...");

            try {
               this.recipeManager.unload();
               this.getLogger().info("Unloaded RecipeManager Successfully.");
            } catch (Exception var6) {
               this.getLogger().severe("Failed to unload RecipeManager.");
               this.logStackTrace(var6);
            }
         }

         if (this.debugManager != null) {
            this.getLogger().info("Unloading DebugManager...");

            try {
               this.debugManager.unload();
               this.getLogger().info("Unloaded DebugManager Successfully.");
            } catch (Exception var5) {
               this.getLogger().severe("Failed to unload DebugManager.");
               this.logStackTrace(var5);
            }
         }

         if (this.graveScheduler != null) {
            this.getLogger().info("Shutting down SchedulerManager...");

            try {
               this.graveScheduler.cancelAll(this);
               this.getLogger().info("Shutting down SchedulerManager Successfully.");
            } catch (Exception var4) {
               this.getLogger().severe("Failed to shut down SchedulerManager.");
               this.logStackTrace(var4);
            }
         }

         this.getLogger().info("Shutdown Completed :)");
      }
   }

   private void loadLibraries() {
      this.getLogger().info("Loading Libraries for GravesX");
      LibraryLoaderUtil libraryLoaderUtil = new LibraryLoaderUtil(this);
      this.getLogger()
         .warning(this.getServer().getName() + " v." + this.getServer().getVersion() + " detected. Using BukkitLibraryManager to download and load libraries.");
      libraryLoaderUtil.loadLibrary("org{}bstats", "bstats-base", "3.2.1", "org{}bstats", "com{}ranull{}graves{}libraries{}bstats", false);
      libraryLoaderUtil.loadLibrary("org{}bstats", "bstats-bukkit", "3.2.1", "org{}bstats", "com{}ranull{}graves{}libraries{}bstats", false);
      libraryLoaderUtil.loadLibrary("com{}zaxxer", "HikariCP", "7.0.2", "com{}zaxxer{}hikari", "com{}ranull{}graves{}libraries{}hikari", false);
      libraryLoaderUtil.loadLibrary("org{}xerial", "sqlite-jdbc", "3.51.2.0", false);

      try {
         Class.forName("org.json.JSONObject");
      } catch (ClassNotFoundException var7) {
         libraryLoaderUtil.loadLibrary("org{}json", "json", "20251224");
      }

      try {
         Class.forName("com.google.gson.Gson");
      } catch (ClassNotFoundException var6) {
         libraryLoaderUtil.loadLibrary("com{}google{}code{}gson", "gson", "2.13.2", false);
      }

      try {
         Class.forName("com.google.common.collect.ImmutableList");
      } catch (ClassNotFoundException var5) {
         libraryLoaderUtil.loadLibrary("com{}google{}guava", "guava", "33.5.0-jre", false);
      }

      String storageType = Objects.requireNonNull(this.getConfig().getString("settings.storage.type")).toUpperCase();
      libraryLoaderUtil.loadLibrary(
         "com{}h2database", "h2", "2.4.240", "org{}h2", "com{}ranull{}graves{}libraries{}h2", false, "https://repo1.maven.org/maven2/"
      );
      switch (storageType) {
         case "POSTGRESQL":
            libraryLoaderUtil.loadLibrary("org{}postgresql", "postgresql", "42.7.11", "org{}postgresql", "com{}ranull{}graves{}libraries{}postgresql", false);
            break;
         case "MARIADB":
            libraryLoaderUtil.loadLibrary("com{}mysql", "mysql-connector-j", "9.6.0", "com{}mysql", "com{}ranull{}graves{}libraries{}mysql", false);
            libraryLoaderUtil.loadLibrary(
               "org{}mariadb{}jdbc", "mariadb-java-client", "3.5.6", "org{}mariadb", "com{}ranull{}graves{}libraries{}mariadb", false
            );
            break;
         case "MYSQL":
            libraryLoaderUtil.loadLibrary("com{}mysql", "mysql-connector-j", "9.6.0", "com{}mysql", "com{}ranull{}graves{}libraries{}mysql", false);
            break;
         case "MSSQL":
            libraryLoaderUtil.loadLibrary(
               "com{}microsoft{}sqlserver", "mssql-jdbc", "13.3.2.jre11-preview", "com{}microsoft", "com{}ranull{}graves{}libraries{}microsoft", false
            );
      }

      libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-platform-bukkit", "4.4.1", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
      libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-api", "4.26.1", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
      libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-text-minimessage", "4.26.1", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
      libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-text-serializer-gson", "4.26.1", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
      libraryLoaderUtil.loadLibrary(
         "com{}github{}puregero",
         "multilib",
         "1.2.4",
         "com{}github{}puregero{}multilib",
         "com{}ranull{}graves{}libraries{}multilib",
         false,
         "https://repo.clojars.org/"
      );
      libraryLoaderUtil.loadLibrary(
         "org{}apache{}commons", "commons-text", "1.15.0", "org{}apache{}commons{}text", "com{}ranull{}graves{}libraries{}commonstext", false
      );
      this.getLogger().info("Finished Loading Libraries for GravesX.");
   }

   public void saveDefaultConfig() {
      this.getConfigManager().ensureDefaultsExist();
   }

   public void reloadConfig() {
      this.getConfigManager().reload();
   }

   @NotNull
   public FileConfiguration getConfig() {
      return this.getConfigManager().config();
   }

   public void reload() {
      if (!this.reloading.compareAndSet(false, true)) {
         this.getLogger().warning("Reload already in progress.");
      } else {
         long started = System.currentTimeMillis();
         this.getLogger().info("==== Reloading " + this.getName() + " ====");

         try {
            this.unloadAllForReload();
            this.loadAllAfterReload();
            long took = System.currentTimeMillis() - started;
            this.infoMessage(this.getName() + " reloaded. (" + took + "ms)");
         } finally {
            this.reloading.set(false);
         }
      }
   }

   private void unloadAllForReload() {
      try {
         if (this.cacheManager != null && this.dataManager != null) {
            this.getLogger().info("Saving Grave inventories before reload...");

            for (Grave grave : this.getCacheManager().getGraveMap().values()) {
               try {
                  this.getDataManager().updateGraveMainThread(grave, "inventory", InventoryUtil.inventoryToString(grave.getInventory(), this));
                  this.debugMessage("Saved inventory for grave UUID " + grave.getUUID() + " successfully", 2);
               } catch (Exception var13) {
                  this.getLogger().severe("Failed to save grave " + grave.getUUID() + " during reload: " + var13.getMessage());
                  this.logStackTrace(var13);
               }
            }

            this.getLogger().info("Grave inventories saved.");
         }
      } catch (Exception var14) {
         this.getLogger().warning("Failed to flush inventories during reload: " + var14.getMessage());
         this.logStackTrace(var14);
      }

      try {
         this.getSchedulerManager().cancelAll(this);
      } catch (Throwable var12) {
      }

      this.tryUnload(this.graveScheduler, "SchedulerManager", "cancelAll", "shutdown", "close", "unload");

      try {
         if (this.graveScheduler != null) {
            this.graveScheduler.cancelAll(this);
         }
      } catch (Exception var11) {
      }

      if (this.moduleManager != null) {
         try {
            this.getLogger().info("[Modules] Disabling modules...");
            this.moduleManager.disableAll();
            this.getLogger().info("[Modules] Modules disabled.");
         } catch (Exception var10) {
            this.getLogger().severe("[Modules] Failed disabling modules during reload: " + var10.getMessage());
            this.logStackTrace(var10);
         }
      }

      try {
         HandlerList.unregisterAll(this);
      } catch (Exception var9) {
         this.getLogger().warning("Failed to unregister all listeners: " + var9.getMessage());
         this.logStackTrace(var9);
      }

      try {
         this.unregisterListeners();
      } catch (Exception var8) {
      }

      this.tryUnload(this.graveManager, "GraveManager", "unload", "shutdown", "close");

      try {
         if (this.graveManager != null) {
            this.graveManager.unload();
         }
      } catch (Exception var7) {
         this.getLogger().severe("Failed to unload GraveManager during reload.");
         this.logStackTrace(var7);
      }

      this.tryUnload(this.integrationManager, "IntegrationManager", "unload", "shutdown", "close");

      try {
         if (this.integrationManager != null) {
            this.integrationManager.unload();

            try {
               if (this.getIntegrationManager().hasSkript()) {
                  this.integrationManager.unloadNoReload();
               }
            } catch (Exception var5) {
            }
         }
      } catch (Exception var6) {
         this.getLogger().severe("Failed to unload IntegrationManager during reload.");
         this.logStackTrace(var6);
      }

      this.tryUnload(this.recipeManager, "RecipeManager", "unload", "reload", "shutdown", "close");
      this.tryUnload(this.chunkManager, "ChunkManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.permissionManager, "PermissionManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.particleManager, "ParticleManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.entityManager, "EntityManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.guiManager, "GUIManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.hologramManager, "HologramManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.entityDataManager, "EntityDataManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.itemStackManager, "ItemStackManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.blockManager, "BlockManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.importManager, "ImportManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.safeLocationManager, "SafeLocationManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.locationManager, "LocationManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.cacheManager, "CacheManager", "unload", "clear", "shutdown", "close", "disable");
      this.tryUnload(this.textDisplayManager, "TextDisplayManager", "unload", "shutdown", "close", "disable");
      this.tryUnload(this.armorStandManager, "ArmorStandManager", "unload", "shutdown", "close", "disable");

      try {
         if (this.dataManager != null) {
            this.getLogger().info("Closing DataManager (DB) for reload...");
            this.dataManager.closeConnection();
            this.getLogger().info("DataManager closed.");
         }
      } catch (Exception var4) {
         this.getLogger().severe("Failed to close DataManager during reload.");
         this.logStackTrace(var4);
      }

      this.recipeManager = null;
      this.graveManager = null;
      this.chunkManager = null;
      this.permissionManager = null;
      this.particleManager = null;
      this.entityManager = null;
      this.guiManager = null;
      this.hologramManager = null;
      this.entityDataManager = null;
      this.itemStackManager = null;
      this.blockManager = null;
      this.importManager = null;
      this.safeLocationManager = null;
      this.locationManager = null;
      this.cacheManager = null;
      this.dataManager = null;
      this.textDisplayManager = null;
      this.armorStandManager = null;
      this.graveScheduler = null;
      this.moduleManager = null;
      this.getLogger().info("All managers unloaded and cleared.");
   }

   private void loadAllAfterReload() {
      RegisterGraveProviders.clear();
      this.getConfigManager().ensureDefaultsExist();
      this.getConfigManager().updateIfNeeded(this.isPluginDevelopmentBuild());
      this.getConfigManager().reload();
      this.saveTextFiles();
      this.loadLibraries();
      this.graveScheduler = new SchedulerManager(this);
      this.versionManager = new VersionManager();
      this.integrationManager.load();

      try {
         if (this.getIntegrationManager().hasSkript()) {
            this.integrationManager.loadNoReload();
         }
      } catch (Exception var5) {
      }

      this.cacheManager = new CacheManager();
      this.dataManager = new DataManager(this);
      this.importManager = new ImportManager(this);
      this.blockManager = new BlockManager(this);
      this.itemStackManager = new ItemStackManager(this);
      this.entityDataManager = new EntityDataManager(this);
      this.hologramManager = new HologramManager(this);
      this.guiManager = new GUIManager(this);
      this.entityManager = new EntityManager(this);
      this.locationManager = new LocationManager(this);
      this.safeLocationManager = new SafeLocationManager(this);
      this.graveManager = new GraveManager(this);
      this.particleManager = new ParticleManager(this);
      this.permissionManager = new PermissionManager(this);
      this.chunkManager = new ChunkManager(this);
      this.getLogger().info("[Modules] Loading GravesX Modules...");
      this.moduleManager = new ModuleManager(this);
      this.moduleManager.setLibraryImporter(new LibbyImporter(this));
      this.deferModuleLoad = this.moduleManager.shouldDeferLoadOnExternalPlugins();
      List<ModuleInfo> detected = this.moduleManager.detectModules();
      if (detected.isEmpty()) {
         this.getLogger().info("[Modules] Detected 0 module(s).");
      } else {
         this.getLogger().info("[Modules] Detected " + detected.size() + " module(s):");

         for (ModuleInfo mi : detected) {
            int libs = mi.libraries() == null ? 0 : mi.libraries().size();
            this.getLogger()
               .info("[Modules] - " + mi.name() + " v" + mi.version() + " (load=" + mi.loadPhase() + ", folia=" + mi.supportsFolia() + ", libs=" + libs + ")");
         }
      }

      if (this.deferModuleLoad) {
         this.moduleManager.loadAll();
      }

      if (this.versionManager.isHasTextDisplays()) {
         this.textDisplayManager = new TextDisplayManager(this);
         this.armorStandManager = new ArmorStandManager(this);
      } else {
         this.armorStandManager = new ArmorStandManager(this);
         this.textDisplayManager = null;
      }

      this.moduleManager.enableAll(GravesXModuleController.LoadPhase.STARTUP);
      DependencyEnableListener depListener = new DependencyEnableListener(this.moduleManager);
      this.getServer().getPluginManager().registerEvents(depListener, this);
      this.getSchedulerManager().runTask(this.moduleManager::tryEnablePending);
      this.registerCommands();
      this.registerListeners();
      this.registerRecipes();
      this.saveTextFiles();
      this.getSchedulerManager().runTaskLater(() -> {
         KeepInventoryDetector.install(this);
         KeepInventoryDetector.logWorldsWithGameruleKeepInventoryTrue(this);
      }, 1L);
      this.getServer().getPluginManager().registerEvents(new LateEnableHook(), this);
      this.moduleManager.enableAll(GravesXModuleController.LoadPhase.POSTWORLD);
      this.moduleManager.tryEnablePending();
      this.getSchedulerManager().runTask(() -> {
         this.compatibilityChecker();
         this.updateChecker();
         this.getConfigManager().updateIfNeeded(this.isPluginDevelopmentBuild());
         this.moduleManager.enableAll(GravesXModuleController.LoadPhase.COMPLETED);
         this.moduleManager.tryEnablePending();
         RegisterGraveProviders.bootstrapFromServices();
      });
   }

   private void tryUnload(Object target, String label, String... methodNames) {
      if (target != null) {
         String[] var4 = methodNames;
         int var5 = methodNames.length;
         int var6 = 0;

         while (var6 < var5) {
            String name = var4[var6];

            try {
               Method m = target.getClass().getMethod(name);
               m.setAccessible(true);
               m.invoke(target);
               return;
            } catch (NoSuchMethodException var9) {
               var6++;
            } catch (Throwable var10) {
               this.getLogger().warning("Error unloading " + label + " via " + name + "(): " + var10.getMessage());
               this.logStackTrace(var10);
               return;
            }
         }
      }
   }

   public void saveTextFiles() {
      ResourceUtil.copyResources("data/text/readme.txt", this.getDataFolder().getPath() + "/readme.txt", this);
      ResourceUtil.copyResources("data/text/placeholders.txt", this.getDataFolder().getPath() + "/placeholders.txt", this);
      if (this.integrationManager != null) {
         if (this.integrationManager.hasPlaceholderAPI()) {
            ResourceUtil.copyResources("data/text/placeholderapi.txt", this.getDataFolder().getPath() + "/placeholderapi.txt", this);
         }

         if (this.integrationManager.hasFurnitureLib()) {
            ResourceUtil.copyResources("data/text/furniturelib.txt", this.getDataFolder().getPath() + "/furniturelib.txt", this);
         }

         if (this.integrationManager.hasSkript()) {
            ResourceUtil.copyResources("data/text/skript.txt", this.getDataFolder().getPath() + "/skript.txt", this);
         }
      }
   }

   public void registerListeners() {
      this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerBucketListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
      if (this.getVersionManager().isFolia()) {
         this.getServer().getPluginManager().registerEvents(new PlayerAfterRespawnListener(this), this);
      } else {
         this.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
      }

      this.getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
      this.getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockEntityExplodeListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockBurnAndIgniteListener(this), this);
      this.getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockFromToListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockPistonExtendListener(this), this);
      this.getServer().getPluginManager().registerEvents(new HangingBreakListener(this), this);
      this.getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
      this.getServer().getPluginManager().registerEvents(new InventoryDragListener(this), this);
      this.getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
      this.getServer().getPluginManager().registerEvents(new InventoryOpenListener(this), this);
      this.getServer().getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ExplosionPrimeListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);
      if (!this.versionManager.is_v1_7()) {
         this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this), this);
      }
   }

   public void unregisterListeners() {
      HandlerList.unregisterAll(this);
   }

   private void registerRecipes() {
      if (this.versionManager.hasPersistentData() && !this.versionManager.isMohist()) {
         this.recipeManager = new RecipeManager(this);
      }
   }

   private void registerCommands() {
      PluginCommand gravesPluginCommand = this.getCommand("graves");
      if (gravesPluginCommand != null) {
         GravesCommand gravesCommand = new GravesCommand(this);
         gravesPluginCommand.setExecutor(gravesCommand);
         gravesPluginCommand.setTabCompleter(gravesCommand);
      }

      PluginCommand cmd = this.getCommand("gravesmodule");
      if (cmd != null) {
         GxModulesCommand handler = new GxModulesCommand(this.moduleManager, this);
         cmd.setExecutor(handler);
         cmd.setTabCompleter(handler);
      }
   }

   public void debugMessage(String string, int level) {
      this.getDebugManager().debug(string, level);
   }

   public void warningMessage(String string) {
      this.getLogger().warning("Warning: " + string);
   }

   public void compatibilityMessage(String string) {
      this.getLogger().warning("Compatibility: " + string + " This is not a bug.");
   }

   public void infoMessage(String string) {
      this.getLogger().info("Information: " + string);
   }

   public void testMessage(String string) {
      this.getLogger().info("Test: " + string);
   }

   public void updateMessage(String string) {
      this.getLogger().info("Update: " + string);
   }

   public void integrationMessage(String string) {
      this.integrationMessage(string, "info");
   }

   public void integrationMessage(String string, String messageType) {
      switch (messageType) {
         case "warning":
         case "warn":
            this.getLogger().warning("Integration: " + string);
            break;
         case "severe":
         case "error":
            this.getLogger().severe("Integration: " + string);
            break;
         case "info":
         case "debug":
            this.getLogger().info("Integration: " + string);
            break;
         default:
            this.getLogger().info("Integration: " + string);
      }
   }

   private void updateChecker() {
      String installedVersion = this.getDescription().getVersion();
      boolean isDevelopmentBuild = installedVersion.matches("(?i)^\\d+(?:\\.\\d+)+(?:-self|-build\\d+)$");
      if (isDevelopmentBuild) {
         this.setPluginDevelopmentBuild(true);
      }

      if (this.getConfig().getBoolean("settings.update.check")) {
         this.getSchedulerManager()
            .runTaskAsynchronously(
               () -> {
                  String latestVersion = this.getLatestVersion();
                  if (latestVersion != null && !installedVersion.equalsIgnoreCase(latestVersion)) {
                     try {
                        String comparableInstalledVersion = installedVersion.replaceFirst("(?i)(-self|-build\\d+)$", "");
                        String comparableLatestVersion = latestVersion.replaceFirst("(?i)(-self|-build\\d+)$", "");
                        int comparisonResult = this.compareVersions(comparableInstalledVersion, comparableLatestVersion);
                        if (comparisonResult < 0) {
                           this.setPluginOutdatedBuild(true);
                           this.getLogger().warning("You are using an outdated version of " + this.getDescription().getName() + ".");
                           this.getLogger().warning("Installed Version: " + installedVersion);
                           this.getLogger().warning("Latest Version:  " + latestVersion);
                           this.getLogger().warning("Grab the latest release from https://www.spigotmc.org/resources/" + this.getSpigotID() + "/");
                        } else if (comparisonResult <= 0 && !isDevelopmentBuild) {
                           this.getLogger().info("You are running the latest version of " + this.getDescription().getName() + ".");
                        } else {
                           this.setPluginDevelopmentBuild(true);
                           this.getLogger()
                              .severe(
                                 "You are running "
                                    + this.getDescription().getName()
                                    + " version "
                                    + installedVersion
                                    + ", which is a development build and is not production safe."
                              );
                           this.getLogger()
                              .severe(
                                 "THERE WILL NOT BE SUPPORT IF YOU LOSE GRAVE DATA FROM DEVELOPMENT OR COMPILED BUILDS. THIS BUILD IS FOR TESTING PURPOSES ONLY"
                              );
                           this.getLogger().severe("Keep note that you are using a development version when you report bugs.");
                           this.getLogger().severe("If the same issue occurs in " + latestVersion + ", then let us know in https://discord.ranull.com/.");
                        }
                     } catch (NumberFormatException var7) {
                        this.setPluginUnknownBuild(true);
                        this.getLogger().severe("NumberFormatException: " + var7.getMessage());
                        if (!installedVersion.equalsIgnoreCase(latestVersion)) {
                           this.getLogger()
                              .severe("You are either running an outdated version of " + this.getDescription().getName() + " or a development version.");
                           this.getLogger().severe("Installed Version: " + installedVersion);
                           this.getLogger().severe("Latest Version:  " + latestVersion);
                        }
                     }
                  } else if (isDevelopmentBuild) {
                     this.setPluginDevelopmentBuild(true);
                     this.getLogger()
                        .severe(
                           "You are running "
                              + this.getDescription().getName()
                              + " version "
                              + installedVersion
                              + ", which is a development build and is not production safe."
                        );
                     this.getLogger()
                        .severe("THERE WILL NOT BE SUPPORT IF YOU LOSE GRAVE DATA FROM DEVELOPMENT OR COMPILED BUILDS. THIS BUILD IS FOR TESTING PURPOSES ONLY");
                     this.getLogger().severe("Keep note that you are using a development version when you report bugs.");
                     if (latestVersion != null) {
                        this.getLogger().severe("If the same issue occurs in " + latestVersion + ", then let us know in https://discord.ranull.com/.");
                     }
                  }
               }
            );
      }
   }

   private int compareVersions(String version1, String version2) {
      String[] levels1 = version1.split("\\.");
      String[] levels2 = version2.split("\\.");
      int length = Math.max(levels1.length, levels2.length);

      for (int i = 0; i < length; i++) {
         int v1 = i < levels1.length ? this.parseVersionPart(levels1[i]) : 0;
         int v2 = i < levels2.length ? this.parseVersionPart(levels2[i]) : 0;
         if (v1 < v2) {
            return -1;
         }

         if (v1 > v2) {
            return 1;
         }
      }

      return 0;
   }

   private int parseVersionPart(String part) {
      try {
         return Integer.parseInt(part);
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   private void compatibilityChecker() {
      this.compatibility = (Compatibility)(this.versionManager.hasBlockData() ? new CompatibilityBlockData() : new CompatibilityMaterialData());
      if (!this.versionManager.hasBlockData()) {
         this.infoMessage(
            "Legacy version detected, Graves will run but may have problems with material names, the default config is setup for the latest version of the game, you can alter the config manually to fix any issues you encounter, you will need to find the names of materials and sounds for your version."
         );
      }

      if (this.versionManager.isBukkit()) {
         this.infoMessage("Bukkit detected, some functions won't work on Bukkit, like hex codes.");
      }

      if (this.versionManager.isMohist()) {
         this.infoMessage(
            "Mohist detected, not injecting custom recipes. We also do not recommend Mohist nor will provide support if something goes wrong. Read here why: https://essentialsx.net/do-not-use-mohist.html"
         );
      }
   }

   public void dumpServerInfo(CommandSender commandSender) {
      if (this.isEnabled()) {
         this.getSchedulerManager().runTaskAsynchronously(() -> {
            String serverDumpInfo = ServerUtil.getServerDumpInfo(this);
            String message = serverDumpInfo;
            String method = this.getConfig().getString("settings.dump.method", "MCLOGS").trim().toUpperCase();
            String response = null;

            try {
               switch (method) {
                  case "HASTEBIN":
                  case "TOPTAL":
                     ConfigurationSection tp = this.getConfig().getConfigurationSection("settings.dump.toptal");
                     String token = tp != null ? tp.getString("token", "") : "";
                     response = ToptalUtil.post(serverDumpInfo, token);
                     break;
                  case "MCLOGS":
                     response = MclogsUtil.postLogToMclogs(serverDumpInfo);
                     break;
                  case "PASTEBIN":
                     ConfigurationSection pb = this.getConfig().getConfigurationSection("settings.dump.pastebin");
                     String devKey = pb != null ? pb.getString("dev-key", "") : "";
                     String userKey = pb != null ? pb.getString("user-key", "") : "";
                     String privacy = pb != null ? pb.getString("privacy", "UNLISTED") : "UNLISTED";
                     String expire = pb != null ? pb.getString("expire", "1W") : "1W";
                     String title = "GravesX Dump " + System.currentTimeMillis();
                     response = PastebinUtil.post(devKey, userKey, title, serverDumpInfo, privacy, expire);
                     break;
                  default:
                     this.getLogger().warning("Unknown dump method '" + method + "', falling back to local file.");
               }
            } catch (Exception var19) {
               this.getLogger().severe("Failed to dump info. You will need to either switch provider or provide the plugin version when reporting dump info.");
               this.logStackTrace(var19);
            }

            if (response != null && !response.isBlank()) {
               message = response;
               this.getLogger().info("Log uploaded successfully. URL: " + response);
            } else {
               try {
                  File dumpDir = new File(this.getDataFolder(), "dump");
                  if (!dumpDir.exists() && !dumpDir.mkdirs()) {
                     throw new IOException("Could not create dump directory: " + dumpDir.getAbsolutePath());
                  }

                  String name = "gravesx-dump-" + System.currentTimeMillis() + ".txt";
                  File dumpFile = new File(dumpDir, name);

                  try (
                     OutputStream os = new FileOutputStream(dumpFile);
                     Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                  ) {
                     w.write(serverDumpInfo);
                  }

                  message = dumpFile.getAbsolutePath();
               } catch (IOException var18) {
                  this.logStackTrace((Exception)var18);
               }
            }

            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Dumped: " + message);
         });
      }
   }

   public VersionManager getVersionManager() {
      return this.versionManager;
   }

   public IntegrationManager getIntegrationManager() {
      return this.integrationManager;
   }

   public GraveManager getGraveManager() {
      return this.graveManager;
   }

   @Deprecated(
      since = "2026.4.9.1"
   )
   public HologramManager getHologramManager() {
      return this.hologramManager;
   }

   public BlockManager getBlockManager() {
      return this.blockManager;
   }

   public ItemStackManager getItemStackManager() {
      return this.itemStackManager;
   }

   public EntityDataManager getEntityDataManager() {
      return this.entityDataManager;
   }

   public CacheManager getCacheManager() {
      return this.cacheManager;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public ImportManager getImportManager() {
      return this.importManager;
   }

   public GUIManager getGUIManager() {
      return this.guiManager;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public LocationManager getLocationManager() {
      return this.locationManager;
   }

   public SafeLocationManager getSafeLocationManager() {
      return this.safeLocationManager;
   }

   public EntityManager getEntityManager() {
      return this.entityManager;
   }

   public ParticleManager getParticleManager() {
      return this.particleManager;
   }

   public DebugManager getDebugManager() {
      return this.debugManager;
   }

   public PermissionManager getPermissionManager() {
      return this.permissionManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public ChunkManager getChunkManager() {
      return this.chunkManager;
   }

   public SchedulerManager getSchedulerManager() {
      return this.graveScheduler;
   }

   public TextDisplayManager getTextDisplayManager() {
      if (this.textDisplayManager != null) {
         return this.textDisplayManager;
      } else {
         throw new GravesXMethodNotSupportedException("TextDisplay is not supported on " + this.getVersionManager().getVersion() + ".");
      }
   }

   public ArmorStandManager getArmorStandManager() {
      return this.armorStandManager;
   }

   public Compatibility getCompatibility() {
      return this.compatibility;
   }

   @Deprecated(
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GlobalScheduler getGravesXScheduler() {
      this.debugMessage(
         "Method plugin.getGravesXScheduler() is deprecated and set for removal in 2027.4.9.1. We recommend using plugin.getSchedulerManager()", 2
      );
      return this.getSchedulerManager().getScheduler();
   }

   public String getPluginReleaseType() {
      if (this.isPluginDevelopmentBuild()) {
         return "Development Build";
      } else if (this.isPluginOutdatedBuild()) {
         return "Outdated Build";
      } else {
         return this.isPluginUnknownBuild() ? "Unknown Build" : "Production Build";
      }
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public ConfigurationSection getConfig(String config, Grave grave) {
      this.debugMessage(
         "Graves#getConfig(String, Grave) is deprecated and will be removed in 2027.4.9.1. Use Graves#getConfigManager().getConfigSection(String, Grave) instead",
         2
      );
      return this.getConfigManager().getConfigSection(config, grave);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public ConfigurationSection getConfig(String config, List<Grave> graveList) {
      this.debugMessage(
         "Graves#getConfig(String, List<Grave>) is deprecated and will be removed in 4.9.12.1. Use Graves#getConfigManager().getConfigSection(String, List<Grave>) instead",
         2
      );
      return this.getConfigManager().getConfigSection(config, graveList);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public ConfigurationSection getConfig(String config, Entity entity) {
      this.debugMessage(
         "Graves#getConfig(String, Entity) is deprecated and will be removed in 4.9.12.1. Use Graves#getConfigManager().getConfigSection(String, Entity) instead",
         2
      );
      return this.getConfigManager().getConfigSection(config, entity);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public ConfigurationSection getConfig(String config, Entity entity, List<String> permissionList) {
      this.debugMessage(
         "Graves#getConfig(String, Entity, List<String>) is deprecated and will be removed in 4.9.12.1. Use Graves#getConfigManager().getConfigSection(String, Entity, List<String>) instead",
         2
      );
      return this.getConfigManager().getConfigSection(config, entity, permissionList);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public ConfigurationSection getConfig(String config, EntityType entityType, List<String> permissionList) {
      this.debugMessage(
         "Graves#getConfig(String, EntityType, List<String>) is deprecated and will be removed in 4.9.12.1. Use Graves#getConfigManager().getConfigSection(String, EntityType, List<String>) instead",
         2
      );
      return this.getConfigManager().getConfigSection(config, entityType, permissionList);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public List<String> getPermissionList(Entity entity) {
      this.debugMessage(
         "Graves#getPermissionList(Entity) is deprecated and will be removed in 4.9.12.1. Use Graves#getConfigManager().getPermissionList(Entity) instead", 2
      );
      return this.getConfigManager().getPermissionList(entity);
   }

   public final File getPluginsFolder() {
      return this.getConfigManager().getPluginsFolder();
   }

   public String getVersion() {
      return this.getDescription().getVersion();
   }

   public String getLatestVersion() {
      return UpdateUtil.getLatestVersion(this.getSpigotID());
   }

   public final int getSpigotID() {
      return 118271;
   }

   public void logStackTrace(Exception e) {
      this.logStackTrace((Throwable)e);
   }

   public void logStackTrace(Throwable t) {
      Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      this.printThrowable(t, "", seen);
   }

   private void printThrowable(Throwable t, String prefix, Set<Throwable> seen) {
      if (t != null && !seen.contains(t)) {
         seen.add(t);
         this.getLogger().severe(prefix + t);

         for (StackTraceElement e : t.getStackTrace()) {
            this.getLogger().severe("  at " + e);
         }

         Throwable[] suppressed = t.getSuppressed();
         if (suppressed != null) {
            for (Throwable s : suppressed) {
               this.printThrowable(s, "Suppressed: ", seen);
            }
         }

         Throwable cause = t.getCause();
         if (cause != null) {
            this.printThrowable(cause, "Caused by: ", seen);
         }
      }
   }

   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   public void logInvalidGraveSite(String grave_uuid, Location affectedGraveLocation, List<String> invalidationReason) {
      String graveWorld;
      try {
         graveWorld = Objects.requireNonNull(affectedGraveLocation.getWorld()).getName();
      } catch (Exception var8) {
         graveWorld = "Unknown";
      }

      String graveX = String.valueOf(affectedGraveLocation.getBlockX());
      String graveY = String.valueOf(affectedGraveLocation.getBlockY());
      String graveZ = String.valueOf(affectedGraveLocation.getBlockZ());
      this.getLogger()
         .warning(
            "Grave "
               + grave_uuid
               + " at location World: "
               + graveWorld
               + ", x: "
               + graveX
               + ", y: "
               + graveY
               + ", z: "
               + graveZ
               + " has the following missing from grave data: "
               + String.join(", ", invalidationReason)
               + ". This shouldn't affect grave behavior. Do not report this as a bug."
         );
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public boolean hasGrantedPermission(String permission, Player player) {
      this.debugMessage(
         "Graves#hasGrantedPermission(String, Player) is deprecated and will be removed in 4.9.12.1. Use Graves#getPermissionManager().hasGrantedPermission(String, Player) instead",
         2
      );
      return this.getPermissionManager().hasGrantedPermission(permission, player);
   }

   @Deprecated(
      since = "4.9.10.1"
   )
   @ScheduledForRemoval(
      inVersion = "4.9.12.1"
   )
   public boolean hasGrantedPermission(String permission, OfflinePlayer offlinePlayer) {
      this.debugMessage(
         "Graves#hasGrantedPermission(String, OfflinePlayer) is deprecated and will be removed in 4.9.12.1. Use Graves#getPermissionManager().hasGrantedPermission(String, OfflinePlayer) instead",
         2
      );
      return this.getPermissionManager().hasGrantedPermission(permission, offlinePlayer);
   }

   public boolean isPluginDevelopmentBuild() {
      return this.isDevelopmentBuild;
   }

   public boolean isPluginOutdatedBuild() {
      return this.isOutdatedBuild;
   }

   public boolean isPluginUnknownBuild() {
      return this.isUnknownBuild;
   }

   public void setPluginDevelopmentBuild(boolean value) {
      this.isDevelopmentBuild = value;
   }

   public void setPluginOutdatedBuild(boolean value) {
      this.isOutdatedBuild = value;
   }

   public void setPluginUnknownBuild(boolean value) {
      this.isUnknownBuild = value;
   }
}
