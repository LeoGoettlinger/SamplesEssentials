package eu.kennytv.maintenance.paper;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.lib.bstats.bukkit.Metrics;
import eu.kennytv.maintenance.paper.command.MaintenancePaperCommand;
import eu.kennytv.maintenance.paper.listener.PaperServerListPingListener;
import eu.kennytv.maintenance.paper.listener.PlayerLoginListener;
import eu.kennytv.maintenance.paper.util.PaperOfflinePlayerInfo;
import eu.kennytv.maintenance.paper.util.PaperTask;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.CachedServerIcon;

public final class MaintenancePaperPlugin extends MaintenancePlugin {
   private static final boolean FOLIA = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
   private final MaintenancePaperBase plugin;
   private CachedServerIcon favicon;

   MaintenancePaperPlugin(MaintenancePaperBase plugin) {
      super(plugin.getDescription().getVersion(), ServerType.PAPER);
      this.plugin = plugin;
      this.settings = new Settings(
         this,
         "redis",
         "proxied-maintenance-servers",
         "fallback",
         "waiting-server",
         "commands-on-single-maintenance-enable",
         "commands-on-single-maintenance-disable"
      );
      this.sendEnableMessage();
      MaintenancePaperCommand command = new MaintenancePaperCommand(this, this.settings);
      this.commandManager = command;
      plugin.getCommand("maintenance").setExecutor(command);
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(new PlayerLoginListener(this, this.settings), plugin);
      pm.registerEvents(new PaperServerListPingListener(this, this.settings), plugin);
      this.continueLastEndtimer();
      new Metrics(plugin, 2205);
      Plugin serverListPlus = pm.getPlugin("ServerListPlus");
      if (pm.isPluginEnabled(serverListPlus)) {
         this.serverListPlusHook = new ServerListPlusHook(serverListPlus);
         if (this.settings.isEnablePingMessages()) {
            this.serverListPlusHook.setEnabled(!this.settings.isMaintenance());
         }

         plugin.getLogger().info("Enabled ServerListPlus integration");
      }
   }

   private static boolean hasClass(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ReflectiveOperationException var2) {
         return false;
      }
   }

   @Override
   public Task startMaintenanceRunnable(Runnable runnable) {
      if (FOLIA) {
         throw new UnsupportedOperationException("Scheduling tasks is not yet supported on Folia");
      } else {
         return new PaperTask(this.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0L, 20L));
      }
   }

   @Override
   public CompletableFuture<SenderInfo> getOfflinePlayer(String name) {
      OfflinePlayer player = this.getServer().getOfflinePlayer(name);
      return CompletableFuture.completedFuture(player.getName() != null ? new PaperOfflinePlayerInfo(player) : null);
   }

   @Override
   public CompletableFuture<SenderInfo> getOfflinePlayer(UUID uuid) {
      OfflinePlayer player = this.getServer().getOfflinePlayer(uuid);
      return CompletableFuture.completedFuture(player.getName() != null ? new PaperOfflinePlayerInfo(player) : null);
   }

   @Override
   public void sync(Runnable runnable) {
      this.getServer().getScheduler().runTask(this.plugin, runnable);
   }

   @Override
   public void async(Runnable runnable) {
      if (FOLIA) {
         runnable.run();
      } else {
         this.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
      }
   }

   @Override
   protected void executeConsoleCommand(String command) {
      this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
   }

   @Override
   public void broadcast(Component component) {
      this.getServer().sendMessage(component);
   }

   @Override
   protected void kickPlayers() {
      for (Player p : this.getServer().getOnlinePlayers()) {
         if (!this.hasPermission(p, "bypass") && !this.settings.isWhitelisted(p.getUniqueId())) {
            p.kick(this.settings.getKickMessage());
         }
      }
   }

   @Override
   public File getDataFolder() {
      return this.plugin.getDataFolder();
   }

   @Override
   public File getPluginFile() {
      return this.plugin.getPluginFile();
   }

   @Override
   protected int getOnlinePlayers() {
      return this.getServer().getOnlinePlayers().size();
   }

   @Override
   protected int getMaxPlayers() {
      return this.getServer().getMaxPlayers();
   }

   @Override
   public InputStream getResource(String name) {
      return this.plugin.getResource(name);
   }

   @Override
   public Logger getLogger() {
      return this.plugin.getLogger();
   }

   @Override
   public String getServerVersion() {
      return this.getServer().getVersion();
   }

   @Override
   public List<PluginDump> getPlugins() {
      return Arrays.stream(this.getServer().getPluginManager().getPlugins())
         .map(plugin -> new PluginDump(plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthors()))
         .collect(Collectors.toList());
   }

   @Override
   protected void loadIcon(File file) throws Exception {
      this.favicon = this.plugin.getServer().loadServerIcon(ImageIO.read(file));
   }

   public boolean hasPermission(CommandSender sender, String permission) {
      return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
   }

   public Server getServer() {
      return this.plugin.getServer();
   }

   public CachedServerIcon getFavicon() {
      return this.favicon;
   }
}
