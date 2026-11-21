package me.chancesd.pvpmanager.sdutils.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.PluginHelpManager;
import me.chancesd.pvpmanager.sdutils.display.DisplayManager;
import me.chancesd.pvpmanager.sdutils.library.PluginLibraries;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SDPlugin extends JavaPlugin {
   private int permissionNodes;
   private DisplayManager displayManager;
   private boolean isReloading;
   private final Map<String, BaseCommand> registeredCommands = new HashMap<>();

   protected Listener registerListener(Listener listener) {
      this.getServer().getPluginManager().registerEvents(listener, this);
      return listener;
   }

   protected BaseCommand registerCommand(PluginCommand command, BaseCommand executor) {
      if (command == null) {
         return executor;
      } else {
         command.setExecutor(executor);
         if (executor != null) {
            this.registeredCommands.put(command.getName(), executor);
         }

         return executor;
      }
   }

   protected BaseCommand registerCommand(String commandName, Function<PluginCommand, BaseCommand> factory) {
      PluginCommand command = this.getCommand(commandName);
      return command == null ? null : this.registerCommand(command, factory.apply(command));
   }

   public Collection<BaseCommand> getRegisteredCommands() {
      return new ArrayList<>(this.registeredCommands.values());
   }

   public BaseCommand getRegisteredCommand(String commandName) {
      return this.registeredCommands.get(commandName.toLowerCase());
   }

   public List<String> getRegisteredCommandNames() {
      return new ArrayList<>(this.registeredCommands.keySet());
   }

   public PluginHelpManager createHelpManager() {
      return new PluginHelpManager(this);
   }

   public abstract void onPluginLoad();

   public abstract void onPluginEnable();

   public abstract void onPluginDisable();

   public void onLoad() {
      Log.setup(this);
      PluginLibraries.checkDependencies(this);
      this.onPluginLoad();
   }

   public void onEnable() {
      this.permissionNodes = this.loadPermissionNodes();
      ScheduleUtils.setupExecutor(this);
      this.displayManager = new DisplayManager();
      this.onPluginEnable();
      this.checkJavaVersion();
   }

   public void onDisable() {
      this.onPluginDisable();
      this.displayManager.cleanup();
      ScheduleUtils.cancelAllTasks();
   }

   public void reload() {
      this.setReloading(true);
      this.onDisable();
      HandlerList.unregisterAll(this);
      Bukkit.getScheduler().cancelTasks(this);
      this.onEnable();
      this.setReloading(false);
   }

   private void checkJavaVersion() {
      String version = System.getProperty("java.version");
      if (version.startsWith("1.")) {
         version = version.substring(2, 3);
      } else {
         int dot = version.indexOf(".");
         if (dot != -1) {
            version = version.substring(0, dot);
         } else {
            int separator = version.indexOf("-");
            if (separator != -1) {
               version = version.substring(0, separator);
            }
         }
      }

      int javaVersion;
      try {
         javaVersion = Integer.parseInt(version);
      } catch (NumberFormatException var5) {
         return;
      }

      if (javaVersion < 21) {
         Log.warning("You appear to be using Java 20 or lower. For now the plugin still works but please update to Java 21+");
         Log.warning("In the future this plugin will stop supporting Java versions this old");
      }
   }

   public DisplayManager getDisplayManager() {
      return this.displayManager;
   }

   public boolean isReloading() {
      return this.isReloading;
   }

   public void setReloading(boolean isReloading) {
      this.isReloading = isReloading;
   }
}
