package ac.grim.grimac.platform.bukkit;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.GrimExternalAPI;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.event.events.CommandExecuteEvent;
import ac.grim.grimac.api.event.events.CompletePredictionEvent;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.event.events.GrimJoinEvent;
import ac.grim.grimac.api.event.events.GrimQuitEvent;
import ac.grim.grimac.api.event.events.GrimReloadEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.command.CloudCommandService;
import ac.grim.grimac.internal.platform.bukkit.resolver.BukkitResolverRegistrar;
import ac.grim.grimac.manager.init.Initable;
import ac.grim.grimac.manager.init.start.ExemptOnlinePlayersOnReload;
import ac.grim.grimac.manager.init.start.StartableInitable;
import ac.grim.grimac.platform.api.Platform;
import ac.grim.grimac.platform.api.PlatformLoader;
import ac.grim.grimac.platform.api.PlatformServer;
import ac.grim.grimac.platform.api.command.CommandService;
import ac.grim.grimac.platform.api.manager.ItemResetHandler;
import ac.grim.grimac.platform.api.manager.MessagePlaceHolderManager;
import ac.grim.grimac.platform.api.manager.PlatformPluginManager;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.player.PlatformPlayerFactory;
import ac.grim.grimac.platform.api.scheduler.PlatformScheduler;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.api.sender.SenderFactory;
import ac.grim.grimac.platform.bukkit.initables.BukkitBStats;
import ac.grim.grimac.platform.bukkit.initables.BukkitEventManager;
import ac.grim.grimac.platform.bukkit.initables.BukkitLuckPermsInitable;
import ac.grim.grimac.platform.bukkit.initables.BukkitTickEndEvent;
import ac.grim.grimac.platform.bukkit.manager.BukkitCloudPlatformCommandArguments;
import ac.grim.grimac.platform.bukkit.manager.BukkitItemResetHandler;
import ac.grim.grimac.platform.bukkit.manager.BukkitMessagePlaceHolderManager;
import ac.grim.grimac.platform.bukkit.manager.BukkitPermissionRegistrationManager;
import ac.grim.grimac.platform.bukkit.manager.BukkitPlatformPluginManager;
import ac.grim.grimac.platform.bukkit.player.BukkitPlatformPlayerFactory;
import ac.grim.grimac.platform.bukkit.scheduler.bukkit.BukkitPlatformScheduler;
import ac.grim.grimac.platform.bukkit.scheduler.folia.FoliaPlatformScheduler;
import ac.grim.grimac.platform.bukkit.sender.BukkitSenderFactory;
import ac.grim.grimac.platform.bukkit.utils.placeholder.PlaceholderAPIExpansion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.brigadier.BrigadierSetting;
import ac.grim.grimac.shaded.incendo.cloud.brigadier.CloudBrigadierManager;
import ac.grim.grimac.shaded.incendo.cloud.bukkit.CloudBukkitCapabilities;
import ac.grim.grimac.shaded.incendo.cloud.execution.ExecutionCoordinator;
import ac.grim.grimac.shaded.incendo.cloud.paper.LegacyPaperCommandManager;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.lazy.LazyHolder;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrimACBukkitLoaderPlugin extends JavaPlugin implements PlatformLoader {
   public static GrimACBukkitLoaderPlugin LOADER;
   private final LazyHolder<PlatformScheduler> scheduler = LazyHolder.simple(this::createScheduler);
   private final LazyHolder<PacketEventsAPI<?>> packetEvents = LazyHolder.simple(() -> SpigotPacketEventsBuilder.build(this));
   private final LazyHolder<BukkitSenderFactory> senderFactory = LazyHolder.simple(BukkitSenderFactory::new);
   private final LazyHolder<ItemResetHandler> itemResetHandler = LazyHolder.simple(BukkitItemResetHandler::new);
   private final LazyHolder<CommandService> commandService = LazyHolder.simple(this::createCommandService);
   private final CloudPlatformCommandArguments commandArguments = new BukkitCloudPlatformCommandArguments();
   private final PlatformPlayerFactory platformPlayerFactory = new BukkitPlatformPlayerFactory();
   private final PlatformPluginManager pluginManager = new BukkitPlatformPluginManager();
   private final GrimPlugin plugin;
   private final PlatformServer platformServer = new BukkitPlatformServer();
   private final MessagePlaceHolderManager messagePlaceHolderManager = new BukkitMessagePlaceHolderManager();
   private final BukkitPermissionRegistrationManager permissionManager = new BukkitPermissionRegistrationManager();

   public GrimACBukkitLoaderPlugin() {
      BukkitResolverRegistrar registrar = new BukkitResolverRegistrar();
      registrar.registerAll(GrimAPI.INSTANCE.getExtensionManager());
      this.plugin = registrar.resolvePlugin(this);
   }

   public void onLoad() {
      LOADER = this;
      GrimAPI.INSTANCE.load(this, this.getBukkitInitTasks());
   }

   private Initable[] getBukkitInitTasks() {
      return new Initable[]{
         new ExemptOnlinePlayersOnReload(),
         new BukkitEventManager(),
         new BukkitTickEndEvent(),
         new BukkitBStats(),
         new BukkitLuckPermsInitable(),
         (StartableInitable)() -> {
            if (BukkitMessagePlaceHolderManager.hasPlaceholderAPI) {
               new PlaceholderAPIExpansion().register();
            }
         }
      };
   }

   public void onEnable() {
      GrimAPI.INSTANCE.start();
   }

   public void onDisable() {
      GrimAPI.INSTANCE.stop();
   }

   @Override
   public PlatformScheduler getScheduler() {
      return this.scheduler.get();
   }

   @Override
   public PacketEventsAPI<?> getPacketEvents() {
      return this.packetEvents.get();
   }

   @Override
   public ItemResetHandler getItemResetHandler() {
      return this.itemResetHandler.get();
   }

   @Override
   public CommandService getCommandService() {
      return this.commandService.get();
   }

   @Override
   public SenderFactory<CommandSender> getSenderFactory() {
      return this.senderFactory.get();
   }

   @Override
   public void registerAPIService() {
      GrimExternalAPI externalAPI = GrimAPI.INSTANCE.getExternalAPI();
      EventBus eventBus = externalAPI.getEventBus();
      GrimPlugin plugin = GrimAPI.INSTANCE.getGrimPlugin();
      ((GrimJoinEvent.Channel)eventBus.get(GrimJoinEvent.class))
         .onJoin(plugin, user -> Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimJoinEvent(user)));
      ((GrimQuitEvent.Channel)eventBus.get(GrimQuitEvent.class))
         .onQuit(plugin, user -> Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimQuitEvent(user)));
      ((GrimReloadEvent.Channel)eventBus.get(GrimReloadEvent.class))
         .onReload(plugin, success -> Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimReloadEvent(success)));
      eventBus.subscribe(plugin, FlagEvent.class, event -> {
         ac.grim.grimac.api.events.FlagEvent bukkitEvent = new ac.grim.grimac.api.events.FlagEvent(event.getUser(), event.getCheck(), event::getVerbose);
         Bukkit.getPluginManager().callEvent(bukkitEvent);
         event.setCancelled(event.isCancelled() || bukkitEvent.isCancelled());
      }, 0, false, GrimACBukkitLoaderPlugin.class);
      ((CommandExecuteEvent.Channel)eventBus.get(CommandExecuteEvent.class)).onCommandExecute(plugin, (user, check, verbose, command, cancelled) -> {
         ac.grim.grimac.api.events.CommandExecuteEvent bukkitEvent = new ac.grim.grimac.api.events.CommandExecuteEvent(user, check, verbose, command);
         Bukkit.getPluginManager().callEvent(bukkitEvent);
         return cancelled || bukkitEvent.isCancelled();
      });
      ((CompletePredictionEvent.Channel)eventBus.get(CompletePredictionEvent.class)).onCompletePrediction(plugin, (user, check, offset, cancelled) -> {
         ac.grim.grimac.api.events.CompletePredictionEvent bukkitEvent = new ac.grim.grimac.api.events.CompletePredictionEvent(user, check, "", offset);
         Bukkit.getPluginManager().callEvent(bukkitEvent);
         return cancelled || bukkitEvent.isCancelled();
      });
      GrimAPIProvider.init(externalAPI);
      Bukkit.getServicesManager().register(GrimAbstractAPI.class, externalAPI, this, ServicePriority.Normal);
   }

   private PlatformScheduler createScheduler() {
      return (PlatformScheduler)(GrimAPI.INSTANCE.getPlatform() == Platform.FOLIA ? new FoliaPlatformScheduler() : new BukkitPlatformScheduler());
   }

   private CommandService createCommandService() {
      try {
         return new CloudCommandService(this::createCloudCommandManager, this.commandArguments);
      } catch (Throwable var2) {
         LogUtil.warn("CRITICAL: Failed to initialize Command Framework. Grim will continue to run with no commands.", var2);
         return () -> {};
      }
   }

   private CommandManager<Sender> createCloudCommandManager() {
      LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(this, ExecutionCoordinator.simpleCoordinator(), this.senderFactory.get());
      if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         try {
            manager.registerBrigadier();
            CloudBrigadierManager<Sender, ?> cbm = manager.brigadierManager();
            cbm.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
         } catch (Throwable var3) {
            LogUtil.error("Failed to register Brigadier native completions. Falling back to standard completions.", var3);
         }
      } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         manager.registerAsynchronousCompletions();
      }

      return manager;
   }

   public BukkitSenderFactory getBukkitSenderFactory() {
      return this.senderFactory.get();
   }

   @Generated
   @Override
   public PlatformPlayerFactory getPlatformPlayerFactory() {
      return this.platformPlayerFactory;
   }

   @Generated
   @Override
   public PlatformPluginManager getPluginManager() {
      return this.pluginManager;
   }

   @Generated
   @Override
   public GrimPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   @Override
   public PlatformServer getPlatformServer() {
      return this.platformServer;
   }

   @Generated
   @Override
   public MessagePlaceHolderManager getMessagePlaceHolderManager() {
      return this.messagePlaceHolderManager;
   }

   @Generated
   public BukkitPermissionRegistrationManager getPermissionManager() {
      return this.permissionManager;
   }
}
