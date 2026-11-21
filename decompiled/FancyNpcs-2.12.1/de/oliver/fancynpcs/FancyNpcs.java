package de.oliver.fancynpcs;

import com.fancyinnovations.config.featureflags.FeatureFlag;
import com.fancyinnovations.config.featureflags.FeatureFlagConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.oliver.fancyanalytics.api.FancyAnalyticsAPI;
import de.oliver.fancyanalytics.api.metrics.MetricSupplier;
import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.LogLevel;
import de.oliver.fancyanalytics.logger.appender.Appender;
import de.oliver.fancyanalytics.logger.appender.ConsoleAppender;
import de.oliver.fancyanalytics.logger.appender.JsonAppender;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancyanalytics.sdk.events.Event;
import de.oliver.fancylib.FancyLib;
import de.oliver.fancylib.Metrics;
import de.oliver.fancylib.VersionConfig;
import de.oliver.fancylib.logging.PluginMiddleware;
import de.oliver.fancylib.serverSoftware.ServerSoftware;
import de.oliver.fancylib.serverSoftware.schedulers.BukkitScheduler;
import de.oliver.fancylib.serverSoftware.schedulers.FancyScheduler;
import de.oliver.fancylib.serverSoftware.schedulers.FoliaScheduler;
import de.oliver.fancylib.translations.Language;
import de.oliver.fancylib.translations.TextConfig;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.versionFetcher.MasterVersionFetcher;
import de.oliver.fancylib.versionFetcher.VersionFetcher;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.NpcManager;
import de.oliver.fancynpcs.api.actions.types.BlockUntilDoneAction;
import de.oliver.fancynpcs.api.actions.types.ConsoleCommandAction;
import de.oliver.fancynpcs.api.actions.types.ExecuteRandomActionAction;
import de.oliver.fancynpcs.api.actions.types.MessageAction;
import de.oliver.fancynpcs.api.actions.types.NeedItemAction;
import de.oliver.fancynpcs.api.actions.types.NeedPermissionAction;
import de.oliver.fancynpcs.api.actions.types.PlaySoundAction;
import de.oliver.fancynpcs.api.actions.types.PlayerCommandAction;
import de.oliver.fancynpcs.api.actions.types.PlayerCommandAsOpAction;
import de.oliver.fancynpcs.api.actions.types.SendToServerAction;
import de.oliver.fancynpcs.api.actions.types.WaitAction;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinManager;
import de.oliver.fancynpcs.commands.CloudCommandManager;
import de.oliver.fancynpcs.listeners.PlayerChangedWorldListener;
import de.oliver.fancynpcs.listeners.PlayerJoinListener;
import de.oliver.fancynpcs.listeners.PlayerLoadedListener;
import de.oliver.fancynpcs.listeners.PlayerNpcsListener;
import de.oliver.fancynpcs.listeners.PlayerQuitListener;
import de.oliver.fancynpcs.listeners.PlayerTeleportListener;
import de.oliver.fancynpcs.listeners.PlayerUseUnknownEntityListener;
import de.oliver.fancynpcs.skins.SkinManagerImpl;
import de.oliver.fancynpcs.skins.SkinUtils;
import de.oliver.fancynpcs.skins.cache.SkinCacheFile;
import de.oliver.fancynpcs.skins.cache.SkinCacheMemory;
import de.oliver.fancynpcs.skins.mineskin.MineSkinQueue;
import de.oliver.fancynpcs.skins.mojang.MojangQueue;
import de.oliver.fancynpcs.skins.uuidcache.UUIDFileCache;
import de.oliver.fancynpcs.tests.PlaceholderApiEnv;
import de.oliver.fancynpcs.tracker.TurnToPlayerTracker;
import de.oliver.fancynpcs.tracker.VisibilityTracker;
import de.oliver.fancynpcs.utils.OldSkinCacheMigrator;
import de.oliver.fancynpcs.v1_21_11.Npc_1_21_11;
import de.oliver.fancynpcs.v1_21_6.Npc_1_21_6;
import de.oliver.fancynpcs.v1_21_9.Npc_1_21_9;
import de.oliver.fancynpcs.v26_1_1.Npc_26_1_1;
import de.oliver.fancynpcs.v26_2.Npc_26_2;
import de.oliver.fancynpcs.v26_3.Npc_26_3;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FancyNpcs extends JavaPlugin implements FancyNpcsPlugin {
   private int cacheLayer;
   public static final FeatureFlag PLAYER_NPCS_FEATURE_FLAG = new FeatureFlag("player-npcs", "Every player can only manage the npcs they have created", false);
   public static final FeatureFlag USE_NATIVE_THREADS_FEATURE_FLAG = new FeatureFlag(
      "use-native-threads", "Use native threads instead of virtual threads.", false
   );
   public static final FeatureFlag ENABLE_DEBUG_MODE_FEATURE_FLAG = new FeatureFlag("enable-debug-mode", "Enable debug mode", false);
   public static final FeatureFlag ENABLE_FOLIA_VISIBILITY_FIX_FEATURE_FLAG = new FeatureFlag(
      "enable-folia-visibility-fix", "When enabled, all npcs will respawn after 100ms when they should spawn", false
   );
   public static final FeatureFlag USE_MINECRAFT_USERCACHE_FEATURE_FLAG = new FeatureFlag(
      "use-minecraft-usercache", "Include the content of usercache.json to the username->uuid cache", false
   );
   private static FancyNpcs instance;
   private final ExtendedFancyLogger fancyLogger;
   private final ScheduledExecutorService npcThread;
   private final FancyScheduler scheduler;
   private final FancyNpcsConfigImpl config;
   private final VersionConfig versionConfig;
   private final FeatureFlagConfig featureFlagConfig;
   private final VersionFetcher versionFetcher;
   private final FancyAnalyticsAPI fancyAnalytics;
   private CloudCommandManager commandManager;
   private TextConfig textConfig;
   private Translator translator;
   private Function<NpcData, Npc> npcAdapter;
   private NpcManagerImpl npcManager;
   private AttributeManagerImpl attributeManager;
   private SkinManagerImpl skinManager;
   private ActionManagerImpl actionManager;
   private VisibilityTracker visibilityTracker;
   private boolean usingPlotSquared;

   public FancyNpcs() {
      instance = this;
      Appender consoleAppender = new ConsoleAppender("[{loggerName}] ({threadName}) {logLevel}: {message}");
      String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
      File logsFile = new File("plugins/FancyNpcs/logs/FN-logs-" + date + ".txt");
      if (!logsFile.exists()) {
         try {
            logsFile.getParentFile().mkdirs();
            logsFile.createNewFile();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      JsonAppender jsonAppender = new JsonAppender(false, false, true, logsFile.getPath());
      this.fancyLogger = new ExtendedFancyLogger("FancyNpcs", LogLevel.INFO, List.of(consoleAppender, jsonAppender), List.of(new PluginMiddleware(this)));
      this.npcThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("FancyNpcs-Npcs").build());
      this.scheduler = (FancyScheduler)(ServerSoftware.isFolia() ? new FoliaScheduler(instance) : new BukkitScheduler(instance));
      this.config = new FancyNpcsConfigImpl();
      this.versionFetcher = new MasterVersionFetcher(this.getName());
      this.versionConfig = new VersionConfig(this, this.versionFetcher);
      this.fancyAnalytics = new FancyAnalyticsAPI("ca2baf32-1fd2-4baa-a38a-f12ed8ab24a4", "Y7EP2jJjYWExZjdmMDkwNTQ5ZmRbIGUI");
      this.fancyAnalytics.getConfig().setDisableLogging(true);
      this.featureFlagConfig = new FeatureFlagConfig(this);
   }

   public static FancyNpcs getInstance() {
      return instance;
   }

   public void onLoad() {
      this.featureFlagConfig.addFeatureFlag(PLAYER_NPCS_FEATURE_FLAG);
      this.featureFlagConfig.addFeatureFlag(USE_NATIVE_THREADS_FEATURE_FLAG);
      this.featureFlagConfig.addFeatureFlag(ENABLE_DEBUG_MODE_FEATURE_FLAG);
      this.featureFlagConfig.addFeatureFlag(ENABLE_FOLIA_VISIBILITY_FIX_FEATURE_FLAG);
      this.featureFlagConfig.addFeatureFlag(USE_MINECRAFT_USERCACHE_FEATURE_FLAG);
      this.featureFlagConfig.load();
      if (ENABLE_DEBUG_MODE_FEATURE_FLAG.isEnabled()) {
         this.fancyLogger.setCurrentLevel(LogLevel.DEBUG);
      }

      String mcVersion = Bukkit.getServer().getMinecraftVersion();

      this.npcAdapter = switch (mcVersion) {
         case "26.3" -> Npc_26_3::new;
         case "26.2" -> Npc_26_2::new;
         case "26.1.2" -> Npc_26_1_1::new;
         case "1.21.11" -> Npc_1_21_11::new;
         case "1.21.9", "1.21.10" -> Npc_1_21_9::new;
         case "1.21.6", "1.21.7", "1.21.8" -> Npc_1_21_6::new;
         default -> null;
      };
      if (this.npcAdapter == null) {
         this.fancyAnalytics
            .sendEvent(
               new Event("pluginLoadingWithUnsupportedVersion", new HashMap<>())
                  .withProperty("version", mcVersion)
                  .withProperty("pluginVersion", this.getPluginMeta().getVersion())
            );
         this.fancyLogger.error("Unsupported minecraft server version.");
         this.getLogger().warning("--------------------------------------------------");
         this.getLogger().warning("Unsupported minecraft server version.");
         this.getLogger().warning("This plugin only supports 1.21.6 - latest");
         this.getLogger().warning("Disabling the FancyNpcs plugin.");
         this.getLogger().warning("--------------------------------------------------");
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         this.npcManager = new NpcManagerImpl(this, this.npcAdapter);
      }
   }

   public void onEnable() {
      this.cacheLayer = this.initializeCacheLayer();
      if (this.npcAdapter != null) {
         new FancyLib(instance);
         this.config.reload();
         this.attributeManager = new AttributeManagerImpl();
         this.actionManager = new ActionManagerImpl();
         this.actionManager.registerAction(new MessageAction());
         this.actionManager.registerAction(new PlayerCommandAction());
         this.actionManager.registerAction(new PlayerCommandAsOpAction());
         this.actionManager.registerAction(new ConsoleCommandAction());
         this.actionManager.registerAction(new SendToServerAction());
         this.actionManager.registerAction(new WaitAction());
         this.actionManager.registerAction(new ExecuteRandomActionAction());
         this.actionManager.registerAction(new BlockUntilDoneAction());
         this.actionManager.registerAction(new NeedPermissionAction());
         this.actionManager.registerAction(new NeedItemAction());
         this.actionManager.registerAction(new PlaySoundAction());
         this.skinManager = new SkinManagerImpl(new UUIDFileCache(), new SkinCacheFile(), new SkinCacheMemory(), MojangQueue.get(), MineSkinQueue.get());
         OldSkinCacheMigrator.migrate();
         this.textConfig = new TextConfig(
            "#ffcc24",
            "gray",
            "#81E366",
            "#E3CA66",
            "#E36666",
            "<color:#ba8813>[</color><gradient:#ffae00:#fffb00:#ffae00>FancyNpcs</gradient><color:#ba8813>]</color> <gray>"
         );
         this.translator = new Translator(this.textConfig);
         this.translator.loadLanguages(this.getDataFolder().getAbsolutePath());
         Language selectedLanguage = this.translator
            .getLanguages()
            .stream()
            .filter(language -> language.getLanguageName().equals(this.config.getLanguage()))
            .findFirst()
            .orElse(this.translator.getFallbackLanguage());
         this.translator.setSelectedLanguage(selectedLanguage);
         this.versionConfig.load();
         ComparableVersion currentVersion = new ComparableVersion(this.versionConfig.getVersion());
         CompletableFuture.supplyAsync(this.getVersionFetcher()::fetchNewestVersion)
            .thenApply(Objects::requireNonNull)
            .whenComplete(
               (newest, error) -> {
                  if (error == null && newest.compareTo(currentVersion) > 0) {
                     this.fancyLogger.warn("You are not using the latest version of the FancyNpcs plugin.");
                     this.getLogger()
                        .warning(
                           "\n-------------------------------------------------------\nYou are not using the latest version of the FancyNpcs plugin.\nPlease update to the newest version (%s).\n%s\n-------------------------------------------------------\n"
                              .formatted(newest, this.getVersionFetcher().getDownloadUrl())
                        );
                  }
               }
            );
         if (!ServerSoftware.isPaper()) {
            this.fancyLogger.warn("You are not using Paper as server software.");
            this.getLogger().warning("--------------------------------------------------");
            this.getLogger().warning("It is recommended to use Paper as server software.");
            this.getLogger().warning("Because you are not using paper, the plugin");
            this.getLogger().warning("might not work correctly.");
            this.getLogger().warning("--------------------------------------------------");
         }

         this.registerMetrics();
         this.checkIfPluginVersionUpdated();
         PluginManager pluginManager = Bukkit.getPluginManager();
         this.usingPlotSquared = pluginManager.isPluginEnabled("PlotSquared");
         pluginManager.registerEvents(new PlayerJoinListener(), instance);
         pluginManager.registerEvents(new PlayerQuitListener(), instance);
         pluginManager.registerEvents(new PlayerTeleportListener(), instance);
         pluginManager.registerEvents(new PlayerChangedWorldListener(), instance);
         pluginManager.registerEvents(this.skinManager, instance);
         pluginManager.registerEvents(new PlayerLoadedListener(), this);
         pluginManager.registerEvents(new PlayerUseUnknownEntityListener(), instance);
         if (PLAYER_NPCS_FEATURE_FLAG.isEnabled()) {
            pluginManager.registerEvents(new PlayerNpcsListener(), instance);
         }

         this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
         this.scheduler.runTaskLater(null, 100L, () -> this.npcManager.loadNpcs());
         this.visibilityTracker = new VisibilityTracker();
         this.npcThread.scheduleAtFixedRate(new TurnToPlayerTracker(), 0L, this.config.getTurnToPlayerTrackerInterval() * 50L, TimeUnit.MILLISECONDS);
         this.npcThread.scheduleAtFixedRate(this.visibilityTracker, 0L, this.config.getNpcUpdateVisibilityInterval() * 50L, TimeUnit.MILLISECONDS);
         int autosaveInterval = this.config.getAutoSaveInterval();
         if (this.config.isEnableAutoSave() && this.config.getAutoSaveInterval() > 0) {
            this.scheduler.runTaskTimerAsynchronously(1200L, autosaveInterval * 60L * 20L, () -> this.npcManager.saveNpcs(false));
         }

         int npcUpdateInterval = this.config.getNpcUpdateInterval();
         this.npcThread
            .scheduleAtFixedRate(
               () -> {
                  for (Npc npc : new ArrayList<>(this.npcManager.getAllNpcs())) {
                     try {
                        boolean shouldUpdate = npc.getData().getDisplayName() != null
                           && !npc.getData().getDisplayName().isBlank()
                           && SkinUtils.isPlaceholder(npc.getData().getDisplayName());
                        if (npc.getData().getSkinData() != null) {
                           String skinID = npc.getData().getSkinData().getIdentifier();
                           if (!skinID.isEmpty() && SkinUtils.isPlaceholder(skinID)) {
                              SkinData skinData = this.skinManager.getByIdentifier(skinID, npc.getData().getSkinData().getVariant());
                              skinData.setIdentifier(skinID);
                              npc.getData().setSkinData(skinData);
                              shouldUpdate = true;
                           }
                        }

                        if (shouldUpdate) {
                           npc.removeForAll();
                           npc.create();
                           npc.spawnForAll();
                        }
                     } catch (Throwable var7) {
                        this.fancyLogger.error("An error occurred while updating '" + npc.getData().getName() + "' NPC.", ThrowableProperty.of(var7));
                     }
                  }
               },
               30L,
               npcUpdateInterval,
               TimeUnit.SECONDS
            );
         if (this.config.isRegisterCommands()) {
            this.commandManager = new CloudCommandManager(this, false).registerArguments().registerExceptionHandlers().registerCommands();
         } else {
            this.getLogger()
               .warning(
                  "Commands and related components have not been registered. This can be changed by setting 'register_commands' to true, and restarting the server."
               );
         }

         if (ENABLE_DEBUG_MODE_FEATURE_FLAG.isEnabled() && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderApiEnv.registerPlaceholders();
         }

         this.fancyLogger.info("FancyNpcs (" + this.versionConfig.getVersion() + ") has been enabled.");
      }
   }

   public void onDisable() {
      this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
      if (this.npcManager != null) {
         this.npcManager.saveNpcs(true);
      }

      this.fancyLogger.info("FancyNpcs has been disabled.");
   }

   private void registerMetrics() {
      Metrics metrics = new Metrics(this, 17543);
      metrics.addCustomChart(new Metrics.SingleLineChart("total_npcs", () -> this.npcManager.getAllNpcs().size()));
      metrics.addCustomChart(new Metrics.SimplePie("update_notifications", () -> this.config.isMuteVersionNotification() ? "No" : "Yes"));
      metrics.addCustomChart(new Metrics.SimplePie("using_development_build", () -> this.versionConfig.isDevelopmentBuild() ? "Yes" : "No"));
      this.fancyAnalytics.registerMinecraftPluginMetrics(instance);
      this.fancyAnalytics.getExceptionHandler().registerLogger(this.getLogger());
      this.fancyAnalytics.getExceptionHandler().registerLogger(Bukkit.getLogger());
      this.fancyAnalytics.getExceptionHandler().registerLogger(this.fancyLogger);
      this.fancyAnalytics.registerStringMetric(new MetricSupplier<>("commit_hash", () -> this.versionConfig.getCommitHash().substring(0, 7)));
      this.fancyAnalytics.registerStringMetric(new MetricSupplier<>("server_size", () -> {
         long onlinePlayers = Bukkit.getOnlinePlayers().size();
         if (onlinePlayers == 0L) {
            return "empty";
         } else if (onlinePlayers <= 25L) {
            return "small";
         } else if (onlinePlayers <= 100L) {
            return "medium";
         } else {
            return onlinePlayers <= 500L ? "large" : "very_large";
         }
      }));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_npcs", () -> (double)this.npcManager.getAllNpcs().size()));
      this.fancyAnalytics
         .registerStringMetric(new MetricSupplier<>("enabled_update_notifications", () -> this.config.isMuteVersionNotification() ? "false" : "true"));
      this.fancyAnalytics
         .registerStringMetric(new MetricSupplier<>("enabled_player_npcs_fflag", () -> PLAYER_NPCS_FEATURE_FLAG.isEnabled() ? "true" : "false"));
      this.fancyAnalytics
         .registerStringMetric(new MetricSupplier<>("using_development_build", () -> this.versionConfig.isDevelopmentBuild() ? "true" : "false"));
      this.fancyAnalytics.registerStringMetric(new MetricSupplier<>("language", () -> this.translator.getSelectedLanguage().getLanguageCode()));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("avg_interaction_cooldown", () -> {
         double sum = 0.0;
         int count = 0;

         for (Npc npc : this.npcManager.getAllNpcs()) {
            if (npc.getData().getInteractionCooldown() > 0.0F) {
               sum += npc.getData().getInteractionCooldown();
               count++;
            }
         }

         return count == 0 ? 0.0 : sum / count;
      }));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_npcs_interaction_cooldown_longer_than_5min", () -> {
         long count = this.npcManager.getAllNpcs().stream().filter(npc -> npc.getData().getInteractionCooldown() > 300.0F).count();
         return (double)count;
      }));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_non_persistent_npcs", () -> {
         long count = this.npcManager.getAllNpcs().stream().filter(npc -> !npc.isSaveToFile()).count();
         return (double)count;
      }));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_not_player_npcs", () -> {
         long count = this.npcManager.getAllNpcs().stream().filter(npc -> npc.getData().getType() != EntityType.PLAYER).count();
         return (double)count;
      }));
      this.fancyAnalytics
         .registerStringArrayMetric(
            new MetricSupplier<>("npc_type", () -> this.npcManager.getAllNpcs().stream().map(npc -> npc.getData().getType().name()).toArray(String[]::new))
         );
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_npcs_having_attributes", () -> {
         long count = this.npcManager.getAllNpcs().stream().filter(npc -> !npc.getData().getAttributes().isEmpty()).count();
         return (double)count;
      }));
      this.fancyAnalytics.registerNumberMetric(new MetricSupplier<>("amount_npc_actions", () -> {
         long count = 0L;

         for (Npc npc : this.npcManager.getAllNpcs()) {
            count += npc.getData().getActions().size();
         }

         return (double)count;
      }));
      this.fancyAnalytics.initialize();
   }

   private void checkIfPluginVersionUpdated() {
      String currentVersion = this.versionConfig.getVersion();
      String lastVersion = "N/A";
      File versionFile = new File(this.getDataFolder(), "version.yml");
      if (!versionFile.exists()) {
         try {
            Files.write(versionFile.toPath(), currentVersion.getBytes());
         } catch (IOException var7) {
            this.fancyLogger.warn("Could not write version file.");
            return;
         }
      } else {
         try {
            lastVersion = new String(Files.readAllBytes(versionFile.toPath()));
         } catch (IOException var6) {
            this.fancyLogger.warn("Could not read version file.");
            return;
         }
      }

      if (!lastVersion.equals(currentVersion)) {
         this.fancyLogger.info("Plugin has been updated from version " + lastVersion + " to " + currentVersion + ".");
         this.fancyAnalytics
            .sendEvent(
               new Event("PluginVersionUpdated", new HashMap<>())
                  .withProperty("from", lastVersion)
                  .withProperty("to", currentVersion)
                  .withProperty("commit_hash", this.versionConfig.getCommitHash())
                  .withProperty("channel", this.versionConfig.getChannel())
                  .withProperty("platform", this.versionConfig.getPlatform())
            );

         try {
            Files.write(versionFile.toPath(), currentVersion.getBytes());
         } catch (IOException var5) {
            this.fancyLogger.warn("Could not write version file.");
         }
      }
   }

   @Override
   public Thread newThread(String name, Runnable runnable) {
      return USE_NATIVE_THREADS_FEATURE_FLAG.isEnabled() ? new Thread(runnable, name) : Thread.ofVirtual().name(name).unstarted(runnable);
   }

   @Override
   public ExtendedFancyLogger getFancyLogger() {
      return this.fancyLogger;
   }

   @Override
   public ScheduledExecutorService getNpcThread() {
      return this.npcThread;
   }

   @Override
   public Function<NpcData, Npc> getNpcAdapter() {
      return this.npcAdapter;
   }

   @Override
   public FancyScheduler getScheduler() {
      return this.scheduler;
   }

   public NpcManagerImpl getNpcManagerImpl() {
      return this.npcManager;
   }

   @Override
   public NpcManager getNpcManager() {
      return this.npcManager;
   }

   public AttributeManagerImpl getAttributeManager() {
      return this.attributeManager;
   }

   @Override
   public SkinManager getSkinManager() {
      return this.skinManager;
   }

   public SkinManagerImpl getSkinManagerImpl() {
      return this.skinManager;
   }

   public ActionManagerImpl getActionManager() {
      return this.actionManager;
   }

   public FancyNpcsConfigImpl getFancyNpcConfig() {
      return this.config;
   }

   public VersionConfig getVersionConfig() {
      return this.versionConfig;
   }

   public CloudCommandManager getCommandManager() {
      return this.commandManager;
   }

   @Override
   public Translator getTranslator() {
      return this.translator;
   }

   @Override
   public void registerCommand(Object command) {
      if (this.commandManager != null) {
         this.commandManager.getAnnotationParser().parse(command);
      }
   }

   public TextConfig getTextConfig() {
      return this.textConfig;
   }

   @Override
   public FeatureFlagConfig getFeatureFlagConfig() {
      return this.featureFlagConfig;
   }

   public VersionFetcher getVersionFetcher() {
      return this.versionFetcher;
   }

   public FancyAnalyticsAPI getFancyAnalytics() {
      return this.fancyAnalytics;
   }

   public VisibilityTracker getVisibilityTracker() {
      return this.visibilityTracker;
   }

   public boolean isUsingPlotSquared() {
      return this.usingPlotSquared;
   }

   @Override
   public JavaPlugin getPlugin() {
      return instance;
   }
}
