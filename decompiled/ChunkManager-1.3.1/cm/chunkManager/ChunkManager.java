package cm.chunkManager;

import cm.chunkManager.api.ChunkManagerAPI;
import cm.chunkManager.components.ChunkAnalyzer;
import cm.chunkManager.components.ChunkCache;
import cm.chunkManager.components.ChunkCostTracker;
import cm.chunkManager.components.ChunkFreezer;
import cm.chunkManager.components.ChunkPreGen;
import cm.chunkManager.components.ChunkPrefetcher;
import cm.chunkManager.components.ChunkProcessor;
import cm.chunkManager.components.ChunkTicketManager;
import cm.chunkManager.components.ChunkVisualizer;
import cm.chunkManager.components.ConfigurationManager;
import cm.chunkManager.components.EntityLimiter;
import cm.chunkManager.components.LanguageManager;
import cm.chunkManager.components.LoadPriorityQueue;
import cm.chunkManager.components.OptScheduler;
import cm.chunkManager.components.PerformanceMonitor;
import cm.chunkManager.components.PlayerAdaptiveLoader;
import cm.chunkManager.components.PortalPreloader;
import cm.chunkManager.components.RegionOptimizer;
import cm.chunkManager.components.RegionTickMonitor;
import cm.chunkManager.components.SmartChunkUnloader;
import cm.chunkManager.components.TaskManager;
import cm.chunkManager.components.TeleportPreloader;
import cm.chunkManager.components.TpsThrottler;
import cm.chunkManager.components.WorldSettingsManager;
import cm.chunkManager.events.ChunkEventManager;
import cm.chunkManager.gui.GuiManager;
import cm.chunkManager.gui.TopChunksMenu;
import cm.chunkManager.hooks.MapOverlayManager;
import cm.chunkManager.hooks.WgBridge;
import cm.chunkManager.metrics.MetricsCollector;
import cm.chunkManager.shaded.bstats.bukkit.Metrics;
import cm.chunkManager.utils.ChunkUtils;
import cm.chunkManager.utils.MemoryManager;
import cm.chunkManager.utils.Scheduler;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkManager extends JavaPlugin implements Listener, TabCompleter, ChunkManagerAPI {
   private TaskManager taskManager;
   private ChunkCache chunkCache;
   private ConfigurationManager configManager;
   private PerformanceMonitor performanceMonitor;
   private MemoryManager memoryManager;
   private ChunkProcessor chunkProcessor;
   private SmartChunkUnloader smartUnloader;
   private LanguageManager languageManager;
   private ChunkAnalyzer chunkAnalyzer;
   private RegionOptimizer regionOptimizer;
   private ChunkPrefetcher chunkPrefetcher;
   private ChunkEventManager eventManager;
   private MetricsCollector metricsCollector;
   private GuiManager guiManager;
   private WorldSettingsManager worldSettingsManager;
   private ChunkFreezer chunkFreezer;
   private ChunkVisualizer chunkVisualizer;
   private OptScheduler optScheduler;
   private ChunkPreGen chunkPreGen;
   private TpsThrottler tpsThrottler;
   private EntityLimiter entityLimiter;
   private LoadPriorityQueue loadPriorityQueue;
   private ChunkTicketManager ticketManager;
   private TeleportPreloader teleportPreloader;
   private PortalPreloader portalPreloader;
   private PlayerAdaptiveLoader adaptiveLoader;
   private ChunkCostTracker costTracker;
   private RegionTickMonitor regionTickMonitor;
   private MapOverlayManager mapOverlay;
   private final DecimalFormat df = new DecimalFormat("#.##");
   private final Map<UUID, Integer> playerRadiusOverrides = new ConcurrentHashMap<>();
   private final Map<UUID, Long> lastPlayerActivity = new ConcurrentHashMap<>();
   private boolean maintenanceMode;
   private boolean autoMaintenanceActive;
   private boolean manualOverride;
   private boolean advancedFeaturesEnabled;

   public void onEnable() {
      this.initializeComponents();
      this.getServer().getPluginManager().registerEvents(this, this);
      this.registerCommands();
      this.startTasks();
      this.setupEventListeners();
      this.guiManager = new GuiManager(this);
      this.getServer().getPluginManager().registerEvents(this.guiManager, this);
      new Metrics(this, 22717);
      this.getLogger().info("ChunkManager v" + this.getDescription().getVersion() + " enabled");
      this.getLogger().info("Loaded " + this.languageManager.getAvailableLanguages().size() + " languages");
      this.getLogger().info("Advanced features: " + (this.advancedFeaturesEnabled ? "ENABLED" : "DISABLED"));
   }

   private void initializeComponents() {
      this.taskManager = new TaskManager(this);
      this.configManager = new ConfigurationManager(this);
      this.languageManager = new LanguageManager(this);
      this.chunkCache = new ChunkCache(this, this.configManager.getMaxCacheSize(), this.configManager.getCacheExpirationTime());
      this.performanceMonitor = new PerformanceMonitor(this);
      this.memoryManager = new MemoryManager(this);
      int coreThreads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
      int maxThreads = Math.max(4, Runtime.getRuntime().availableProcessors());
      this.chunkProcessor = new ChunkProcessor(this, coreThreads, maxThreads);
      this.smartUnloader = new SmartChunkUnloader(this, this.chunkProcessor);
      this.worldSettingsManager = new WorldSettingsManager(this);
      this.advancedFeaturesEnabled = this.getConfig().getBoolean("advanced-features.enabled", true);
      if (this.advancedFeaturesEnabled) {
         this.chunkAnalyzer = new ChunkAnalyzer(this);
         this.regionOptimizer = new RegionOptimizer(this);
         this.chunkPrefetcher = new ChunkPrefetcher(this);
         this.eventManager = new ChunkEventManager(this);
         this.metricsCollector = new MetricsCollector(this);
         boolean exportMetrics = this.getConfig().getBoolean("performance.export-metrics", false);
         int metricsInterval = this.getConfig().getInt("performance.metrics-interval", 300);
         this.metricsCollector.startExporting(exportMetrics, metricsInterval);
      }

      this.maintenanceMode = this.configManager.isMaintenanceModeEnabled();
      this.manualOverride = !this.getConfig().getBoolean("maintenance.auto-maintenance", true);
      if (this.maintenanceMode) {
         this.configManager.setDynamicRadius(this.configManager.getMaintenanceModeRadius());
         this.autoMaintenanceActive = !this.manualOverride;
      }

      this.performanceMonitor.setPerformanceHandler(this::handlePerformanceChange);
      this.chunkFreezer = new ChunkFreezer(this);
      this.chunkVisualizer = new ChunkVisualizer(this);
      this.optScheduler = new OptScheduler(this);
      this.chunkPreGen = new ChunkPreGen(this);
      this.tpsThrottler = new TpsThrottler(this, this.performanceMonitor, this.configManager.getTpsThrottleMin(), this.configManager.getTpsThrottleTarget());
      this.entityLimiter = new EntityLimiter(
         this,
         this.configManager.getEntityLimitTotal(),
         this.configManager.getEntityLimitHostile(),
         this.configManager.getEntityLimitPassive(),
         this.configManager.getEntityLimitItems()
      );
      this.entityLimiter.setEnabled(this.configManager.isEntityLimiterEnabled());
      this.loadPriorityQueue = new LoadPriorityQueue(this);
      this.ticketManager = new ChunkTicketManager(this);
      if (this.advancedFeaturesEnabled && this.chunkPrefetcher != null) {
         this.teleportPreloader = new TeleportPreloader(this, this.chunkPrefetcher, this.configManager.getTeleportPreloadRadius());
         this.teleportPreloader.setEnabled(this.configManager.isTeleportPreloadEnabled());
         this.portalPreloader = new PortalPreloader(this, this.chunkPrefetcher, this.configManager.getPortalPreloadRadius());
         this.portalPreloader.setEnabled(this.configManager.isPortalPreloadEnabled());
         this.getServer().getPluginManager().registerEvents(this.portalPreloader, this);
      }

      this.adaptiveLoader = new PlayerAdaptiveLoader(
         this,
         this.configManager.getPingThresholdLow(),
         this.configManager.getPingThresholdMed(),
         this.configManager.getPingThresholdHigh(),
         this.configManager.getGeyserRadiusReduction()
      );
      this.adaptiveLoader.setEnabled(this.configManager.isAdaptiveLoadingEnabled());
      this.costTracker = new ChunkCostTracker(this);
      this.costTracker.setEnabled(this.configManager.isChunkCostEnabled());
      this.regionTickMonitor = new RegionTickMonitor(this);
      this.regionTickMonitor.setEnabled(this.configManager.isRegionTickMonitorEnabled());
      this.mapOverlay = new MapOverlayManager(this, this.costTracker, this.configManager.isMapOverlayEnabled(), this.configManager.getMapOverlayTopCount());
      if (WgBridge.isAvailable() && this.configManager.isWgKeepRegionsLoaded()) {
         this.startWgRegionScan();
      }
   }

   private void startWgRegionScan() {
      int interval = this.configManager.getWgScanInterval() * 20;
      Scheduler.runGlobalTimer(this, () -> {
         if (this.configManager.isWgKeepRegionsLoaded()) {
            for (World world : Bukkit.getWorlds()) {
               if (!this.isWorldBlacklisted(world)) {
                  for (long packed : WgBridge.getRegionChunks(world, this.configManager.getWgRegionWhitelist())) {
                     ChunkUtils.getChunkAtAsync(world, (int)(packed >> 32), (int)packed, this);
                  }
               }
            }
         }
      }, 200L, interval);
   }

   private void registerCommands() {
      String[] commands = new String[]{
         "chunkstatus",
         "chunkreload",
         "optimizechunks",
         "chunkgc",
         "chunksummary",
         "maintenance",
         "chunklang",
         "chunkanalyze",
         "chunkregions",
         "chunkmetrics",
         "cm",
         "cmfreeze",
         "cmviz",
         "cmpregen",
         "cmtop",
         "cmrtps",
         "cmmap"
      };

      for (String cmdName : commands) {
         PluginCommand cmd = this.getCommand(cmdName);
         if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
         }
      }
   }

   private void startTasks() {
      this.taskManager.startTask("chunkLoader", this::processChunkQueue, 1L, 1L);
      this.taskManager.startTask("smartUnloader", this::performSmartUnload, 200L, 200L);
      this.taskManager.startTask("cacheCleaner", () -> this.chunkCache.cleanExpiredCache(), 600L, 600L);
      this.taskManager.startTask("tickUpdater", () -> this.performanceMonitor.updateTickTime(), 1L, 1L);
      this.taskManager.startTask("performanceMonitor", this::monitorPerformance, 200L, 200L);
      this.taskManager.startAsyncTask("memoryMonitor", () -> this.memoryManager.checkMemoryStatus(), 400L, 400L);
      if (this.advancedFeaturesEnabled) {
         this.taskManager.startTask("regionDecay", () -> this.regionOptimizer.decayActivityScores(), 1200L, 1200L);
         this.taskManager.startTask("analyzerCleanup", () -> this.chunkAnalyzer.cleanExpiredCache(), 6000L, 6000L);
         this.taskManager.startTask("metricsUpdate", this::updateMetrics, 100L, 100L);
      }

      if (this.configManager.isChunkCostEnabled()) {
         this.taskManager
            .startTask(
               "costTracker",
               () -> this.costTracker.updateAll(),
               this.configManager.getChunkCostUpdateInterval(),
               this.configManager.getChunkCostUpdateInterval()
            );
      }

      if (this.configManager.isRegionTickMonitorEnabled()) {
         this.taskManager
            .startTask(
               "regionTickMonitor",
               () -> this.regionTickMonitor.tick(),
               this.configManager.getRegionTickUpdateInterval(),
               this.configManager.getRegionTickUpdateInterval()
            );
      }

      if (this.configManager.isMapOverlayEnabled()) {
         this.taskManager
            .startTask(
               "mapOverlay",
               () -> this.mapOverlay.refresh(),
               this.configManager.getMapOverlayRefreshInterval(),
               this.configManager.getMapOverlayRefreshInterval()
            );
      }

      this.taskManager.startTask("ticketCleanup", () -> {
         if (this.ticketManager != null) {
            this.ticketManager.cleanup();
         }
      }, 200L, 200L);
   }

   public void onDisable() {
      this.taskManager.cancelAllTasks();
      this.chunkCache.clearAll();
      this.memoryManager.cleanup();
      this.chunkProcessor.shutdown();
      this.smartUnloader.cleanup();
      if (this.chunkVisualizer != null) {
         this.chunkVisualizer.shutdown();
      }

      if (this.optScheduler != null) {
         this.optScheduler.shutdown();
      }

      if (this.chunkPreGen != null) {
         this.chunkPreGen.shutdown();
      }

      if (this.ticketManager != null) {
         this.ticketManager.shutdown();
      }

      if (this.costTracker != null) {
         this.costTracker.cleanup();
      }

      if (this.regionTickMonitor != null) {
         this.regionTickMonitor.stop();
      }

      if (this.mapOverlay != null) {
         this.mapOverlay.shutdown();
      }

      if (this.adaptiveLoader != null) {
         this.adaptiveLoader = null;
      }

      if (this.guiManager != null) {
         this.guiManager.shutdown();
      }

      if (this.advancedFeaturesEnabled) {
         this.regionOptimizer.cleanup();
         this.chunkPrefetcher.shutdown();
         this.chunkAnalyzer.clearCache();
         this.metricsCollector.shutdown();
      }

      this.getLogger().info("ChunkManager disabled");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      String cmdName = command.getName().toLowerCase(Locale.ROOT);
      if (cmdName.equals("chunklang")) {
         if (this.requireAdmin(sender)) {
            this.handleLanguageCommand(sender, args);
         }

         return true;
      } else if (!this.requireAdmin(sender)) {
         return true;
      } else {
         switch (cmdName) {
            case "chunkstatus":
               this.displayChunkInfo(sender);
               break;
            case "chunkreload":
               this.reloadConfigValues(sender);
               break;
            case "optimizechunks":
               this.optimizeChunks(sender);
               break;
            case "chunkgc":
               this.runChunkGC(sender);
               break;
            case "chunksummary":
               this.generateChunkSummary(sender);
               break;
            case "maintenance":
               this.handleMaintenanceCommand(sender, args);
               break;
            case "chunkanalyze":
               if (this.advancedFeaturesEnabled) {
                  this.analyzeChunks(sender, args);
               }
               break;
            case "chunkregions":
               if (this.advancedFeaturesEnabled) {
                  this.displayRegionInfo(sender);
               }
               break;
            case "chunkmetrics":
               if (this.advancedFeaturesEnabled) {
                  this.displayMetrics(sender);
               }
               break;
            case "cm":
               if (sender instanceof Player p) {
                  this.guiManager.openMainMenu(p);
               } else {
                  sender.sendMessage(this.languageManager.getMessage("player-only-gui"));
               }
               break;
            case "cmfreeze":
               this.handleFreezeCommand(sender, args);
               break;
            case "cmviz":
               if (sender instanceof Player p) {
                  boolean on = this.chunkVisualizer.toggle(p);
                  p.sendMessage(this.languageManager.getMessage(on ? "viz-enabled" : "viz-disabled"));
               } else {
                  sender.sendMessage(this.languageManager.getMessage("player-only"));
               }
               break;
            case "cmpregen":
               this.handlePreGenCommand(sender, args);
               break;
            case "cmtop":
               if (sender instanceof Player p) {
                  this.guiManager.openMenu(p, new TopChunksMenu(this, p, this.guiManager));
               } else {
                  this.displayTopChunksConsole(sender);
               }
               break;
            case "cmrtps":
               this.displayRegionTps(sender);
               break;
            case "cmmap":
               this.handleMapCommand(sender, args);
               break;
            default:
               return false;
         }

         return true;
      }
   }

   private void handleLanguageCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(this.languageManager.getMessage("current-lang", Collections.singletonMap("lang", this.languageManager.getCurrentLanguageName())));
         sender.sendMessage(this.languageManager.getMessage("available-langs"));
         this.languageManager
            .getAvailableLanguages()
            .forEach((code, name) -> sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + code + ChatColor.GRAY + " (" + name + ")"));
         sender.sendMessage(this.languageManager.getMessage("usage-lang"));
      } else {
         String langCode = args[0].toLowerCase();
         if (this.languageManager.getAvailableLanguages().containsKey(langCode)) {
            this.languageManager.setLanguage(langCode);
            this.getConfig().set("language", langCode);
            this.saveConfig();
            sender.sendMessage(this.languageManager.getMessage("lang-changed", Collections.singletonMap("lang", this.languageManager.getCurrentLanguageName())));
         } else {
            sender.sendMessage(this.languageManager.getMessage("invalid-lang", Collections.singletonMap("code", langCode)));
         }
      }
   }

   private boolean requireAdmin(CommandSender sender) {
      if (!sender.hasPermission("chunkmanager.admin")) {
         sender.sendMessage(ChatColor.RED + this.languageManager.getMessage("no-permission"));
         return false;
      } else {
         return true;
      }
   }

   private void handleMaintenanceCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         this.showMaintenanceStatus(sender);
      } else {
         String var3 = args[0].toLowerCase(Locale.ROOT);
         switch (var3) {
            case "on":
               this.setMaintenanceMode(true, sender, false);
               break;
            case "off":
               this.setMaintenanceMode(false, sender, false);
               break;
            case "status":
               this.showMaintenanceStatus(sender);
               break;
            case "auto":
               this.toggleAutoMaintenance(sender);
               break;
            default:
               sender.sendMessage(this.languageManager.getMessage("usage-maintenance"));
         }
      }
   }

   private void handleFreezeCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(this.languageManager.getMessage("usage-freeze"));
      } else {
         String var3 = args[0].toLowerCase(Locale.ROOT);
         switch (var3) {
            case "add":
               if (!(sender instanceof Player p)) {
                  sender.sendMessage(this.languageManager.getMessage("player-only"));
                  return;
               }

               int var14 = p.getLocation().getBlockX() >> 4;
               int var16 = p.getLocation().getBlockZ() >> 4;
               String var18 = p.getWorld().getName();
               Map<String, String> ph = new HashMap<>();
               ph.put("x", String.valueOf(var14));
               ph.put("z", String.valueOf(var16));
               ph.put("world", var18);
               if (this.chunkFreezer.freeze(var18, var14, var16)) {
                  sender.sendMessage(this.languageManager.getMessage("chunk-frozen", ph));
               } else {
                  sender.sendMessage(this.languageManager.getMessage("chunk-already-frozen", ph));
               }
               break;
            case "remove":
               if (!(sender instanceof Player p)) {
                  sender.sendMessage(this.languageManager.getMessage("player-only"));
                  return;
               }

               int var13 = p.getLocation().getBlockX() >> 4;
               int var15 = p.getLocation().getBlockZ() >> 4;
               String var17 = p.getWorld().getName();
               Map<String, String> ph = new HashMap<>();
               ph.put("x", String.valueOf(var13));
               ph.put("z", String.valueOf(var15));
               ph.put("world", var17);
               if (this.chunkFreezer.unfreeze(var17, var13, var15)) {
                  sender.sendMessage(this.languageManager.getMessage("chunk-unfrozen", ph));
               } else {
                  sender.sendMessage(this.languageManager.getMessage("chunk-not-frozen", ph));
               }
               break;
            case "list":
               Set<String> frozen = this.chunkFreezer.getFrozenChunks();
               if (frozen.isEmpty()) {
                  sender.sendMessage(this.languageManager.getMessage("frozen-list-empty"));
               } else {
                  sender.sendMessage(this.languageManager.getMessage("frozen-list-header", Collections.singletonMap("count", String.valueOf(frozen.size()))));

                  for (String key : frozen) {
                     String[] parts = key.split(";");
                     if (parts.length == 3) {
                        sender.sendMessage(ChatColor.GRAY + "  " + parts[0] + " [" + parts[1] + ", " + parts[2] + "]");
                     }
                  }
               }
               break;
            case "clear":
               int count = this.chunkFreezer.clear();
               sender.sendMessage(this.languageManager.getMessage("frozen-cleared", Collections.singletonMap("count", String.valueOf(count))));
               break;
            default:
               sender.sendMessage(this.languageManager.getMessage("usage-freeze"));
         }
      }
   }

   private void handlePreGenCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(this.languageManager.getMessage("usage-pregen"));
      } else {
         String var3 = args[0].toLowerCase(Locale.ROOT);
         switch (var3) {
            case "start":
               if (this.chunkPreGen.isActive()) {
                  sender.sendMessage(this.languageManager.getMessage("pregen-already-running"));
                  return;
               }

               if (args.length >= 3) {
                  World world = Bukkit.getWorld(args[1]);
                  if (world == null) {
                     sender.sendMessage(this.languageManager.getMessage("invalid-world", Collections.singletonMap("world", args[1])));
                     return;
                  }

                  int radius;
                  try {
                     radius = Integer.parseInt(args[2]);
                  } catch (NumberFormatException var11) {
                     sender.sendMessage(this.languageManager.getMessage("invalid-radius", Collections.singletonMap("value", args[2])));
                     return;
                  }

                  if (radius < 1 || radius > 500) {
                     sender.sendMessage(this.languageManager.getMessage("radius-range"));
                     return;
                  }

                  int cx = world.getSpawnLocation().getBlockX() >> 4;
                  int cz = world.getSpawnLocation().getBlockZ() >> 4;
                  if (sender instanceof Player p) {
                     this.chunkPreGen.start(p, world, cx, cz, radius);
                  } else {
                     sender.sendMessage(this.languageManager.getMessage("player-only-pregen"));
                  }
               } else if (args.length == 2) {
                  if (!(sender instanceof Player p)) {
                     sender.sendMessage(this.languageManager.getMessage("console-pregen-usage"));
                     return;
                  }

                  int radiusx;
                  try {
                     radiusx = Integer.parseInt(args[1]);
                  } catch (NumberFormatException var10) {
                     sender.sendMessage(this.languageManager.getMessage("invalid-radius", Collections.singletonMap("value", args[1])));
                     return;
                  }

                  if (radiusx < 1 || radiusx > 500) {
                     sender.sendMessage(this.languageManager.getMessage("radius-range"));
                     return;
                  }

                  int cx = p.getLocation().getBlockX() >> 4;
                  int cz = p.getLocation().getBlockZ() >> 4;
                  this.chunkPreGen.start(p, p.getWorld(), cx, cz, radiusx);
               } else {
                  sender.sendMessage(this.languageManager.getMessage("usage-pregen-start"));
               }
               break;
            case "stop":
               if (!this.chunkPreGen.isActive()) {
                  sender.sendMessage(this.languageManager.getMessage("pregen-not-running"));
                  return;
               }

               this.chunkPreGen.stop();
               break;
            case "status":
               if (!this.chunkPreGen.isActive()) {
                  sender.sendMessage(this.languageManager.getMessage("pregen-not-running"));
                  return;
               }

               Map<String, String> ph = this.chunkPreGen.getProgress();
               sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "PreGen Status " + ChatColor.GRAY + "---");
               sender.sendMessage(this.languageManager.getMessage("pregen-progress", ph));
               sender.sendMessage(this.languageManager.getMessage("skipped", Collections.singletonMap("count", ph.get("skipped"))));
               sender.sendMessage(ChatColor.GRAY + "---------------------------");
               break;
            default:
               sender.sendMessage(this.languageManager.getMessage("usage-pregen"));
         }
      }
   }

   private void toggleAutoMaintenance(CommandSender sender) {
      this.manualOverride = !this.manualOverride;
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("state", this.manualOverride ? "disabled" : "enabled");
      sender.sendMessage(ChatColor.YELLOW + this.languageManager.getMessage("auto-maintenance-toggle", placeholders));
      this.configManager.saveAutoMaintenance(!this.manualOverride);
   }

   private void setMaintenanceMode(boolean enable, CommandSender sender, boolean autoMode) {
      if (!autoMode || !this.manualOverride) {
         if (this.maintenanceMode == enable && sender != null) {
            this.showMaintenanceStatus(sender);
         } else {
            this.maintenanceMode = enable;
            if (enable) {
               this.configManager.setDynamicRadius(this.configManager.getMaintenanceModeRadius());
               this.autoMaintenanceActive = autoMode;
               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("radius", String.valueOf(this.configManager.getDynamicRadius()));
               if (sender != null) {
                  this.sendColoredMessage(sender, this.languageManager.getMessage("maintenance-mode-enabled", placeholders));
               }
            } else {
               this.configManager.setDynamicRadius(this.configManager.getDefaultRadius());
               this.autoMaintenanceActive = false;
               if (!autoMode) {
                  this.manualOverride = true;
               }

               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("radius", String.valueOf(this.configManager.getDynamicRadius()));
               if (sender != null) {
                  this.sendColoredMessage(sender, this.languageManager.getMessage("maintenance-mode-disabled", placeholders));
               }
            }

            this.configManager.saveMaintenanceMode(enable);
         }
      }
   }

   private void sendColoredMessage(CommandSender sender, String message) {
      if (sender == Bukkit.getConsoleSender()) {
         this.getLogger().info(ChatColor.stripColor(message));
      } else {
         sender.sendMessage(message);
         if (this.configManager.shouldBroadcastMaintenanceChanges()) {
            Bukkit.getServer().broadcastMessage(message);
         }
      }
   }

   private void showMaintenanceStatus(CommandSender sender) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("status", this.maintenanceMode ? "ENABLED" : "DISABLED");
      placeholders.put("radius", String.valueOf(this.configManager.getDynamicRadius()));
      placeholders.put("auto", this.manualOverride ? "DISABLED" : "ENABLED");
      sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "Maintenance Status " + ChatColor.GRAY + "---");
      sender.sendMessage(ChatColor.YELLOW + this.languageManager.getMessage("maintenance-mode-status", placeholders));
      sender.sendMessage(ChatColor.YELLOW + this.languageManager.getMessage("auto-maintenance-status", placeholders));
      sender.sendMessage(ChatColor.GRAY + "---------------------------");
   }

   private void reloadConfigValues(CommandSender sender) {
      boolean previousMaintenanceMode = this.maintenanceMode;
      boolean previousManualOverride = this.manualOverride;
      this.configManager.reload();
      this.languageManager.reloadLanguages();
      this.worldSettingsManager.reload();
      this.chunkCache = new ChunkCache(this, this.configManager.getMaxCacheSize(), this.configManager.getCacheExpirationTime());
      this.smartUnloader.reloadConfig();
      if (previousManualOverride) {
         this.maintenanceMode = previousMaintenanceMode;
         this.manualOverride = previousManualOverride;
         if (this.maintenanceMode) {
            this.configManager.setDynamicRadius(this.configManager.getMaintenanceModeRadius());
         }
      }

      sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("config-reloaded"));
   }

   private void displayChunkInfo(CommandSender sender) {
      Map<String, String> info = this.performanceMonitor.getPerformanceInfo();
      MemoryManager.MemoryStats memStats = this.memoryManager.getMemoryStats();
      Map<String, Object> heatStats = this.smartUnloader.getHeatMapStats();
      double tickTime = this.performanceMonitor.getAverageTickTime();
      double tps = tickTime > 50.0 ? Math.max(0.0, 20.0 - (tickTime - 50.0) / 50.0) : 20.0;
      sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "ChunkManager v" + this.getDescription().getVersion() + " Status " + ChatColor.GRAY + "---");
      ChatColor tpsColor = tps >= 19.0 ? ChatColor.GREEN : (tps >= 17.0 ? ChatColor.YELLOW : (tps >= 15.0 ? ChatColor.GOLD : ChatColor.RED));
      sender.sendMessage(
         ChatColor.AQUA + "Server TPS: " + tpsColor + this.df.format(tps) + "/20.0 " + ChatColor.GRAY + "(" + this.df.format(tickTime) + "ms tick)"
      );
      sender.sendMessage(
         ChatColor.AQUA + this.languageManager.getMessage("chunks-in-queue", Collections.singletonMap("size", String.valueOf(this.chunkCache.getQueueSize())))
      );
      sender.sendMessage(
         ChatColor.AQUA
            + this.languageManager.getMessage("chunks-kept-loaded", Collections.singletonMap("size", String.valueOf(this.chunkCache.getKeepLoadedSize())))
      );
      sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("online-players", Collections.singletonMap("count", info.get("online_players"))));
      sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("total-loaded-chunks", Collections.singletonMap("count", info.get("total_chunks"))));
      ChatColor memColor = memStats.usagePercentage > 80.0 ? ChatColor.RED : (memStats.usagePercentage > 60.0 ? ChatColor.YELLOW : ChatColor.GREEN);
      sender.sendMessage(
         ChatColor.AQUA
            + "Memory: "
            + memColor
            + this.df.format(memStats.usedMB)
            + "/"
            + this.df.format(memStats.maxMB)
            + " MB "
            + ChatColor.GRAY
            + "("
            + this.df.format(memStats.usagePercentage)
            + "%)"
      );
      sender.sendMessage(
         ChatColor.AQUA
            + this.languageManager.getMessage("dynamic-radius", Collections.singletonMap("radius", String.valueOf(this.configManager.getDynamicRadius())))
      );
      if (this.maintenanceMode) {
         sender.sendMessage(this.languageManager.getMessage("maintenance-active"));
      }

      sender.sendMessage(
         ChatColor.AQUA
            + "Heat Map: "
            + ChatColor.WHITE
            + "Hot: "
            + heatStats.get("hotChunks")
            + ", Cold: "
            + heatStats.get("coldChunks")
            + ", Total: "
            + heatStats.get("totalChunks")
      );
      sender.sendMessage(
         ChatColor.AQUA
            + "Processor Queue: "
            + ChatColor.WHITE
            + this.chunkProcessor.getQueueSize()
            + " (Active Threads: "
            + this.chunkProcessor.getActiveThreads()
            + ")"
      );
      if (this.advancedFeaturesEnabled) {
         sender.sendMessage(this.languageManager.getMessage("advanced-enabled"));
      }

      sender.sendMessage(ChatColor.GRAY + "---------------------------");
   }

   private void optimizeChunks(CommandSender sender) {
      sender.sendMessage(this.languageManager.getMessage("optimizing"));
      this.taskManager.runAsyncTask("optimize", () -> {
         int totalUnloaded = 0;

         for (World world : Bukkit.getWorlds()) {
            if (!this.isWorldBlacklisted(world)) {
               totalUnloaded += this.smartUnloader.performSmartUnload(world, 50);
            }
         }

         PerformanceMonitor.PerformanceData perfData = this.performanceMonitor.getCurrentPerformance();
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("unloaded_chunks", String.valueOf(totalUnloaded));
         placeholders.put("memory_usage", this.df.format(perfData.memoryUsage));
         placeholders.put("average_tick_time", this.df.format(perfData.tickTime));
         Scheduler.runGlobal(this, () -> {
            sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "Optimization Results " + ChatColor.GRAY + "---");
            sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("unloaded-chunks", placeholders));
            sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("current-memory-usage", placeholders));
            sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("current-average-tick-time", placeholders));
            sender.sendMessage(ChatColor.GRAY + "---------------------------");
         });
      });
   }

   private void runChunkGC(CommandSender sender) {
      sender.sendMessage(this.languageManager.getMessage("cleaning-memory"));
      this.performanceMonitor.runGarbageCollection(results -> {
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("before_gc", this.df.format(results.get("before")));
         placeholders.put("after_gc", this.df.format(results.get("after")));
         placeholders.put("freed_memory", this.df.format(results.get("freed")));
         sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "Memory Cleanup Results " + ChatColor.GRAY + "---");
         sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("memory-before-gc", placeholders));
         sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("memory-after-gc", placeholders));
         sender.sendMessage(ChatColor.AQUA + this.languageManager.getMessage("freed-memory", placeholders));
         sender.sendMessage(ChatColor.GRAY + "---------------------------");
      });
   }

   private void generateChunkSummary(CommandSender sender) {
      sender.sendMessage(this.languageManager.getMessage("generating-summary"));
      Scheduler.runGlobal(this, () -> {
         Map<World, PerformanceMonitor.WorldStatistics> worldStats = new HashMap<>();

         for (World world : Bukkit.getWorlds()) {
            if (!this.isWorldBlacklisted(world)) {
               worldStats.put(world, this.performanceMonitor.gatherWorldStatistics(world));
            }
         }

         Scheduler.runAsync(this, () -> {
            StringBuilder summary = new StringBuilder();
            summary.append(ChatColor.GRAY).append("--- ").append(ChatColor.AQUA).append("World Analysis").append(ChatColor.GRAY).append(" ---\n");

            for (World worldx : Bukkit.getWorlds()) {
               if (this.isWorldBlacklisted(worldx)) {
                  summary.append(ChatColor.RED).append("World '").append(worldx.getName()).append("' is blacklisted\n\n");
               } else if (worldStats.containsKey(worldx)) {
                  this.appendWorldSummary(summary, worldx, worldStats.get(worldx));
               }
            }

            summary.append(ChatColor.GRAY).append("---------------------------");
            Scheduler.runGlobal(this, () -> sender.sendMessage(summary.toString()));
         });
      });
   }

   private void appendWorldSummary(StringBuilder summary, World world, PerformanceMonitor.WorldStatistics stats) {
      summary.append(ChatColor.AQUA).append(this.languageManager.getMessage("world-name", Collections.singletonMap("world", world.getName()))).append("\n");
      summary.append(ChatColor.AQUA)
         .append(this.languageManager.getMessage("loaded-chunks", Collections.singletonMap("count", String.valueOf(world.getLoadedChunks().length))))
         .append("\n");
      Map<String, String> placeholders = stats.toPlaceholderMap();
      summary.append(ChatColor.AQUA).append(this.languageManager.getMessage("active-chunks", placeholders)).append("\n");
      summary.append(ChatColor.AQUA).append(this.languageManager.getMessage("total-entities", placeholders)).append("\n");
      summary.append(ChatColor.AQUA).append(this.languageManager.getMessage("total-tile-entities", placeholders)).append("\n");
      summary.append(ChatColor.AQUA).append(this.languageManager.getMessage("total-players", placeholders)).append("\n\n");
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getTo() != null) {
         if (event.getTo().getWorld() != null) {
            int fromCX = event.getFrom().getBlockX() >> 4;
            int fromCZ = event.getFrom().getBlockZ() >> 4;
            int toCX = event.getTo().getBlockX() >> 4;
            int toCZ = event.getTo().getBlockZ() >> 4;
            if (fromCX != toCX || fromCZ != toCZ) {
               Player player = event.getPlayer();
               World world = event.getTo().getWorld();
               if (!this.isWorldBlacklisted(world)) {
                  this.preloadChunksAround(event.getTo(), player);
                  this.lastPlayerActivity.put(player.getUniqueId(), System.currentTimeMillis());
                  if (this.adaptiveLoader != null && this.adaptiveLoader.isEnabled()) {
                     this.adaptiveLoader.updatePlayer(player);
                  }

                  if (this.advancedFeaturesEnabled) {
                     this.regionOptimizer.onPlayerMove(player, event.getFrom(), event.getTo());
                     this.chunkPrefetcher.trackPlayerMovement(player);
                     this.metricsCollector.recordMetric("player_movements", 1L);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.playerRadiusOverrides.remove(player.getUniqueId());
      this.lastPlayerActivity.remove(player.getUniqueId());
      this.smartUnloader.onPlayerQuit(player);
      this.chunkVisualizer.remove(player.getUniqueId());
      if (this.adaptiveLoader != null) {
         this.adaptiveLoader.onPlayerQuit(player.getUniqueId());
      }

      if (this.teleportPreloader != null) {
         this.teleportPreloader.onPlayerQuit(player.getUniqueId());
      }

      if (this.advancedFeaturesEnabled) {
         this.regionOptimizer.onPlayerQuit(player);
         this.chunkPrefetcher.onPlayerQuit(player);
      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      Chunk chunk = event.getChunk();
      if (!this.isWorldBlacklisted(chunk.getWorld())) {
         this.smartUnloader.onChunkLoad(chunk);
         if (this.entityLimiter != null && this.entityLimiter.isEnabled()) {
            this.entityLimiter.enforceLimit(chunk);
         }

         if (this.advancedFeaturesEnabled) {
            this.eventManager.fireLoadEvent(chunk, event.isNewChunk(), null);
            this.metricsCollector.recordMetric("chunks_loaded", 1L);
         }
      }
   }

   @EventHandler
   public void onChunkUnload(ChunkUnloadEvent event) {
      Chunk c = event.getChunk();
      if (!this.isWorldBlacklisted(c.getWorld())) {
         if (this.chunkCache.shouldKeepLoaded(c)) {
            this.chunkCache.removeFromKeepLoaded(c);
            this.chunkCache.addToQueue(c);
         } else if (!this.chunkCache.isCached(c)) {
            this.chunkCache.addToCache(c);
         }

         if (this.advancedFeaturesEnabled) {
            this.eventManager.fireUnloadEvent(c, ChunkEventManager.ChunkUnloadEvent.UnloadReason.AUTOMATIC);
            this.metricsCollector.recordMetric("chunks_unloaded", 1L);
         }
      }
   }

   private void preloadChunksAround(Location location, Player player) {
      World world = location.getWorld();
      if (world != null) {
         int radius = this.getEffectiveRadius(player);
         int centerX = location.getBlockX() >> 4;
         int centerZ = location.getBlockZ() >> 4;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               int chunkX = centerX + x;
               int chunkZ = centerZ + z;
               ChunkUtils.getChunkAtAsync(world, chunkX, chunkZ, this).thenAccept(chunk -> this.smartUnloader.onChunkAccess(chunk));
            }
         }
      }
   }

   private int getEffectiveRadius(Player player) {
      if (this.memoryManager.isLowMemoryMode()) {
         return Math.max(1, this.configManager.getDynamicRadius() - 1);
      } else {
         Integer override = this.playerRadiusOverrides.get(player.getUniqueId());
         if (override != null) {
            return override;
         } else {
            if (this.configManager.isPermissionRadiusEnabled()) {
               for (Entry<String, Integer> tier : this.configManager.getPermissionRadiusTiers()) {
                  if (player.hasPermission("chunkmanager.radius." + tier.getKey())) {
                     int permRadius = tier.getValue();
                     if (this.adaptiveLoader != null && this.adaptiveLoader.isEnabled()) {
                        permRadius = Math.max(1, permRadius + this.adaptiveLoader.getRadiusModifier(player));
                     }

                     return permRadius;
                  }
               }
            }

            int radius = this.configManager.getDynamicRadius();
            if (this.adaptiveLoader != null && this.adaptiveLoader.isEnabled()) {
               radius = Math.max(1, radius + this.adaptiveLoader.getRadiusModifier(player));
            }

            return radius;
         }
      }
   }

   private void processChunkQueue() {
      int chunksLoaded = 0;
      int maxChunks = this.memoryManager.isLowMemoryMode() ? Math.max(1, this.configManager.getMaxChunksToLoad() / 2) : this.configManager.getMaxChunksToLoad();
      if (this.tpsThrottler != null && this.configManager.isTpsThrottleEnabled()) {
         maxChunks = this.tpsThrottler.throttle(maxChunks);
      }

      while (chunksLoaded < maxChunks && !this.chunkCache.isQueueEmpty()) {
         Chunk chunk = this.chunkCache.pollFromQueue();
         if (chunk != null) {
            this.chunkProcessor.submitLoadTask(chunk);
            chunksLoaded++;
         }
      }
   }

   private void performSmartUnload() {
      for (World world : Bukkit.getWorlds()) {
         if (!this.isWorldBlacklisted(world)) {
            int unloaded = this.smartUnloader.performSmartUnload(world, 10);
            if (unloaded > 0) {
               this.getLogger().fine("Smart unloaded " + unloaded + " cold chunks from " + world.getName());
            }
         }
      }
   }

   private void monitorPerformance() {
      if (!this.manualOverride) {
         this.performanceMonitor.analyzePerformance();
         this.adjustSettingsBasedOnPerformance();
      }
   }

   private void handlePerformanceChange(PerformanceMonitor.PerformanceStatus status) {
      if (!this.manualOverride) {
         switch (status) {
            case CRITICAL:
               if (!this.maintenanceMode) {
                  this.setMaintenanceMode(true, Bukkit.getConsoleSender(), true);
               }
               break;
            case POOR:
               if (!this.maintenanceMode && this.configManager.isDynamicRadiusEnabled()) {
                  int currentRadius = this.configManager.getDynamicRadius();
                  if (currentRadius > 1) {
                     this.configManager.setDynamicRadius(currentRadius - 1);
                     this.getLogger()
                        .fine(this.languageManager.getMessage("radius-reduced", Collections.singletonMap("radius", String.valueOf(currentRadius - 1))));
                  }
               }
               break;
            case EXCELLENT:
               if (this.maintenanceMode && this.autoMaintenanceActive) {
                  this.setMaintenanceMode(false, Bukkit.getConsoleSender(), true);
               } else if (this.configManager.isDynamicRadiusEnabled()) {
                  int currentRadius = this.configManager.getDynamicRadius();
                  if (currentRadius < 6) {
                     this.configManager.setDynamicRadius(currentRadius + 1);
                     this.getLogger()
                        .fine(this.languageManager.getMessage("radius-increased", Collections.singletonMap("radius", String.valueOf(currentRadius + 1))));
                  }
               }
         }
      }
   }

   private void adjustSettingsBasedOnPerformance() {
      PerformanceMonitor.PerformanceData data = this.performanceMonitor.getCurrentPerformance();
      if (this.maintenanceMode) {
         if (this.autoMaintenanceActive) {
            if (data.tickTime < this.configManager.getMaintenanceModeTickTimeThreshold()
               && data.memoryPercentage < this.configManager.getMaintenanceModeMemoryThreshold()) {
               this.setMaintenanceMode(false, Bukkit.getConsoleSender(), true);
            }
         }
      } else {
         if (data.tickTime > this.configManager.getMaintenanceModeTickTimeThreshold()
            || data.memoryPercentage > this.configManager.getMaintenanceModeMemoryThreshold()) {
            this.setMaintenanceMode(true, Bukkit.getConsoleSender(), true);
         }
      }
   }

   @Override
   public boolean isWorldBlacklisted(World world) {
      return world == null ? true : this.configManager.getWorldBlacklist().stream().anyMatch(blacklisted -> blacklisted.equalsIgnoreCase(world.getName()));
   }

   private void setupEventListeners() {
      if (this.advancedFeaturesEnabled) {
         this.eventManager.registerListener(ChunkEventManager.ChunkEventType.PRE_LOAD, event -> this.metricsCollector.recordMetric("cache_misses", 1L));
         this.eventManager.registerListener(ChunkEventManager.ChunkEventType.CACHE_HIT, event -> this.metricsCollector.recordMetric("cache_hits", 1L));
         this.eventManager
            .registerListener(ChunkEventManager.ChunkEventType.OPTIMIZATION_START, event -> this.metricsCollector.recordMetric("optimization_runs", 1L));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (this.advancedFeaturesEnabled) {
         Location to = event.getTo();
         if (to != null && !this.isWorldBlacklisted(to.getWorld())) {
            this.chunkPrefetcher.prefetchArea(to.getWorld(), to, 3);
         }
      }

      if (this.teleportPreloader != null && this.teleportPreloader.isEnabled()) {
         Location to = event.getTo();
         if (to != null) {
            this.teleportPreloader.onTeleport(event.getPlayer(), to);
         }
      }
   }

   private void updateMetrics() {
      if (this.advancedFeaturesEnabled) {
         double tickTime = this.performanceMonitor.getAverageTickTime();
         double memoryUsage = this.memoryManager.getMemoryUsagePercentage() * 100.0;
         int loadedChunks = this.performanceMonitor.getTotalLoadedChunks();
         int onlinePlayers = Bukkit.getOnlinePlayers().size();
         double tps = tickTime > 50.0 ? Math.max(0.0, 20.0 - (tickTime - 50.0) / 50.0) : 20.0;
         Runtime runtime = Runtime.getRuntime();
         long usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
         this.metricsCollector.recordTimeSeriesPoint("tps", tps);
         this.metricsCollector.recordTimeSeriesPoint("memory_mb", usedMemoryMB);
         this.metricsCollector.recordTimeSeriesPoint("loaded_chunks", loadedChunks);
         this.metricsCollector.recordTimeSeriesPoint("online_players", onlinePlayers);
         this.metricsCollector.recordMetric("tick_time", (long)tickTime);
         this.metricsCollector.recordMetric("memory_usage", (long)memoryUsage);
      }
   }

   private void analyzeChunks(CommandSender sender, String[] args) {
      if (!this.advancedFeaturesEnabled) {
         sender.sendMessage(this.languageManager.getMessage("advanced-disabled"));
      } else {
         if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            sender.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "  World Analysis Report");
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            this.taskManager
               .runAsyncTask(
                  "analyze",
                  () -> {
                     for (World worldx : Bukkit.getWorlds()) {
                        if (!this.isWorldBlacklisted(worldx)) {
                           Map<String, Object> stats = this.chunkAnalyzer.getWorldStatistics(worldx);
                           Scheduler.runGlobal(
                              this,
                              () -> {
                                 sender.sendMessage("");
                                 sender.sendMessage(ChatColor.GOLD + "  " + worldx.getName().toUpperCase());
                                 sender.sendMessage(ChatColor.DARK_GRAY + "  ─────────────────────────");
                                 if (stats.containsKey("status")) {
                                    sender.sendMessage(ChatColor.RED + "  " + stats.get("status"));
                                 } else {
                                    sender.sendMessage(
                                       ChatColor.GRAY
                                          + "  Chunks      "
                                          + ChatColor.WHITE
                                          + stats.get("totalChunks")
                                          + ChatColor.DARK_GRAY
                                          + "  |  "
                                          + ChatColor.GRAY
                                          + "Memory  "
                                          + ChatColor.WHITE
                                          + stats.get("totalMemoryUsageMB")
                                          + " MB"
                                    );
                                    sender.sendMessage(
                                       ChatColor.GRAY
                                          + "  Complexity  "
                                          + ChatColor.WHITE
                                          + this.df.format(stats.get("averageComplexity"))
                                          + ChatColor.DARK_GRAY
                                          + "  |  "
                                          + ChatColor.GRAY
                                          + "Impact  "
                                          + ChatColor.WHITE
                                          + this.df.format(stats.get("averagePerformanceImpact"))
                                          + "%"
                                    );
                                    sender.sendMessage("");
                                    sender.sendMessage(
                                       ChatColor.GRAY
                                          + "  Priority    "
                                          + ChatColor.YELLOW
                                          + "High: "
                                          + stats.get("highPriorityChunks")
                                          + ChatColor.DARK_GRAY
                                          + "  |  "
                                          + ChatColor.RED
                                          + "Extreme: "
                                          + stats.get("extremeComplexityChunks")
                                    );
                                    sender.sendMessage(
                                       ChatColor.GRAY
                                          + "  Features    "
                                          + ChatColor.GREEN
                                          + "Struct: "
                                          + stats.get("structureChunks")
                                          + ChatColor.DARK_GRAY
                                          + " | "
                                          + ChatColor.RED
                                          + "Redstone: "
                                          + stats.get("redstoneChunks")
                                          + ChatColor.DARK_GRAY
                                          + " | "
                                          + ChatColor.YELLOW
                                          + "Farms: "
                                          + stats.get("farmChunks")
                                    );
                                    sender.sendMessage(
                                       ChatColor.GRAY
                                          + "  Entities    "
                                          + ChatColor.WHITE
                                          + "Mobs: "
                                          + stats.get("totalMobs")
                                          + ChatColor.DARK_GRAY
                                          + " ("
                                          + this.df.format(stats.get("averageMobsPerChunk"))
                                          + "/chunk)"
                                          + ChatColor.DARK_GRAY
                                          + "  |  "
                                          + ChatColor.GOLD
                                          + "Tiles: "
                                          + stats.get("totalTileEntities")
                                    );
                                 }
                              }
                           );
                        }
                     }

                     Scheduler.runGlobalLater(this, () -> {
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                     }, 5L);
                  }
               );
         } else {
            World world = Bukkit.getWorld(args[0]);
            if (world != null) {
               Location loc = sender instanceof Player p ? p.getLocation() : new Location(world, 0.0, 64.0, 0.0);
               int cx = loc.getBlockX() >> 4;
               int cz = loc.getBlockZ() >> 4;
               ChunkUtils.getChunkAtAsync(world, cx, cz, this)
                  .thenAccept(
                     chunk -> {
                        ChunkAnalyzer.ChunkAnalysisData data = this.chunkAnalyzer.analyzeChunkSync(chunk);
                        Scheduler.runGlobal(
                           this,
                           () -> {
                              sender.sendMessage("");
                              sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                              sender.sendMessage(
                                 "" + ChatColor.AQUA + ChatColor.BOLD + "  Chunk Analysis " + ChatColor.GRAY + "[" + chunk.getX() + ", " + chunk.getZ() + "]"
                              );
                              sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                              sender.sendMessage("");
                              sender.sendMessage(
                                 ChatColor.GRAY
                                    + "  Complexity   "
                                    + ChatColor.WHITE
                                    + data.complexity
                                    + ChatColor.DARK_GRAY
                                    + " ("
                                    + data.getComplexityLevel()
                                    + ")"
                              );
                              sender.sendMessage(ChatColor.GRAY + "  Priority     " + ChatColor.WHITE + data.priority + "/100");
                              sender.sendMessage(ChatColor.GRAY + "  Impact       " + ChatColor.WHITE + this.df.format(data.performanceImpact) + "%");
                              sender.sendMessage(ChatColor.GRAY + "  Memory       " + ChatColor.WHITE + data.estimatedMemoryUsage / 1024 + " KB");
                              sender.sendMessage("");
                              sender.sendMessage(ChatColor.GRAY + "  Features");
                              sender.sendMessage(
                                 ChatColor.DARK_GRAY
                                    + "    Structures: "
                                    + (data.hasPlayerStructures ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
                                    + ChatColor.DARK_GRAY
                                    + "  Redstone: "
                                    + (data.hasRedstone ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
                                    + ChatColor.DARK_GRAY
                                    + "  Farms: "
                                    + (data.hasFarmland ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
                              );
                              sender.sendMessage("");
                              sender.sendMessage(ChatColor.GRAY + "  Entities");
                              sender.sendMessage(
                                 ChatColor.DARK_GRAY
                                    + "    Mobs: "
                                    + ChatColor.WHITE
                                    + data.mobCount
                                    + ChatColor.DARK_GRAY
                                    + "  Passive: "
                                    + ChatColor.WHITE
                                    + data.passiveEntityCount
                                    + ChatColor.DARK_GRAY
                                    + "  Items: "
                                    + ChatColor.WHITE
                                    + data.itemCount
                                    + ChatColor.DARK_GRAY
                                    + "  Tiles: "
                                    + ChatColor.WHITE
                                    + data.tileEntityCount
                              );
                              sender.sendMessage(ChatColor.DARK_GRAY + "    Density: " + ChatColor.WHITE + this.df.format(data.entityDensity) + "/chunk");
                              if (!data.blockCategories.isEmpty()) {
                                 sender.sendMessage("");
                                 sender.sendMessage(ChatColor.GRAY + "  Block Types");
                                 data.blockCategories
                                    .forEach((category, count) -> sender.sendMessage(ChatColor.DARK_GRAY + "    " + category + ": " + ChatColor.WHITE + count));
                              }

                              sender.sendMessage("");
                              sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                           }
                        );
                     }
                  );
            } else {
               sender.sendMessage(this.languageManager.getMessage("invalid-world", Collections.singletonMap("world", args[0])));
            }
         }
      }
   }

   private void displayRegionInfo(CommandSender sender) {
      if (!this.advancedFeaturesEnabled) {
         sender.sendMessage(this.languageManager.getMessage("advanced-disabled"));
      } else {
         sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "Region Statistics " + ChatColor.GRAY + "---");

         for (World world : Bukkit.getWorlds()) {
            if (!this.isWorldBlacklisted(world)) {
               Map<String, Object> stats = this.regionOptimizer.getRegionStatistics(world);
               sender.sendMessage(ChatColor.YELLOW + world.getName() + ":");
               sender.sendMessage(ChatColor.WHITE + "  Total Regions: " + stats.get("totalRegions"));
               sender.sendMessage(ChatColor.WHITE + "  Active Regions: " + stats.get("activeRegions"));
               sender.sendMessage(ChatColor.WHITE + "  Optimized Regions: " + stats.get("optimizedRegions"));
               sender.sendMessage(ChatColor.WHITE + "  High Activity: " + stats.get("highActivityRegions"));
            }
         }

         sender.sendMessage(ChatColor.GRAY + "---------------------------");
      }
   }

   private void displayMetrics(CommandSender sender) {
      if (!this.advancedFeaturesEnabled) {
         sender.sendMessage(this.languageManager.getMessage("advanced-disabled"));
      } else {
         Map<String, Object> metrics = this.metricsCollector.getCurrentMetrics();
         Map<String, Object> prefetchStats = this.chunkPrefetcher.getPrefetchStatistics();
         double tickTime = this.performanceMonitor.getAverageTickTime();
         double tps = tickTime > 50.0 ? Math.max(0.0, 20.0 - (tickTime - 50.0) / 50.0) : 20.0;
         Runtime runtime = Runtime.getRuntime();
         long maxMemory = runtime.maxMemory() / 1048576L;
         long totalMemory = runtime.totalMemory() / 1048576L;
         long freeMemory = runtime.freeMemory() / 1048576L;
         long usedMemory = totalMemory - freeMemory;
         sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.AQUA + "Advanced Metrics " + ChatColor.GRAY + "---");
         sender.sendMessage(ChatColor.YELLOW + "Server Performance:");
         ChatColor tpsColor = tps >= 19.0 ? ChatColor.GREEN : (tps >= 17.0 ? ChatColor.YELLOW : (tps >= 15.0 ? ChatColor.GOLD : ChatColor.RED));
         sender.sendMessage(ChatColor.WHITE + "  TPS: " + tpsColor + this.df.format(tps) + "/20.0");
         sender.sendMessage(ChatColor.WHITE + "  Tick Time: " + this.df.format(tickTime) + "ms (target: 50ms)");
         sender.sendMessage(
            ChatColor.WHITE + "  Memory: " + usedMemory + "/" + maxMemory + " MB (" + this.df.format((double)usedMemory / maxMemory * 100.0) + "%)"
         );
         sender.sendMessage(ChatColor.YELLOW + "Cache Performance:");
         if (metrics.containsKey("cache_hits")) {
            Map<String, Object> cacheHits = (Map<String, Object>)metrics.get("cache_hits");
            Map<String, Object> cacheMisses = (Map<String, Object>)metrics.get("cache_misses");
            long hits = (Long)cacheHits.get("count");
            long misses = (Long)cacheMisses.get("count");
            double hitRate = hits + misses > 0L ? (double)hits / (hits + misses) * 100.0 : 0.0;
            sender.sendMessage(ChatColor.WHITE + "  Hits: " + hits + " | Misses: " + misses);
            sender.sendMessage(ChatColor.WHITE + "  Hit Rate: " + this.df.format(hitRate) + "%");
         } else {
            sender.sendMessage(ChatColor.GRAY + "  No cache data available yet");
         }

         sender.sendMessage(ChatColor.YELLOW + "Prefetch Statistics:");
         sender.sendMessage(ChatColor.WHITE + "  Total Prefetched: " + prefetchStats.get("totalPrefetched"));
         sender.sendMessage(ChatColor.WHITE + "  Cache Size: " + prefetchStats.get("cacheSize"));
         sender.sendMessage(ChatColor.WHITE + "  Tracked Players: " + prefetchStats.get("trackedPlayers"));
         sender.sendMessage(ChatColor.WHITE + "  Avg Confidence: " + this.df.format((Double)prefetchStats.get("averageConfidence") * 100.0) + "%");
         if (metrics.containsKey("optimization_runs")) {
            sender.sendMessage(ChatColor.YELLOW + "Optimization Stats:");
            Map<String, Object> optRuns = (Map<String, Object>)metrics.get("optimization_runs");
            sender.sendMessage(ChatColor.WHITE + "  Total Runs: " + optRuns.get("count"));
         }

         sender.sendMessage(ChatColor.GRAY + "---------------------------");
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("chunkmanager.admin")) {
         return null;
      } else {
         String cmdName = command.getName().toLowerCase();
         if (cmdName.equals("maintenance") && args.length == 1) {
            return Arrays.asList("on", "off", "auto", "status");
         } else if (cmdName.equals("chunklang") && args.length == 1) {
            return new ArrayList<>(this.languageManager.getAvailableLanguages().keySet());
         } else if (cmdName.equals("chunkanalyze") && args.length == 1) {
            return Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).collect(Collectors.toList());
         } else if (cmdName.equals("cmfreeze") && args.length == 1) {
            return Arrays.asList("add", "remove", "list", "clear");
         } else {
            if (cmdName.equals("cmpregen")) {
               if (args.length == 1) {
                  return Arrays.asList("start", "stop", "status");
               }

               if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
                  List<String> suggestions = new ArrayList<>(Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList()));
                  suggestions.addAll(Arrays.asList("5", "10", "25", "50"));
                  return suggestions;
               }
            }

            return null;
         }
      }
   }

   @Override
   public int getCurrentRadius() {
      return this.configManager.getDynamicRadius();
   }

   @Override
   public boolean isMaintenanceMode() {
      return this.maintenanceMode;
   }

   @Override
   public void setMaintenanceMode(boolean enabled) {
      this.setMaintenanceMode(enabled, Bukkit.getConsoleSender(), false);
   }

   @Override
   public void setPlayerRadius(Player player, int radius) {
      this.playerRadiusOverrides.put(player.getUniqueId(), radius);
   }

   @Override
   public int getPlayerRadius(Player player) {
      return this.getEffectiveRadius(player);
   }

   @Override
   public CompletableFuture<PerformanceMonitor.WorldStatistics> getWorldStatistics(World world) {
      return CompletableFuture.supplyAsync(() -> this.performanceMonitor.gatherWorldStatistics(world));
   }

   @Override
   public Map<String, String> getPerformanceInfo() {
      return this.performanceMonitor.getPerformanceInfo();
   }

   @Override
   public void forceChunkOptimization() {
      for (World world : Bukkit.getWorlds()) {
         if (!this.isWorldBlacklisted(world)) {
            this.smartUnloader.performSmartUnload(world, 100);
         }
      }
   }

   @Override
   public void forceGarbageCollection() {
      this.memoryManager.forceGarbageCollection();
   }

   @Override
   public void reloadConfiguration() {
      this.configManager.reload();
      this.languageManager.reloadLanguages();
   }

   @Override
   public int getLoadedChunksCount() {
      return this.performanceMonitor.getTotalLoadedChunks();
   }

   @Override
   public int getQueuedChunksCount() {
      return this.chunkCache.getQueueSize();
   }

   @Override
   public double getAverageTickTime() {
      return this.performanceMonitor.getAverageTickTime();
   }

   @Override
   public double getMemoryUsagePercentage() {
      return this.memoryManager.getMemoryUsagePercentage() * 100.0;
   }

   public ConfigurationManager getConfigManager() {
      return this.configManager;
   }

   public PerformanceMonitor getPerformanceMonitor() {
      return this.performanceMonitor;
   }

   public MemoryManager getMemoryManager() {
      return this.memoryManager;
   }

   public ChunkCache getChunkCache() {
      return this.chunkCache;
   }

   public boolean isManualOverride() {
      return this.manualOverride;
   }

   public GuiManager getGuiManager() {
      return this.guiManager;
   }

   public RegionOptimizer getRegionOptimizer() {
      return this.regionOptimizer;
   }

   public ChunkAnalyzer getChunkAnalyzer() {
      return this.chunkAnalyzer;
   }

   public WorldSettingsManager getWorldSettingsManager() {
      return this.worldSettingsManager;
   }

   public ChunkFreezer getChunkFreezer() {
      return this.chunkFreezer;
   }

   public ChunkVisualizer getChunkVisualizer() {
      return this.chunkVisualizer;
   }

   public SmartChunkUnloader getSmartUnloader() {
      return this.smartUnloader;
   }

   public ChunkPreGen getChunkPreGen() {
      return this.chunkPreGen;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public TpsThrottler getTpsThrottler() {
      return this.tpsThrottler;
   }

   public EntityLimiter getEntityLimiter() {
      return this.entityLimiter;
   }

   public ChunkCostTracker getCostTracker() {
      return this.costTracker;
   }

   public RegionTickMonitor getRegionTickMonitor() {
      return this.regionTickMonitor;
   }

   public MapOverlayManager getMapOverlay() {
      return this.mapOverlay;
   }

   public PlayerAdaptiveLoader getAdaptiveLoader() {
      return this.adaptiveLoader;
   }

   public LoadPriorityQueue getLoadPriorityQueue() {
      return this.loadPriorityQueue;
   }

   public ChunkTicketManager getTicketManager() {
      return this.ticketManager;
   }

   private void displayRegionTps(CommandSender sender) {
      if (this.regionTickMonitor != null && this.regionTickMonitor.isEnabled()) {
         List<RegionTickMonitor.RegionStats> list = this.regionTickMonitor.getLaggiest(10);
         if (list.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No regions tracked yet.");
         } else {
            sender.sendMessage(ChatColor.AQUA + "Top " + list.size() + " regions by MSPT:");
            int i = 1;

            for (RegionTickMonitor.RegionStats s : list) {
               String color = s.avgMspt > 70.0
                  ? ChatColor.RED + ""
                  : (s.avgMspt > 55.0 ? ChatColor.GOLD + "" : (s.avgMspt > 45.0 ? ChatColor.YELLOW + "" : ChatColor.GREEN + ""));
               sender.sendMessage(
                  ChatColor.GRAY
                     + " #"
                     + i++
                     + " "
                     + ChatColor.WHITE
                     + s.worldName
                     + " r("
                     + s.regionX
                     + ","
                     + s.regionZ
                     + ") "
                     + color
                     + this.df.format(s.avgMspt)
                     + " ms "
                     + ChatColor.GRAY
                     + "(tps "
                     + this.df.format(s.getTps())
                     + ")"
               );
            }
         }
      } else {
         sender.sendMessage(ChatColor.YELLOW + "Region tick monitor is disabled.");
      }
   }

   private void handleMapCommand(CommandSender sender, String[] args) {
      if (this.mapOverlay == null) {
         sender.sendMessage(ChatColor.RED + "Map overlay not initialized.");
      } else if (args.length == 0) {
         sender.sendMessage(ChatColor.AQUA + "Map overlay: " + (this.mapOverlay.isEnabled() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
         sender.sendMessage(ChatColor.GRAY + "Hooks active: " + (this.mapOverlay.isAnyAvailable() ? ChatColor.GREEN + "yes" : ChatColor.YELLOW + "no"));
         sender.sendMessage(ChatColor.GRAY + "Usage: /cmmap <refresh|on|off>");
      } else {
         String var3 = args[0].toLowerCase(Locale.ROOT);
         switch (var3) {
            case "refresh":
               this.mapOverlay.refresh();
               sender.sendMessage(ChatColor.GREEN + "Map overlay refreshed.");
               break;
            case "on":
               this.mapOverlay.setEnabled(true);
               sender.sendMessage(ChatColor.GREEN + "Map overlay enabled.");
               break;
            case "off":
               this.mapOverlay.setEnabled(false);
               sender.sendMessage(ChatColor.YELLOW + "Map overlay disabled.");
               break;
            default:
               sender.sendMessage(ChatColor.GRAY + "Usage: /cmmap <refresh|on|off>");
         }
      }
   }

   private void displayTopChunksConsole(CommandSender sender) {
      if (this.costTracker != null && this.costTracker.isEnabled()) {
         List<ChunkCostTracker.ChunkCost> top = this.costTracker.getTopChunks(10);
         if (top.isEmpty()) {
            sender.sendMessage(this.languageManager.getMessage("cmtop-empty"));
         } else {
            sender.sendMessage(this.languageManager.getMessage("cmtop-header", Collections.singletonMap("count", String.valueOf(top.size()))));

            for (int i = 0; i < top.size(); i++) {
               ChunkCostTracker.ChunkCost c = top.get(i);
               Map<String, String> ph = new HashMap<>();
               ph.put("rank", String.valueOf(i + 1));
               ph.put("world", c.worldName);
               ph.put("x", String.valueOf(c.x));
               ph.put("z", String.valueOf(c.z));
               ph.put("cost", this.df.format(c.costScore));
               ph.put("entities", String.valueOf(c.entityCount));
               ph.put("tiles", String.valueOf(c.tileEntityCount));
               sender.sendMessage(this.languageManager.getMessage("cmtop-entry", ph));
            }
         }
      } else {
         sender.sendMessage(this.languageManager.getMessage("cmtop-empty"));
      }
   }

   @Override
   public boolean isBedrockPlayer(Player player) {
      return this.adaptiveLoader != null && this.adaptiveLoader.isBedrockPlayer(player);
   }

   @Override
   public double getCurrentTps() {
      double tickTime = this.performanceMonitor.getAverageTickTime();
      return tickTime > 50.0 ? Math.max(0.0, 20.0 - (tickTime - 50.0) / 50.0) : 20.0;
   }
}
