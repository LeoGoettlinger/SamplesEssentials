package com.skyblockexp.ezrtp.bootstrap;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.api.EzRtpAPI;
import com.skyblockexp.ezrtp.bootstrap.component.ConfigurationService;
import com.skyblockexp.ezrtp.bootstrap.component.EconomyCoordinator;
import com.skyblockexp.ezrtp.bootstrap.component.ListenerRegistrar;
import com.skyblockexp.ezrtp.bootstrap.component.NetworkCoordinator;
import com.skyblockexp.ezrtp.bootstrap.component.PlatformAdapterRegistrar;
import com.skyblockexp.ezrtp.bootstrap.component.UsageResetScheduler;
import com.skyblockexp.ezrtp.command.ForceRtpCommand;
import com.skyblockexp.ezrtp.command.RandomTeleportCommand;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.ForceRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.biome.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.performance.PerformanceSettings;
import com.skyblockexp.ezrtp.config.safety.UnsafeLocationSettings;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import com.skyblockexp.ezrtp.gui.RandomTeleportGuiManager;
import com.skyblockexp.ezrtp.integration.EzCountdownDisplayBridge;
import com.skyblockexp.ezrtp.integration.EzCountdownDisplayBridgeImpl;
import com.skyblockexp.ezrtp.integration.TeamsApiSubcommandBridge;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.metrics.EzRtpMetricsRegistrar;
import com.skyblockexp.ezrtp.performance.PerformanceMonitor;
import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.ChunkLoadStrategyRegistry;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridge;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeCapabilitiesDetector;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridge;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridgeRegistry;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.pvptag.CombatLogXPvpTagProvider;
import com.skyblockexp.ezrtp.pvptag.PvpManagerPvpTagProvider;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import com.skyblockexp.ezrtp.pvptag.SimpleCombatLogPvpTagProvider;
import com.skyblockexp.ezrtp.storage.MySqlRtpUsageStorage;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.storage.YamlRtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.ChunkyProvider;
import com.skyblockexp.ezrtp.teleport.ChunkyRuntimeProvider;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.heatmap.HeatmapSimulationStore;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationStatistics;
import com.skyblockexp.ezrtp.update.ReleaseUpdateChecker;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class EzRtpPluginBootstrap {
   private static final String CORE_PLUGIN_NAME = "EzRTP";
   private static final List<String> RUNTIME_MODULE_PLUGIN_NAMES = List.of(
      "EzRTPPaperModule", "EzRTPPurpurModule", "EzRTPPaper", "EzRTP-Paper", "EzRTPPaperRuntimeModule"
   );
   private final EzRtpPlugin plugin;
   private final ConfigurationService configurationService;
   private final EconomyCoordinator economyCoordinator;
   private final NetworkCoordinator networkCoordinator;
   private final ListenerRegistrar listenerRegistrar;
   private EzRtpConfiguration configuration;
   private ForceRtpConfiguration forceRtpConfiguration;
   private RandomTeleportService teleportService;
   private PerformanceSettings performanceSettings;
   private PerformanceMonitor performanceMonitor;
   private UnsafeLocationSettings unsafeLocationSettings;
   private UnsafeLocationMonitor unsafeLocationMonitor;
   private EconomyService economyService = EconomyService.disabled();
   private RtpUsageStorage usageStorage;
   private ProtectionRegistry protectionRegistry;
   private MessageProvider messageProvider;
   private ChunkyProvider chunkyAPI;
   private EzRtpMetricsRegistrar metricsRegistrar;
   private UsageResetScheduler usageResetScheduler;
   private final HeatmapSimulationStore heatmapSimulationStore = new HeatmapSimulationStore();
   private ChunkyWarmupCoordinator chunkyWarmupCoordinator;
   private PvpTagService pvpTagService;
   private TeamsApiSubcommandBridge teamsApiSubcommandBridge;
   private EzCountdownDisplayBridge ezCountdownBridge;

   public EzRtpPluginBootstrap(EzRtpPlugin plugin) {
      this.plugin = plugin;
      this.configurationService = new ConfigurationService(plugin);
      this.economyCoordinator = new EconomyCoordinator(plugin);
      this.networkCoordinator = new NetworkCoordinator(plugin);
      this.listenerRegistrar = new ListenerRegistrar(plugin);
      this.chunkyWarmupCoordinator = new ChunkyWarmupCoordinator();
   }

   public void enable() {
      this.plugin.saveDefaultConfig();
      this.configurationService.ensureAdditionalConfigFiles();
      EzRtpPluginBootstrap.RuntimeArtifactStatus runtimeArtifactStatus = this.inspectRuntimeArtifacts();
      if (!runtimeArtifactStatus.unsupportedModules().isEmpty()) {
         String unsupported = String.join(", ", runtimeArtifactStatus.unsupportedModules());
         this.plugin.getLogger().severe("Unsupported EzRTP runtime module combination detected: " + unsupported + ".");
         this.plugin.getLogger().severe("Likely symptoms include duplicate chat messages and duplicate/random double teleports.");
         this.plugin
            .getLogger()
            .severe(
               "Remove unsupported EzRTP runtime module jar(s) and keep only 'EzRTP.jar' and optionally one supported module plugin such as 'EzRTPPaperModule'."
            );
         this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
      } else {
         this.logRuntimeArtifactSummary(runtimeArtifactStatus);
         PlatformAdapterRegistrar.registerKnownProviders(this.plugin.getLogger());
         ChunkLoadStrategyRegistry.loadAndRegister(this.plugin, this.plugin.getLogger());
         PlatformRuntimeRegistry.loadAndRegister(this.plugin, this.plugin.getLogger());
         PlatformGuiBridgeRegistry.loadAndRegister(this.plugin, this.plugin.getLogger());
         PlatformMessageServiceRegistry.loadAndRegister(this.plugin, this.plugin.getLogger());
         PlatformSenderBridgeRegistry.loadAndRegister(this.plugin, this.plugin.getLogger());
         this.logSelectedPlatformAdapters();
         this.economyService = this.economyCoordinator.initializeEconomy();
         this.reloadPluginConfiguration();
         this.usageStorage = this.createUsageStorage();
         this.usageResetScheduler = new UsageResetScheduler(this.plugin, PlatformRuntimeRegistry.get().scheduler(), this.usageStorage);
         this.usageResetScheduler.schedule();
         this.registerListeners();
         this.registerCommand();
         this.initializeMetrics();
         new ReleaseUpdateChecker(this.plugin).checkForUpdates();
         this.plugin.getLogger().info("Ready.");
      }
   }

   private void logRuntimeArtifactSummary(EzRtpPluginBootstrap.RuntimeArtifactStatus status) {
      if (status.hasSupportedRuntimeModule()) {
         this.plugin.getLogger().info("Runtime module: " + String.join(", ", status.supportedRuntimeModules()));
      } else {
         this.plugin.getLogger().warning("No runtime module installed; using built-in Bukkit adapters.");
      }
   }

   private EzRtpPluginBootstrap.RuntimeArtifactStatus inspectRuntimeArtifacts() {
      Map<String, Plugin> byName = new LinkedHashMap<>();

      for (Plugin installedPlugin : this.plugin.getServer().getPluginManager().getPlugins()) {
         String pluginName = installedPlugin.getName();
         if (pluginName != null && !pluginName.isBlank()) {
            byName.put(pluginName, installedPlugin);
         }
      }

      List<String> installedArtifacts = new ArrayList<>();
      if (byName.containsKey("EzRTP")) {
         installedArtifacts.add("EzRTP");
      }

      List<String> supportedRuntimeModules = new ArrayList<>();
      List<String> unsupportedModules = new ArrayList<>();

      for (String runtimeModuleName : RUNTIME_MODULE_PLUGIN_NAMES) {
         if (byName.containsKey(runtimeModuleName)) {
            installedArtifacts.add(runtimeModuleName);
            if (!"EzRTPPaperModule".equals(runtimeModuleName) && !"EzRTPPurpurModule".equals(runtimeModuleName)) {
               unsupportedModules.add(runtimeModuleName);
            } else {
               supportedRuntimeModules.add(runtimeModuleName);
            }
         }
      }

      return new EzRtpPluginBootstrap.RuntimeArtifactStatus(installedArtifacts, supportedRuntimeModules, unsupportedModules);
   }

   public void disable() {
      if (!PlatformRuntimeCapabilitiesDetector.detect(this.plugin).regionizedRuntime()) {
         this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
      }

      if (this.unsafeLocationMonitor != null) {
         this.unsafeLocationMonitor.shutdown();
         this.unsafeLocationMonitor = null;
      }

      if (this.performanceMonitor != null) {
         this.performanceMonitor.shutdown();
         this.performanceMonitor = null;
      }

      if (this.teleportService != null) {
         this.teleportService.shutdown();

         try {
            EzRtpAPI.unregisterProvider(this.teleportService);
         } catch (Throwable var2) {
         }
      }

      if (this.teamsApiSubcommandBridge != null) {
         this.teamsApiSubcommandBridge.unregister();
         this.teamsApiSubcommandBridge = null;
      }

      this.networkCoordinator.shutdown();
      this.plugin.getLogger().info("EzRTP plugin disabled.");
      PlatformSenderBridgeRegistry.closeAndUnregister();
      PlatformMessageServiceRegistry.closeAndUnregister();
      ChunkLoadStrategyRegistry.unregister();
      PlatformRuntimeRegistry.unregister();
      PlatformGuiBridgeRegistry.unregister();
   }

   public void reloadPluginConfiguration() {
      this.configuration = this.configurationService.reloadConfiguration();
      this.forceRtpConfiguration = this.configurationService.reloadForceRtpConfiguration();
      RandomTeleportSettings defaultSettings = this.configuration.getDefaultSettings();
      boolean chunkyIntegrationEnabled = defaultSettings.getChunkyIntegrationSettings().isEnabled();
      if (chunkyIntegrationEnabled && this.chunkyAPI == null) {
         try {
            Class<?> chunkyApiClass = Class.forName("org.popcraft.chunky.api.ChunkyAPI");
            Object loaded = Bukkit.getServicesManager().load(chunkyApiClass);
            if (loaded != null) {
               ChunkyProvider provider = ChunkyRuntimeProvider.loadProvider();
               if (provider != null) {
                  provider.registerListeners(this.plugin);
                  if (this.chunkyWarmupCoordinator != null) {
                     this.chunkyWarmupCoordinator.registerWithChunky(provider, this.plugin);
                  }

                  this.chunkyAPI = provider;
               }
            }
         } catch (ClassNotFoundException var12) {
            this.plugin.getLogger().info("Chunky plugin not found - Chunky integration disabled.");
         } catch (Throwable var13) {
            this.plugin.getLogger().warning("Failed to initialize Chunky integration: " + var13.getMessage());
         }
      } else if (!chunkyIntegrationEnabled && this.chunkyAPI != null) {
         this.plugin.getLogger().info("Chunky integration disabled in configuration.");
         this.chunkyAPI = null;
      }

      if (this.pvpTagService == null) {
         this.pvpTagService = new PvpTagService();
         if (this.plugin.getServer().getPluginManager().isPluginEnabled("CombatLogX")) {
            this.pvpTagService.registerProvider(new CombatLogXPvpTagProvider());
            this.plugin.getLogger().info("PvP tag integration: CombatLogX detected.");
         }

         if (this.plugin.getServer().getPluginManager().isPluginEnabled("PvPManager")) {
            this.pvpTagService.registerProvider(new PvpManagerPvpTagProvider());
            this.plugin.getLogger().info("PvP tag integration: PvPManager detected.");
         }

         if (this.plugin.getServer().getPluginManager().isPluginEnabled("CombatLog")) {
            this.pvpTagService.registerProvider(new SimpleCombatLogPvpTagProvider());
            this.plugin.getLogger().info("PvP tag integration: Simple Combat Log detected.");
         }
      }

      this.messageProvider = this.configurationService.getMessageProvider();
      if (this.ezCountdownBridge == null && this.plugin.getServer().getPluginManager().isPluginEnabled("EzCountdown")) {
         try {
            this.ezCountdownBridge = new EzCountdownDisplayBridgeImpl(this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents((Listener)this.ezCountdownBridge, this.plugin);
            this.plugin.getLogger().info("EzCountdown integration enabled.");
         } catch (NoClassDefFoundError var10) {
            this.plugin.getLogger().warning("Failed to initialize EzCountdown integration: " + var10.getMessage());
            this.ezCountdownBridge = null;
         } catch (Throwable var11) {
            this.plugin.getLogger().warning("Failed to initialize EzCountdown integration: " + var11.getMessage());
            this.ezCountdownBridge = null;
         }
      }

      if (this.teleportService != null) {
         this.teleportService.setEzCountdownBridge(this.ezCountdownBridge);
      }

      boolean forceLegacy = this.configurationService.getEffectiveBaseConfiguration().getBoolean("messages.force-legacy-colors", false);
      MessageUtil.setForceLegacyColors(forceLegacy);
      if (this.chunkyWarmupCoordinator != null) {
         ChunkyIntegrationSettings chunkySettings = defaultSettings.getChunkyIntegrationSettings();
         this.chunkyWarmupCoordinator
            .configureMemorySafety(
               chunkySettings.isMemorySafetyEnabled(),
               chunkySettings.getMinFreeMemoryMb(),
               chunkySettings.getMaxCoordinatorEntries(),
               chunkySettings.getLowMemoryRetentionMinutes()
            );
         if (chunkySettings.isMemorySafetyEnabled()) {
            Runtime runtime = Runtime.getRuntime();
            long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
            this.plugin
               .getLogger()
               .info(String.format("Chunky: memory safety enabled (%dMB free, threshold %dMB)", freeMemoryMb, chunkySettings.getMinFreeMemoryMb()));
         }
      }

      if (this.protectionRegistry == null) {
         this.protectionRegistry = new ProtectionRegistry(this.plugin);
      }

      this.protectionRegistry.warnMissingProviders(defaultSettings.getProtectionSettings());
      if (this.teleportService == null) {
         this.teleportService = new RandomTeleportService(
            this.plugin,
            defaultSettings,
            this.configuration.getQueueSettings(),
            this.economyService,
            (player, settings) -> this.configuration.resolveTeleportCost(player, settings),
            this.protectionRegistry,
            this.messageProvider,
            ChunkLoadStrategyRegistry.get(),
            PlatformRuntimeRegistry.get(),
            this.chunkyAPI,
            this.chunkyWarmupCoordinator,
            this.pvpTagService
         );
         this.teleportService.setEzCountdownBridge(this.ezCountdownBridge);

         try {
            EzRtpAPI.registerProvider(this.plugin, this.teleportService);
         } catch (Throwable var9) {
         }
      } else {
         this.teleportService.reload(defaultSettings, this.configuration.getQueueSettings());
         this.teleportService.setEconomyService(this.economyService);
         this.teleportService.setCostResolver((player, settings) -> this.configuration.resolveTeleportCost(player, settings));
         this.teleportService.setProtectionRegistry(this.protectionRegistry);
         this.teleportService.setEzCountdownBridge(this.ezCountdownBridge);

         try {
            EzRtpAPI.registerProvider(this.plugin, this.teleportService);
         } catch (Throwable var8) {
         }
      }

      this.networkCoordinator.reload(this.configuration);
      RandomTeleportGuiManager guiManager = this.listenerRegistrar.getGuiManager();
      if (guiManager != null) {
         guiManager.closeAll();
      }

      FactionClaimSelectionGuiManager factionClaimGuiManager = this.listenerRegistrar.getFactionClaimGuiManager();
      if (factionClaimGuiManager != null) {
         factionClaimGuiManager.closeAll();
      }

      this.configurationService.validateEconomyConfiguration(this.configuration, this.economyService);
      this.performanceSettings = this.configurationService.reloadPerformanceConfiguration();
      if (this.performanceMonitor != null) {
         this.performanceMonitor.shutdown();
      }

      this.performanceMonitor = new PerformanceMonitor(
         this.performanceSettings, this.teleportService.getStatistics(), this.teleportService.getBiomeCache(), this.plugin.getLogger()
      );
      this.teleportService.setPerformanceMonitor(this.performanceMonitor);
      this.performanceMonitor.schedulePeriodicExport(PlatformRuntimeRegistry.get().scheduler());
      this.unsafeLocationSettings = this.configurationService.reloadUnsafeLocationConfiguration();
      if (this.unsafeLocationMonitor != null) {
         this.unsafeLocationMonitor.shutdown();
      }

      this.unsafeLocationMonitor = new UnsafeLocationMonitor(
         this.unsafeLocationSettings, new UnsafeLocationStatistics(), this.teleportService.getStatistics(), this.plugin.getLogger()
      );
      this.teleportService.setUnsafeLocationMonitor(this.unsafeLocationMonitor);
      this.unsafeLocationMonitor.schedulePeriodicExport(PlatformRuntimeRegistry.get().scheduler());
   }

   public MessageProvider getMessageProvider() {
      return this.messageProvider;
   }

   public RandomTeleportService getTeleportService() {
      return this.teleportService;
   }

   public PerformanceMonitor getPerformanceMonitor() {
      return this.performanceMonitor;
   }

   public UnsafeLocationMonitor getUnsafeLocationMonitor() {
      return this.unsafeLocationMonitor;
   }

   public EzRtpConfiguration getConfiguration() {
      return this.configuration;
   }

   public ForceRtpConfiguration getForceRtpConfiguration() {
      return this.forceRtpConfiguration;
   }

   public RtpUsageStorage getUsageStorage() {
      return this.usageStorage;
   }

   private void initializeMetrics() {
      this.metricsRegistrar = new EzRtpMetricsRegistrar(this.plugin, this::getTeleportService);
      this.metricsRegistrar.register();
   }

   private void logSelectedPlatformAdapters() {
      PlatformRuntime runtime = PlatformRuntimeRegistry.get();
      PlatformScheduler scheduler = runtime.scheduler();
      ChunkLoadStrategy strategy = ChunkLoadStrategyRegistry.get();
      PlatformGuiBridge guiBridge = PlatformGuiBridgeRegistry.get();
      PlatformMessageService messageService = PlatformMessageServiceRegistry.get();
      PlatformSenderBridge senderBridge = PlatformSenderBridgeRegistry.get();
      this.plugin
         .getLogger()
         .info("Platform: " + runtime.getClass().getSimpleName() + " | " + scheduler.getClass().getSimpleName() + " | " + strategy.getClass().getSimpleName());
      this.plugin
         .getLogger()
         .info(
            "Adapters: GUI="
               + guiBridge.getClass().getSimpleName()
               + " | Messages="
               + (messageService != null ? messageService.getClass().getSimpleName() : "<none>")
               + " | Sender="
               + (senderBridge != null ? senderBridge.getClass().getSimpleName() : "<none>")
         );
   }

   private RtpUsageStorage createUsageStorage() {
      ConfigurationSection effectiveBase = this.configurationService.getEffectiveBaseConfiguration();
      String storageType = effectiveBase.getString("rtp-limits.storage", "yaml");
      if (storageType.equalsIgnoreCase("mysql")) {
         String url = effectiveBase.getString("rtp-limits.mysql.url", "jdbc:mysql://localhost:3306/mc");
         String user = effectiveBase.getString("rtp-limits.mysql.user", "root");
         String pass = effectiveBase.getString("rtp-limits.mysql.password", "");
         return new MySqlRtpUsageStorage(url, user, pass);
      } else {
         return new YamlRtpUsageStorage(new File(this.plugin.getDataFolder(), "rtp-usage.yml"));
      }
   }

   private void registerCommand() {
      RandomTeleportGuiManager guiManager = this.listenerRegistrar.getGuiManager();
      FactionClaimSelectionGuiManager factionClaimGuiManager = this.listenerRegistrar.getFactionClaimGuiManager();
      RandomTeleportCommand command = new RandomTeleportCommand(
         this.plugin,
         this::getTeleportService,
         this::getConfiguration,
         this::getProtectionRegistry,
         guiManager,
         factionClaimGuiManager,
         this.usageStorage,
         this.heatmapSimulationStore,
         this.chunkyAPI,
         this.chunkyWarmupCoordinator
      );
      PluginCommand pluginCommand = Objects.requireNonNull(this.plugin.getCommand("rtp"), "rtp command not defined in plugin.yml");
      pluginCommand.setExecutor(command);
      pluginCommand.setTabCompleter(command);
      ForceRtpCommand forceRtpCommand = new ForceRtpCommand(this.plugin, this::getTeleportService, this::getConfiguration, this::getForceRtpConfiguration);
      PluginCommand forceRtpPluginCommand = Objects.requireNonNull(this.plugin.getCommand("forcertp"), "forcertp command not defined in plugin.yml");
      forceRtpPluginCommand.setExecutor(forceRtpCommand);
      forceRtpPluginCommand.setTabCompleter(forceRtpCommand);
      this.teamsApiSubcommandBridge = new TeamsApiSubcommandBridge(this.plugin, factionClaimGuiManager);
      this.teamsApiSubcommandBridge.register();
   }

   private void registerListeners() {
      this.listenerRegistrar
         .register(this::getTeleportService, this::getConfiguration, this.networkCoordinator::getNetworkService, this::getMessageProvider, this.usageStorage);
   }

   public Object getChunkyAPI() {
      return this.chunkyAPI;
   }

   public ProtectionRegistry getProtectionRegistry() {
      return this.protectionRegistry;
   }

   private record RuntimeArtifactStatus(List<String> installedArtifacts, List<String> supportedRuntimeModules, List<String> unsupportedModules) {
      private boolean hasSupportedRuntimeModule() {
         return !this.supportedRuntimeModules.isEmpty();
      }
   }
}
