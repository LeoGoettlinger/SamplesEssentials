package com.viaversion.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaManager;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.api.scheduler.Scheduler;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.configuration.ConfigurationProviderImpl;
import com.viaversion.viaversion.connection.ConnectionManagerImpl;
import com.viaversion.viaversion.debug.DebugHandlerImpl;
import com.viaversion.viaversion.protocol.ProtocolManagerImpl;
import com.viaversion.viaversion.protocol.ServerProtocolVersionRange;
import com.viaversion.viaversion.protocol.ServerProtocolVersionSingleton;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.task.TabCompleteTask;
import com.viaversion.viaversion.protocols.v1_8to1_9.task.IdlePacketTask;
import com.viaversion.viaversion.scheduler.TaskScheduler;
import com.viaversion.viaversion.update.UpdateUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;

public class ViaManagerImpl implements ViaManager {
   private final ProtocolManagerImpl protocolManager = new ProtocolManagerImpl();
   private final ConnectionManager connectionManager = new ConnectionManagerImpl();
   private final ConfigurationProvider configurationProvider = new ConfigurationProviderImpl();
   private final DebugHandler debugHandler = new DebugHandlerImpl();
   private final ViaProviders providers = new ViaProviders();
   private final Scheduler scheduler = new TaskScheduler();
   private final ViaPlatform<?> platform;
   private final ViaInjector injector;
   private final ViaCommandHandler commandHandler;
   private final ViaPlatformLoader loader;
   private final Set<String> subPlatforms = new HashSet<>();
   private List<Runnable> enableListeners = new ArrayList<>();
   private List<Runnable> postEnableListeners = new ArrayList<>();
   private PlatformTask<?> mappingLoadingTask;
   private boolean initialized;

   public ViaManagerImpl(ViaPlatform<?> platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader) {
      this.platform = platform;
      this.injector = injector;
      this.commandHandler = commandHandler;
      this.loader = loader;
   }

   public static ViaManager initAndLoad(
      ViaPlatform<?> platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader, Runnable... enableListeners
   ) {
      ViaManagerImpl manager = new ViaManagerImpl(platform, injector, commandHandler, loader);
      Via.init(manager);
      platform.getConf().reload();

      for (Runnable listener : enableListeners) {
         manager.addEnableListener(listener);
      }

      manager.init();
      manager.onServerLoaded();
      return manager;
   }

   public static ViaManagerImpl.ViaManagerBuilder builder() {
      return new ViaManagerImpl.ViaManagerBuilder();
   }

   public void init() {
      this.configurationProvider.register(this.platform.getConf());
      if (System.getProperty("ViaVersion") != null) {
         this.platform.onReload();
      }

      if (!this.injector.lateProtocolVersionSetting()) {
         this.loadServerProtocol();
      }

      MappingDataLoader.loadGlobalIdentifiers();
      this.protocolManager.registerProtocols();

      try {
         this.injector.inject();
      } catch (Exception var3) {
         this.platform.getLogger().log(Level.SEVERE, "ViaVersion failed to inject:", (Throwable)var3);
         return;
      }

      System.setProperty("ViaVersion", this.platform.getPluginVersion());

      for (Runnable listener : this.enableListeners) {
         listener.run();
      }

      this.enableListeners = null;
      this.initialized = true;
   }

   public void onServerLoaded() {
      if (this.platform.getConf().isCheckForUpdates()) {
         UpdateUtil.sendUpdateMessage();
      }

      if (!this.protocolManager.getServerProtocolVersion().isKnown()) {
         this.loadServerProtocol();
      }

      ServerProtocolVersion protocolVersion = this.protocolManager.getServerProtocolVersion();
      if (protocolVersion.isKnown()) {
         if (this.platform.isProxy()) {
            this.platform.getLogger().info("ViaVersion detected lowest supported version by the proxy: " + protocolVersion.lowestSupportedProtocolVersion());
            this.platform.getLogger().info("Highest supported version by the proxy: " + protocolVersion.highestSupportedProtocolVersion());
            if (this.debugHandler.enabled()) {
               this.platform
                  .getLogger()
                  .info("Supported version range: " + Arrays.toString(protocolVersion.supportedProtocolVersions().toArray(new ProtocolVersion[0])));
            }
         } else {
            this.platform.getLogger().info("ViaVersion detected server version: " + protocolVersion.highestSupportedProtocolVersion());
         }

         if (!this.protocolManager.isWorkingPipe()) {
            this.platform.getLogger().warning("ViaVersion does not have any compatible versions for this server version!");
            this.platform
               .getLogger()
               .warning("ViaVersion only supports newer client versions. Use ViaBackwards to allow older versions (ViaRewind for 1.7/1.8) to join.");
            this.platform
               .getLogger()
               .warning("Get setup help at https://viaversion.com/setup or download ViaBackwards/ViaRewind directly at https://ci.viaversion.com/");
            this.platform.getLogger().warning("Need more help? Join our Discord at https://viaversion.com/discord");
         } else if (protocolVersion.highestSupportedProtocolVersion().olderThan(ProtocolVersion.v1_16)) {
            this.platform
               .getLogger()
               .warning(
                  "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run Via on this Minecraft version, but we will prioritize issues with legacy Minecraft versions less. Please consider updating to give your players a better experience and to avoid issues that have long been fixed."
               );
         }
      }

      this.checkJavaVersion();
      this.unsupportedSoftwareWarning();
      this.loader.load();
      this.mappingLoadingTask = Via.getPlatform().runRepeatingAsync(() -> {
         if (this.protocolManager.checkForMappingCompletion() && this.mappingLoadingTask != null) {
            this.mappingLoadingTask.cancel();
            this.mappingLoadingTask = null;
         }
      }, 10L);
      ProtocolVersion serverProtocolVersion = this.protocolManager.getServerProtocolVersion().lowestSupportedProtocolVersion();
      if (serverProtocolVersion.olderThan(ProtocolVersion.v1_9) && Via.getConfig().isSimulatePlayerTick()) {
         Via.getPlatform().runRepeatingSync(new IdlePacketTask(), 1L);
      }

      if (serverProtocolVersion.olderThan(ProtocolVersion.v1_13) && Via.getConfig().get1_13TabCompleteDelay() > 0) {
         Via.getPlatform().runRepeatingSync(new TabCompleteTask(), 1L);
      }

      this.protocolManager.refreshVersions();

      for (Runnable listener : this.postEnableListeners) {
         listener.run();
      }

      this.postEnableListeners = null;
   }

   private void loadServerProtocol() {
      try {
         ProtocolVersion serverProtocolVersion = this.injector.getServerProtocolVersion();
         ServerProtocolVersion versionInfo;
         if (this.platform.isProxy()) {
            SortedSet<ProtocolVersion> supportedVersions = this.injector.getServerProtocolVersions();
            versionInfo = new ServerProtocolVersionRange(supportedVersions.first(), supportedVersions.last(), supportedVersions);
         } else {
            versionInfo = new ServerProtocolVersionSingleton(serverProtocolVersion);
         }

         this.protocolManager.setServerProtocol(versionInfo);
      } catch (Exception var4) {
         this.platform.getLogger().log(Level.SEVERE, "ViaVersion failed to get the server protocol!", (Throwable)var4);
      }
   }

   public void destroy() {
      if (this.platform.couldBeReloading()) {
         this.platform.getLogger().info("ViaVersion is disabling. If this is a reload and you experience issues, please reboot instead.");
      }

      try {
         this.injector.uninject();
      } catch (Exception var2) {
         this.platform.getLogger().log(Level.SEVERE, "ViaVersion failed to uninject:", (Throwable)var2);
      }

      this.loader.unload();
      this.scheduler.shutdown();
      this.platform.getLogger().info("ViaVersion has been disabled; uninjected the platform and shut down the scheduler.");
   }

   private void checkJavaVersion() {
      int javaVersion = Runtime.version().feature();
      if (javaVersion < 17) {
         this.platform
            .getLogger()
            .warning("You are running an outdated Java version, please update it to at least Java 21 (your version is " + javaVersion + ").");
         this.platform.getLogger().warning("ViaVersion no longer officially supports this version of Java, only offering unsupported compatibility builds.");
         this.platform.getLogger().warning("See https://github.com/ViaVersion/ViaVersion/releases/tag/5.0.0 for more information.");
      } else if (javaVersion < 21) {
         this.platform.getLogger().warning("Please update your Java runtime to at least Java 21 (your version is " + javaVersion + ").");
         this.platform.getLogger().warning("At some point in the future, ViaVersion will no longer be compatible with this version of Java.");
      }
   }

   private void unsupportedSoftwareWarning() {
      boolean found = false;

      for (UnsupportedSoftware software : this.platform.getUnsupportedSoftwareClasses()) {
         String match = software.match();
         if (match != null) {
            if (!found) {
               this.platform.getLogger().severe("************************************************");
               this.platform.getLogger().severe("You are using unsupported software and may encounter unforeseeable issues.");
               this.platform.getLogger().severe("");
               found = true;
            }

            this.platform.getLogger().severe("We strongly advise against using " + match + ":");
            this.platform.getLogger().severe(software.getReason());
            this.platform.getLogger().severe("");
         }
      }

      if (found) {
         this.platform.getLogger().severe("We will not provide support in case you encounter issues possibly related to this software.");
         this.platform.getLogger().severe("************************************************");
      }
   }

   @Override
   public ViaPlatform<?> getPlatform() {
      return this.platform;
   }

   @Override
   public ConnectionManager getConnectionManager() {
      return this.connectionManager;
   }

   @Override
   public ProtocolManager getProtocolManager() {
      return this.protocolManager;
   }

   @Override
   public ViaProviders getProviders() {
      return this.providers;
   }

   @Override
   public DebugHandler debugHandler() {
      return this.debugHandler;
   }

   @Override
   public ViaInjector getInjector() {
      return this.injector;
   }

   public ViaCommandHandler getCommandHandler() {
      return this.commandHandler;
   }

   @Override
   public ViaPlatformLoader getLoader() {
      return this.loader;
   }

   @Override
   public Scheduler getScheduler() {
      return this.scheduler;
   }

   @Override
   public ConfigurationProvider getConfigurationProvider() {
      return this.configurationProvider;
   }

   @Override
   public Set<String> getSubPlatforms() {
      return this.subPlatforms;
   }

   @Override
   public void addEnableListener(Runnable runnable) {
      this.enableListeners.add(runnable);
   }

   @Override
   public void addPostEnableListener(Runnable runnable) {
      this.postEnableListeners.add(runnable);
   }

   @Override
   public boolean isInitialized() {
      return this.initialized;
   }

   public static final class ViaManagerBuilder {
      private ViaPlatformLoader loader = ViaPlatformLoader.NOOP;
      private ViaPlatform<?> platform;
      private ViaInjector injector;
      private ViaCommandHandler commandHandler;

      public ViaManagerImpl.ViaManagerBuilder platform(ViaPlatform<?> platform) {
         this.platform = platform;
         return this;
      }

      public ViaManagerImpl.ViaManagerBuilder injector(ViaInjector injector) {
         this.injector = injector;
         return this;
      }

      public ViaManagerImpl.ViaManagerBuilder loader(ViaPlatformLoader loader) {
         this.loader = loader;
         return this;
      }

      public ViaManagerImpl.ViaManagerBuilder commandHandler(ViaCommandHandler commandHandler) {
         this.commandHandler = commandHandler;
         return this;
      }

      public ViaManagerImpl build() {
         return new ViaManagerImpl(this.platform, this.injector, this.commandHandler, this.loader);
      }
   }
}
