package com.viaversion.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.bukkit.commands.BukkitCommandHandler;
import com.viaversion.viaversion.bukkit.listeners.JoinListener;
import com.viaversion.viaversion.bukkit.platform.BukkitViaAPI;
import com.viaversion.viaversion.bukkit.platform.BukkitViaConfig;
import com.viaversion.viaversion.bukkit.platform.BukkitViaInjector;
import com.viaversion.viaversion.bukkit.platform.BukkitViaLoader;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTask;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTaskTask;
import com.viaversion.viaversion.bukkit.platform.FoliaViaTask;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ViaVersionPlugin extends JavaPlugin implements ViaPlatform<Player> {
   private static final boolean FOLIA = PaperViaInjector.hasClass("io.papermc.paper.threadedregions.RegionizedServer");
   private static final Runnable DUMMY_RUNNABLE = () -> {};
   private static ViaVersionPlugin instance;
   private final BukkitCommandHandler commandHandler = new BukkitCommandHandler();
   private final BukkitViaConfig conf;
   private final ViaAPI<Player> api = new BukkitViaAPI();
   private boolean lateBind;

   public ViaVersionPlugin() {
      instance = this;
      this.conf = new BukkitViaConfig(this.getDataFolder(), this.getLogger());
      Via.init(
         ViaManagerImpl.builder()
            .platform(this)
            .commandHandler(this.commandHandler)
            .injector(new BukkitViaInjector())
            .loader(new BukkitViaLoader(this))
            .build()
      );
      this.conf.reload();
   }

   public void onLoad() {
      this.lateBind = !((BukkitViaInjector)Via.getManager().getInjector()).isBinded();
      if (!this.lateBind) {
         this.getLogger().info("ViaVersion " + this.getDescription().getVersion() + " is now loaded. Registering protocol transformers and injecting...");
         ((ViaManagerImpl)Via.getManager()).init();
      } else {
         this.getLogger().info("ViaVersion " + this.getDescription().getVersion() + " is now loaded. Waiting for boot (late-bind).");
      }
   }

   public void onEnable() {
      ViaManagerImpl manager = (ViaManagerImpl)Via.getManager();
      if (this.lateBind) {
         this.getLogger().info("Registering protocol transformers and injecting...");
         manager.init();
      }

      if (Via.getConfig().shouldRegisterUserConnectionOnJoin()) {
         this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
      }

      if (FOLIA) {
         Class<? extends Event> serverInitEventClass;
         try {
            serverInitEventClass = (Class<? extends Event>)Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
         } catch (ReflectiveOperationException var4) {
            throw new RuntimeException(var4);
         }

         this.getServer()
            .getPluginManager()
            .registerEvent(serverInitEventClass, new Listener() {}, EventPriority.HIGHEST, (listener, event) -> manager.onServerLoaded(), this);
      } else if (Via.getManager().getInjector().lateProtocolVersionSetting()) {
         this.runSync(manager::onServerLoaded);
      } else {
         manager.onServerLoaded();
      }

      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "vv:server_details");
      this.getCommand("viaversion").setExecutor(this.commandHandler);
      this.getCommand("viaversion").setTabCompleter(this.commandHandler);
   }

   public void onDisable() {
      ((ViaManagerImpl)Via.getManager()).destroy();
   }

   @Override
   public String getPlatformName() {
      return Bukkit.getServer().getName();
   }

   @Override
   public String getPlatformVersion() {
      return Bukkit.getServer().getVersion();
   }

   @Override
   public PlatformTask runAsync(Runnable runnable) {
      return (PlatformTask)(FOLIA
         ? new BukkitViaTaskTask(Via.getManager().getScheduler().execute(runnable))
         : new BukkitViaTask(this.getServer().getScheduler().runTaskAsynchronously(this, runnable)));
   }

   @Override
   public PlatformTask runRepeatingAsync(Runnable runnable, long ticks) {
      return (PlatformTask)(FOLIA
         ? new BukkitViaTaskTask(Via.getManager().getScheduler().scheduleRepeating(runnable, 0L, ticks * 50L, TimeUnit.MILLISECONDS))
         : new BukkitViaTask(this.getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, 0L, ticks)));
   }

   @Override
   public PlatformTask runSync(Runnable runnable) {
      return (PlatformTask)(FOLIA ? this.runAsync(runnable) : new BukkitViaTask(this.getServer().getScheduler().runTask(this, runnable)));
   }

   @Override
   public PlatformTask runSync(Runnable runnable, long delay) {
      return (PlatformTask)(FOLIA
         ? new FoliaViaTask(this.getServer().getGlobalRegionScheduler().runDelayed(this, e -> runnable.run(), delay <= 0L ? 1L : delay))
         : new BukkitViaTask(this.getServer().getScheduler().runTaskLater(this, runnable, delay)));
   }

   public PlatformTask<?> runSyncAt(Runnable runnable, Block block) {
      return (PlatformTask<?>)(FOLIA
         ? new FoliaViaTask(this.getServer().getRegionScheduler().run(this, block.getLocation(), e -> runnable.run()))
         : this.runSync(runnable));
   }

   public PlatformTask<?> runSyncFor(Runnable runnable, Player player) {
      return (PlatformTask<?>)(FOLIA ? new FoliaViaTask(player.getScheduler().run(this, e -> runnable.run(), DUMMY_RUNNABLE)) : this.runSync(() -> {
         if (player.isOnline()) {
            runnable.run();
         }
      }));
   }

   @Override
   public PlatformTask runRepeatingSync(Runnable runnable, long period) {
      return (PlatformTask)(FOLIA
         ? new FoliaViaTask(this.getServer().getGlobalRegionScheduler().runAtFixedRate(this, e -> runnable.run(), 1L, period))
         : new BukkitViaTask(this.getServer().getScheduler().runTaskTimer(this, runnable, 0L, period)));
   }

   @Override
   public void sendMessage(UserConnection connection, String message) {
      UUID uuid = connection.getProtocolInfo().getUuid();
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         player.sendMessage(message);
      }
   }

   @Override
   public boolean kickPlayer(UserConnection connection, String message) {
      UUID uuid = connection.getProtocolInfo().getUuid();
      Player player;
      if (uuid != null && (player = Bukkit.getPlayer(uuid)) != null) {
         player.kickPlayer(message);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void sendCustomPayload(UserConnection connection, String channel, byte[] message) {
      UUID uuid = connection.getProtocolInfo().getUuid();
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         Bukkit.getMessenger().dispatchIncomingMessage(player, channel, message);
      }
   }

   @Override
   public void sendCustomPayloadToClient(UserConnection connection, String channel, byte[] message) {
      UUID uuid = connection.getProtocolInfo().getUuid();
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         try {
            String className = Bukkit.getServer().getClass().getName();
            String craftBukkitPackage = className.substring(0, className.lastIndexOf(46));
            Class.forName(craftBukkitPackage + ".entity.CraftPlayer").getMethod("addChannel", String.class).invoke(player, channel);
         } catch (InvocationTargetException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException var8) {
            this.getLogger().log(Level.SEVERE, "Failed to register custom payload channel " + channel + " for player " + player.getName(), (Throwable)var8);
            return;
         }

         player.sendPluginMessage(this, channel, message);
      }
   }

   @Override
   public void onReload() {
      if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
         this.getLogger().severe("ViaVersion is already loaded, we're going to kick all the players... because otherwise we'll crash because of ProtocolLib.");

         for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', this.conf.getReloadDisconnectMsg()));
         }
      } else {
         this.getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
      }
   }

   @Override
   public JsonObject getDump() {
      JsonObject platformSpecific = new JsonObject();
      List<PluginInfo> plugins = new ArrayList<>();

      for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
         plugins.add(
            new PluginInfo(
               p.isEnabled(), p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), p.getDescription().getAuthors()
            )
         );
      }

      platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
      return platformSpecific;
   }

   public BukkitViaConfig getConf() {
      return this.conf;
   }

   @Override
   public ViaAPI<Player> getApi() {
      return this.api;
   }

   @Override
   public boolean hasPlugin(String name) {
      return this.getServer().getPluginManager().getPlugin(name) != null;
   }

   @Override
   public boolean couldBeReloading() {
      return !PaperViaInjector.PAPER_IS_STOPPING_METHOD || !Bukkit.isStopping();
   }

   public boolean isLateBind() {
      return this.lateBind;
   }

   @Deprecated(
      forRemoval = true
   )
   public static ViaVersionPlugin getInstance() {
      return instance;
   }
}
