package me.clip.placeholderapi;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import me.clip.placeholderapi.commands.PlaceholderCommandRouter;
import me.clip.placeholderapi.configuration.PlaceholderAPIConfig;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Version;
import me.clip.placeholderapi.expansion.manager.CloudExpansionManager;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitAudiences;
import me.clip.placeholderapi.listeners.ServerLoadEventListener;
import me.clip.placeholderapi.metrics.bukkit.Metrics;
import me.clip.placeholderapi.metrics.charts.AdvancedPie;
import me.clip.placeholderapi.metrics.charts.SimplePie;
import me.clip.placeholderapi.scheduler.UniversalScheduler;
import me.clip.placeholderapi.scheduler.scheduling.schedulers.TaskScheduler;
import me.clip.placeholderapi.updatechecker.UpdateChecker;
import me.clip.placeholderapi.util.ExpansionSafetyCheck;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPIPlugin extends JavaPlugin {
   private int worldEvents;
   @NotNull
   private static final Version VERSION;
   private static PlaceholderAPIPlugin instance;
   @NotNull
   private final PlaceholderAPIConfig config = new PlaceholderAPIConfig(this);
   @NotNull
   private final LocalExpansionManager localExpansionManager = new LocalExpansionManager(this);
   @NotNull
   private final CloudExpansionManager cloudExpansionManager = new CloudExpansionManager(this);
   @NotNull
   private final TaskScheduler scheduler = UniversalScheduler.getScheduler(this);
   private BukkitAudiences adventure;
   private boolean safetyCheck = false;

   @NotNull
   public static PlaceholderAPIPlugin getInstance() {
      return instance;
   }

   @NotNull
   public static String booleanTrue() {
      return getInstance().getPlaceholderAPIConfig().booleanTrue();
   }

   @NotNull
   public static String booleanFalse() {
      return getInstance().getPlaceholderAPIConfig().booleanFalse();
   }

   @NotNull
   public static SimpleDateFormat getDateFormat() {
      try {
         return new SimpleDateFormat(getInstance().getPlaceholderAPIConfig().dateFormat());
      } catch (IllegalArgumentException var1) {
         Msg.warn("Configured date format ('%s') is invalid! Defaulting to 'MM/dd/yy HH:mm:ss'", var1, getInstance().getPlaceholderAPIConfig().dateFormat());
         return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
      }
   }

   @Deprecated
   public static Version getServerVersion() {
      return VERSION;
   }

   public void onLoad() {
      this.saveDefaultConfig();
      this.safetyCheck = new ExpansionSafetyCheck(this).runChecks();
      if (!this.safetyCheck) {
         instance = this;
      }
   }

   public void onEnable() {
      this.worldEvents = this.registerWorldEvents();
      if (!this.safetyCheck) {
         this.setupCommand();
         this.setupMetrics();
         this.setupExpansions();
         this.adventure = BukkitAudiences.create(this);
         if (this.config.isCloudEnabled()) {
            this.getCloudExpansionManager().load();
         }

         if (this.config.checkUpdates()) {
            new UpdateChecker(this).fetch();
         }
      }
   }

   public void onDisable() {
      if (!this.safetyCheck) {
         this.getCloudExpansionManager().kill();
         this.getLocalExpansionManager().kill();
         HandlerList.unregisterAll(this);
         this.scheduler.cancelTasks(this);
         this.adventure.close();
         this.adventure = null;
         instance = null;
      }
   }

   public void reloadConf(@NotNull CommandSender sender) {
      this.getLocalExpansionManager().kill();
      this.reloadConfig();
      this.getLocalExpansionManager().load(sender);
      if (this.config.isCloudEnabled()) {
         this.getCloudExpansionManager().load();
      } else {
         this.getCloudExpansionManager().kill();
      }
   }

   @NotNull
   public LocalExpansionManager getLocalExpansionManager() {
      return this.localExpansionManager;
   }

   @NotNull
   public CloudExpansionManager getCloudExpansionManager() {
      return this.cloudExpansionManager;
   }

   @NotNull
   public BukkitAudiences getAdventure() {
      return this.adventure;
   }

   @NotNull
   public TaskScheduler getScheduler() {
      return this.scheduler;
   }

   @NotNull
   public PlaceholderAPIConfig getPlaceholderAPIConfig() {
      return this.config;
   }

   private void setupCommand() {
      PluginCommand pluginCommand = this.getCommand("placeholderapi");
      if (pluginCommand != null) {
         PlaceholderCommandRouter router = new PlaceholderCommandRouter(this);
         pluginCommand.setExecutor(router);
         pluginCommand.setTabCompleter(router);
      }
   }

   private void setupMetrics() {
      Metrics metrics = new Metrics(this, 438);
      metrics.addCustomChart(new SimplePie("using_expansion_cloud", () -> this.getPlaceholderAPIConfig().isCloudEnabled() ? "yes" : "no"));
      metrics.addCustomChart(new SimplePie("using_spigot", () -> getServerVersion().isSpigot() ? "yes" : "no"));
      metrics.addCustomChart(new AdvancedPie("expansions_used", () -> {
         Map<String, Integer> values = new HashMap<>();

         for (PlaceholderExpansion expansion : this.getLocalExpansionManager().getExpansions()) {
            values.put(expansion.getRequiredPlugin() == null ? expansion.getIdentifier() : expansion.getRequiredPlugin(), 1);
         }

         return values;
      }));
   }

   private void setupExpansions() {
      Bukkit.getPluginManager().registerEvents(this.getLocalExpansionManager(), this);

      try {
         Class.forName("org.bukkit.event.server.ServerLoadEvent");
         new ServerLoadEventListener(this);
      } catch (ClassNotFoundException var2) {
         this.scheduler.runTaskLater(() -> this.getLocalExpansionManager().load(Bukkit.getConsoleSender()), 1L);
      }
   }

   static {
      String version = ServerVersionResolver.resolve(Bukkit.getServer().getBukkitVersion()).getLegacyVersion();

      boolean isSpigot;
      try {
         Class.forName("org.spigotmc.SpigotConfig");
         isSpigot = true;
      } catch (ClassNotFoundException | ExceptionInInitializerError var3) {
         isSpigot = false;
      }

      VERSION = new Version(version, isSpigot);
   }
}
