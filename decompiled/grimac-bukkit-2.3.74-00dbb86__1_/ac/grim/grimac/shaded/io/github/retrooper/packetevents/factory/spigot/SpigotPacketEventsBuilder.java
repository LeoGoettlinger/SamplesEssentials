package ac.grim.grimac.shaded.io.github.retrooper.packetevents.factory.spigot;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.injector.ChannelInjector;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.player.PlayerManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.NettyManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.settings.PacketEventsSettings;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.PEVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.updatechecker.UpdateChecker;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bstats.bukkit.Metrics;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bstats.charts.SimplePie;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bukkit.InternalBukkitListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bukkit.InternalBukkitLoginListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bukkit.InternalGlobalBukkitListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bukkit.InternalPaperJoinListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.bukkit.InternalPaperListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.injector.connection.ServerConnectionInitializer;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.manager.InternalBukkitPacketListener;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.manager.player.PlayerManagerImpl;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.manager.protocol.ProtocolManagerImpl;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.manager.server.ServerManagerImpl;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.netty.NettyManagerImpl;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.BukkitLogManager;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.protocolsupport.ProtocolSupportUtil;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.viaversion.CustomPipelineUtil;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SpigotPacketEventsBuilder {
   private static PacketEventsAPI<Plugin> API_INSTANCE;

   public static void clearBuildCache() {
      API_INSTANCE = null;
   }

   public static PacketEventsAPI<Plugin> build(Plugin plugin) {
      if (API_INSTANCE == null) {
         API_INSTANCE = buildNoCache(plugin);
      }

      return API_INSTANCE;
   }

   public static PacketEventsAPI<Plugin> build(Plugin plugin, PacketEventsSettings settings) {
      if (API_INSTANCE == null) {
         API_INSTANCE = buildNoCache(plugin, settings);
      }

      return API_INSTANCE;
   }

   public static PacketEventsAPI<Plugin> buildNoCache(Plugin plugin) {
      return buildNoCache(plugin, new PacketEventsSettings());
   }

   public static PacketEventsAPI<Plugin> buildNoCache(Plugin plugin, PacketEventsSettings inSettings) {
      return new PacketEventsAPI<Plugin>() {
         private final PacketEventsSettings settings = inSettings;
         private final ProtocolManager protocolManager = new ProtocolManagerImpl();
         private final ServerManager serverManager = new ServerManagerImpl();
         private final PlayerManager playerManager = new PlayerManagerImpl();
         private final NettyManager nettyManager = new NettyManagerImpl();
         private final SpigotChannelInjector injector = new SpigotChannelInjector();
         private final LogManager logManager = new BukkitLogManager(this);
         private boolean loaded;
         private boolean initialized;
         private boolean lateBind = false;
         private boolean terminated = false;

         @Override
         public void load() {
            if (!this.loaded) {
               String id = plugin.getName().toLowerCase(Locale.ROOT);
               PacketEvents.IDENTIFIER = "pe-" + id;
               PacketEvents.ENCODER_NAME = "pe-encoder-" + id;
               PacketEvents.DECODER_NAME = "pe-decoder-" + id;
               PacketEvents.CONNECTION_HANDLER_NAME = "pe-connection-handler-" + id;
               PacketEvents.SERVER_CHANNEL_HANDLER_NAME = "pe-connection-initializer-" + id;
               PacketEvents.TIMEOUT_HANDLER_NAME = "pe-timeout-handler-" + id;

               try {
                  SpigotReflectionUtil.init();
                  CustomPipelineUtil.init();
               } catch (Exception var3) {
                  throw new IllegalStateException(var3);
               }

               super.load();
               this.lateBind = !this.injector.isServerBound();
               if (!this.lateBind) {
                  this.injector.inject();
               }

               this.loaded = true;
               this.getLogManager().info("Loaded packetevents v2.13.1+8187e23-SNAPSHOT" + ("packetevents".equals(id) ? "" : " for " + plugin.getName()));
            }
         }

         @Override
         protected void registerInternalListener() {
            this.getEventManager().registerListener(new InternalBukkitPacketListener());
            this.getEventManager().registerListener(new InternalBukkitPacketListener(PacketListenerPriority.LOWEST, true));
         }

         @Override
         public boolean isLoaded() {
            return this.loaded;
         }

         private String getMinecraftVersion() {
            try {
               return Bukkit.getMinecraftVersion();
            } catch (NoSuchMethodError var4) {
               String bukkitVersion = Bukkit.getBukkitVersion();
               int dashIndex = bukkitVersion.indexOf(45);
               return dashIndex != -1 ? bukkitVersion.substring(0, dashIndex) : bukkitVersion;
            }
         }

         @Override
         public void init() {
            this.load();
            if (!this.initialized) {
               Plugin pluginx = (Plugin)PacketEvents.getAPI().getPlugin();
               String minecraftVersion = this.getMinecraftVersion();
               AtomicBoolean stopping = new AtomicBoolean(false);
               BiConsumer<PEVersion, UpdateChecker.UpdateCheckerStatus> unsupportedSoftwareLogic = (peVersion, status) -> {
                  if (minecraftVersion.contains("Unknown")) {
                     ServerVersion fallbackVersion = ServerVersion.V_1_8_8;
                     String failureToDetectVersionMsg = "Your server software is preventing us from checking the Minecraft Server version. This is what we found: "
                        + minecraftVersion
                        + ". We will assume the Server version is "
                        + fallbackVersion.name()
                        + "... If you need assistance, join our Discord server: https://discord.gg/DVHxPPxHZc";
                     this.getLogManager().warn(failureToDetectVersionMsg);
                  } else {
                     PEVersion bukkitServerVersion = PEVersion.fromString(minecraftVersion);
                     PEVersion latestSupportedVersion = PEVersion.fromString(ServerVersion.getLatest().getReleaseName());
                     if (bukkitServerVersion.isNewerThan(latestSupportedVersion)) {
                        String developmentBuildsMsg = "Please test the development builds, as they may already have support for your Minecraft version (hint: select the build that contains 'spigot'): https://ci.codemc.io/job/retrooper/job/packetevents";
                        String releaseBuildsMsg = "Please test the latest stable release, as it should already have support for your Minecraft version: https://modrinth.com/plugin/packetevents";
                        String newBuildsMsg = status != UpdateChecker.UpdateCheckerStatus.OUTDATED
                              && status != UpdateChecker.UpdateCheckerStatus.FAILED
                              && status != null
                           ? developmentBuildsMsg
                           : releaseBuildsMsg;
                        this.getLogManager()
                           .warn(
                              "Your build of PacketEvents does not support the Minecraft version "
                                 + bukkitServerVersion
                                 + "! The latest Minecraft version supported by your build of PacketEvents is "
                                 + latestSupportedVersion
                                 + ". "
                                 + newBuildsMsg
                                 + " If you're in need of any help, join our Discord server: https://discord.gg/DVHxPPxHZc"
                           );
                        if (bukkitServerVersion.major() > latestSupportedVersion.major() || bukkitServerVersion.minor() > latestSupportedVersion.minor()) {
                           Bukkit.getPluginManager().disablePlugin(plugin);
                           stopping.set(true);
                        }
                     }
                  }
               };
               if (this.settings.shouldCheckForUpdates()) {
                  this.getUpdateChecker().handleUpdateCheck(unsupportedSoftwareLogic);
               } else {
                  unsupportedSoftwareLogic.accept(null, null);
               }

               if (stopping.get()) {
                  return;
               }

               Metrics metrics = new Metrics(pluginx, 11327);
               metrics.addCustomChart(new SimplePie("packetevents_version", () -> this.getVersion().toStringWithoutSnapshot()));
               Bukkit.getPluginManager().registerEvents(new InternalGlobalBukkitListener(), pluginx);

               try {
                  Class.forName("io.papermc.paper.connection.PlayerConnection");
                  if (this.serverManager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_9)) {
                     Bukkit.getPluginManager().registerEvents(new InternalPaperJoinListener(pluginx), pluginx);
                  } else {
                     Bukkit.getPluginManager().registerEvents(new InternalPaperListener(pluginx), pluginx);
                  }
               } catch (ClassNotFoundException var10) {
                  if (this.serverManager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
                     Bukkit.getPluginManager().registerEvents(new InternalBukkitLoginListener(), pluginx);
                  } else {
                     Bukkit.getPluginManager().registerEvents(new InternalBukkitListener(pluginx), pluginx);
                  }
               }

               if (this.lateBind) {
                  Runnable lateBindTask = () -> {
                     if (this.injector.isServerBound()) {
                        this.injector.inject();
                     }
                  };
                  FoliaScheduler.runTaskOnInit(pluginx, lateBindTask);
               }

               if (!"true".equalsIgnoreCase(System.getenv("PE_IGNORE_INCOMPATIBILITY"))) {
                  this.checkCompatibility();
               }

               for (Player player : Bukkit.getOnlinePlayers()) {
                  User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                  SpigotChannelInjector injector = (SpigotChannelInjector)PacketEvents.getAPI().getInjector();
                  injector.updatePlayer(user, player);
               }

               this.initialized = true;
            }
         }

         private void checkCompatibility() {
            ViaVersionUtil.checkIfViaIsPresent();
            ProtocolSupportUtil.isAvailable();
            Plugin viaPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
            if (viaPlugin != null) {
               String[] ver = viaPlugin.getDescription().getVersion().split("\\.", 3);
               int major = Integer.parseInt(ver[0]);
               int minor = Integer.parseInt(ver[1]);
               if (major < 4 || major == 4 && minor < 5) {
                  PacketEvents.getAPI()
                     .getLogManager()
                     .severe("You are attempting to combine 2.0 PacketEvents with a ViaVersion older than 4.5.0, please update your ViaVersion!");
                  Plugin ourPlugin = this.getPlugin();
                  Bukkit.getPluginManager().disablePlugin(ourPlugin);
                  throw new IllegalStateException("ViaVersion incompatibility! Update to v4.5.0 or newer!");
               }
            }

            Plugin protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
            if (protocolLibPlugin != null) {
               int majorVersion = Integer.parseInt(protocolLibPlugin.getDescription().getVersion().split("\\.", 2)[0]);
               if (majorVersion < 5) {
                  PacketEvents.getAPI()
                     .getLogManager()
                     .severe(
                        "You are attempting to combine 2.0 PacketEvents with a ProtocolLib version older than v5.0.0. This is no longer works, please update to their dev builds. https://ci.dmulloy2.net/job/ProtocolLib/lastBuild/"
                     );
                  Plugin ourPlugin = this.getPlugin();
                  Bukkit.getPluginManager().disablePlugin(ourPlugin);
                  throw new IllegalStateException("ProtocolLib incompatibility! Update to v5.0.0 or newer!");
               }
            }
         }

         @Override
         public boolean isInitialized() {
            return this.initialized;
         }

         @Override
         public void terminate() {
            if (this.initialized) {
               super.terminate();

               for (User user : this.protocolManager.getUsers()) {
                  ServerConnectionInitializer.destroyHandlers(user.getChannel());
               }

               this.initialized = false;
               this.terminated = true;
            }
         }

         @Override
         public boolean isTerminated() {
            return this.terminated;
         }

         public Plugin getPlugin() {
            return plugin;
         }

         @Override
         public ProtocolManager getProtocolManager() {
            return this.protocolManager;
         }

         @Override
         public ServerManager getServerManager() {
            return this.serverManager;
         }

         @Override
         public PlayerManager getPlayerManager() {
            return this.playerManager;
         }

         @Override
         public PacketEventsSettings getSettings() {
            return this.settings;
         }

         @Override
         public NettyManager getNettyManager() {
            return this.nettyManager;
         }

         @Override
         public ChannelInjector getInjector() {
            return this.injector;
         }

         @Override
         public LogManager getLogManager() {
            return this.logManager;
         }
      };
   }
}
