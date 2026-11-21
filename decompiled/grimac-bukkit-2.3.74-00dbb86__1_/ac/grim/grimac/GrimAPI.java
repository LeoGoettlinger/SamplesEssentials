package ac.grim.grimac;

import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.internal.event.OptimizedEventBus;
import ac.grim.grimac.internal.plugin.resolver.GrimExtensionManager;
import ac.grim.grimac.internal.storage.backend.BackendRegistryImpl;
import ac.grim.grimac.internal.storage.backend.memory.InMemoryBackendProvider;
import ac.grim.grimac.internal.storage.backend.mongo.MongoBackendProvider;
import ac.grim.grimac.internal.storage.backend.mysql.MysqlBackendProvider;
import ac.grim.grimac.internal.storage.backend.postgres.PostgresBackendProvider;
import ac.grim.grimac.internal.storage.backend.redis.RedisBackendProvider;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackendProvider;
import ac.grim.grimac.manager.AlertManagerImpl;
import ac.grim.grimac.manager.DiscordManager;
import ac.grim.grimac.manager.InitManager;
import ac.grim.grimac.manager.SpectateManager;
import ac.grim.grimac.manager.TickManager;
import ac.grim.grimac.manager.config.BaseConfigManager;
import ac.grim.grimac.manager.datastore.DataStoreLifecycle;
import ac.grim.grimac.manager.init.Initable;
import ac.grim.grimac.platform.api.Platform;
import ac.grim.grimac.platform.api.PlatformLoader;
import ac.grim.grimac.platform.api.PlatformServer;
import ac.grim.grimac.platform.api.command.CommandService;
import ac.grim.grimac.platform.api.manager.ItemResetHandler;
import ac.grim.grimac.platform.api.manager.MessagePlaceHolderManager;
import ac.grim.grimac.platform.api.manager.PermissionRegistrationManager;
import ac.grim.grimac.platform.api.manager.PlatformPluginManager;
import ac.grim.grimac.platform.api.player.PlatformPlayerFactory;
import ac.grim.grimac.platform.api.scheduler.PlatformScheduler;
import ac.grim.grimac.platform.api.sender.SenderFactory;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.anticheat.PlayerDataManager;
import ac.grim.grimac.utils.common.arguments.CommonGrimArguments;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import lombok.Generated;

public final class GrimAPI {
   private int permissionNodes;
   public static final GrimAPI INSTANCE = new GrimAPI();
   private final Platform platform = detectPlatform();
   private final BaseConfigManager configManager;
   private final AlertManagerImpl alertManager;
   private final SpectateManager spectateManager;
   private final DiscordManager discordManager;
   private final PlayerDataManager playerDataManager;
   private final TickManager tickManager;
   private final GrimExtensionManager extensionManager;
   private final EventBus eventBus;
   private final GrimExternalAPI externalAPI;
   private DataStoreLifecycle dataStoreLifecycle;
   private final BackendRegistry backendRegistry = buildBackendRegistry();
   private PlatformLoader loader;
   private InitManager initManager;
   private boolean initialized = false;

   private GrimAPI() {
      this.configManager = new BaseConfigManager();
      this.alertManager = new AlertManagerImpl();
      this.spectateManager = new SpectateManager();
      this.discordManager = new DiscordManager();
      this.playerDataManager = new PlayerDataManager();
      this.tickManager = new TickManager();
      this.extensionManager = new GrimExtensionManager();
      this.eventBus = new OptimizedEventBus(this.extensionManager);
      this.externalAPI = new GrimExternalAPI(this);
   }

   private static Platform detectPlatform() {
      Platform override = CommonGrimArguments.PLATFORM_OVERRIDE.value();
      if (override != null) {
         return override;
      } else if (ReflectionUtils.hasClass("io.papermc.paper.threadedregions.RegionizedServer")) {
         return Platform.FOLIA;
      } else if (ReflectionUtils.hasClass("org.bukkit.Bukkit")) {
         return Platform.BUKKIT;
      } else if (ReflectionUtils.hasClass("net.fabricmc.loader.api.FabricLoader")) {
         return Platform.FABRIC;
      } else {
         throw new IllegalStateException("Unknown platform!");
      }
   }

   public void load(PlatformLoader platformLoader, Initable... platformSpecificInitables) {
      this.loader = platformLoader;
      this.dataStoreLifecycle = new DataStoreLifecycle(this.getGrimPlugin(), this.backendRegistry);
      this.initManager = new InitManager(this.loader.getPacketEvents(), platformSpecificInitables);
      this.initManager.load();
      this.initialized = true;
   }

   private static BackendRegistry buildBackendRegistry() {
      BackendRegistryImpl registry = new BackendRegistryImpl();
      registry.register(new SqliteBackendProvider());
      registry.register(new InMemoryBackendProvider());
      registry.register(new MysqlBackendProvider());
      registry.register(new PostgresBackendProvider());
      registry.register(new MongoBackendProvider());
      registry.register(new RedisBackendProvider());
      return registry;
   }

   public void start() {
      this.checkInitialized();
      this.initManager.start();
   }

   public void stop() {
      this.checkInitialized();
      this.initManager.stop();
   }

   public PlatformScheduler getScheduler() {
      return this.loader.getScheduler();
   }

   public PlatformPlayerFactory getPlatformPlayerFactory() {
      return this.loader.getPlatformPlayerFactory();
   }

   public GrimPlugin getGrimPlugin() {
      return this.loader.getPlugin();
   }

   public SenderFactory<?> getSenderFactory() {
      return this.loader.getSenderFactory();
   }

   public ItemResetHandler getItemResetHandler() {
      return this.loader.getItemResetHandler();
   }

   public PlatformPluginManager getPluginManager() {
      return this.loader.getPluginManager();
   }

   public PlatformServer getPlatformServer() {
      return this.loader.getPlatformServer();
   }

   @NotNull
   public MessagePlaceHolderManager getMessagePlaceHolderManager() {
      return this.loader.getMessagePlaceHolderManager();
   }

   public CommandService getCommandService() {
      return this.loader.getCommandService();
   }

   private void checkInitialized() {
      this.permissionNodes = this.loadPermissionNodes();
      if (!this.initialized) {
         throw new IllegalStateException("GrimAPI has not been initialized!");
      }
   }

   public PermissionRegistrationManager getPermissionManager() {
      return this.loader.getPermissionManager();
   }

   @Generated
   public Platform getPlatform() {
      return this.platform;
   }

   @Generated
   public BaseConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public AlertManagerImpl getAlertManager() {
      return this.alertManager;
   }

   @Generated
   public SpectateManager getSpectateManager() {
      return this.spectateManager;
   }

   @Generated
   public DiscordManager getDiscordManager() {
      return this.discordManager;
   }

   @Generated
   public PlayerDataManager getPlayerDataManager() {
      return this.playerDataManager;
   }

   @Generated
   public TickManager getTickManager() {
      return this.tickManager;
   }

   @Generated
   public GrimExtensionManager getExtensionManager() {
      return this.extensionManager;
   }

   @Generated
   public EventBus getEventBus() {
      return this.eventBus;
   }

   @Generated
   public GrimExternalAPI getExternalAPI() {
      return this.externalAPI;
   }

   @Generated
   public DataStoreLifecycle getDataStoreLifecycle() {
      return this.dataStoreLifecycle;
   }

   @Generated
   public BackendRegistry getBackendRegistry() {
      return this.backendRegistry;
   }

   @Generated
   public PlatformLoader getLoader() {
      return this.loader;
   }

   @Generated
   public InitManager getInitManager() {
      return this.initManager;
   }

   @Generated
   public boolean isInitialized() {
      return this.initialized;
   }
}
