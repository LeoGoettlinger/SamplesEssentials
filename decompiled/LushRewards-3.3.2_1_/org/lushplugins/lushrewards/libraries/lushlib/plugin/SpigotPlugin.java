package org.lushplugins.lushrewards.libraries.lushlib.plugin;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.command.Command;
import org.lushplugins.lushrewards.libraries.lushlib.hook.Hook;
import org.lushplugins.lushrewards.libraries.lushlib.manager.Manager;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;

public abstract class SpigotPlugin extends JavaPlugin {
   protected ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
   private ConcurrentHashMap<Class<? extends Manager>, Manager> managers = new ConcurrentHashMap<>();
   protected ConcurrentHashMap<String, Hook> hooks = new ConcurrentHashMap<>();
   protected ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();

   public void debug(@NotNull String message) {
      this.log(Level.FINE, message);
   }

   public void debug(@NotNull String message, @NotNull Throwable throwable) {
      this.log(Level.FINE, message, throwable);
   }

   @Deprecated
   public void debug(@NotNull String message, @NotNull Throwable... throwables) {
      this.log(Level.FINE, message, throwables);
   }

   public void log(@NotNull Level level, @NotNull String message) {
      this.getLogger().log(level, message);
   }

   public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable throwable) {
      this.getLogger().log(level, message, throwable);
   }

   @Deprecated
   public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwables) {
      if (throwables.length > 0) {
         for (Throwable throwable : throwables) {
            this.getLogger().log(level, message, throwable);
         }
      } else {
         this.getLogger().log(level, message);
      }
   }

   public Collection<Module> getModules() {
      return this.modules.values();
   }

   public Optional<Module> getModule(String moduleId) {
      return Optional.ofNullable(this.modules.get(moduleId));
   }

   public void registerModule(Module module) {
      if (this.modules.containsKey(module.getId())) {
         this.log(Level.SEVERE, "Failed to register module with id '" + module.getId() + "', a module with this id is already registered");
      } else {
         this.modules.put(module.getId(), module);
      }
   }

   public void unregisterModule(String moduleId) {
      if (this.modules.containsKey(moduleId)) {
         this.modules.get(moduleId).disable();
         this.modules.remove(moduleId);
      }
   }

   public void unregisterAllModules() {
      this.modules.values().forEach(Module::disable);
      this.modules.clear();
   }

   public <T extends Manager> Optional<T> getManager(Class<T> clazz) {
      return this.managers.containsKey(clazz) ? Optional.of((T)this.managers.get(clazz)) : Optional.empty();
   }

   public void registerManager(@NotNull Manager... managers) {
      if (this.managers == null) {
         this.managers = new ConcurrentHashMap<>();
      }

      for (Manager manager : managers) {
         this.managers.put((Class<? extends Manager>)manager.getClass(), manager);
         manager.enable();
      }
   }

   public void unregisterManager(Class<? extends Manager> clazz) {
      Manager manager = this.managers.get(clazz);
      if (manager != null) {
         manager.disable();
         this.managers.remove(clazz);
      }
   }

   public Collection<Hook> getHooks() {
      return this.hooks.values();
   }

   public Optional<Hook> getHook(String hookId) {
      return Optional.ofNullable(this.hooks.get(hookId));
   }

   public void registerHook(Hook hook) {
      if (this.hooks.containsKey(hook.getId())) {
         this.log(Level.SEVERE, "Failed to register hook with id '" + hook.getId() + "', a hook with this id is already registered");
      } else {
         this.hooks.put(hook.getId(), hook);
      }
   }

   public void unregisterHook(String hookId) {
      if (this.hooks.containsKey(hookId)) {
         this.hooks.get(hookId).disable();
         this.hooks.remove(hookId);
      }
   }

   public void unregisterAllHooks() {
      this.hooks.values().forEach(Module::disable);
      this.hooks.clear();
   }

   public void addHook(String pluginName, Runnable runnable) {
      PluginManager pluginManager = this.getServer().getPluginManager();
      if (pluginManager.getPlugin(pluginName) instanceof JavaPlugin hookPlugin && hookPlugin.isEnabled()) {
         this.getLogger().info("Found plugin \"" + pluginName + "\". Enabling " + pluginName + " support.");
         runnable.run();
      }
   }

   public Command getLushCommand(String name) {
      return this.commands.get(name);
   }

   public void registerListener(Listener listener) {
      this.getServer().getPluginManager().registerEvents(listener, this);
   }

   public void registerListeners(Listener... listeners) {
      PluginManager pluginManager = this.getServer().getPluginManager();

      for (Listener listener : listeners) {
         pluginManager.registerEvents(listener, this);
      }
   }

   public void registerCommand(Command command) {
      this.registerCommand(command.getName(), command, command.getRequiredPermission());
   }

   public void registerCommand(String commandName, CommandExecutor executor, @Nullable String permission) {
      PluginCommand command = this.getCommand(commandName);
      if (command == null) {
         this.getLogger().severe("Failed to register command '" + commandName + "', make sure the command has been defined in the plugin.yml");
      } else {
         command.setExecutor(executor);
         command.setPermission(permission);
      }
   }

   public boolean callEvent(Event event) {
      Bukkit.getServer().getPluginManager().callEvent(event);
      return event instanceof Cancellable cancellable ? !cancellable.isCancelled() : true;
   }

   public NamespacedKey namespacedKey(String key) {
      return new NamespacedKey(this, key);
   }

   public FileConfiguration getConfigResource(String path) {
      return YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), path));
   }

   public void saveDefaultResource(String path) {
      if (!new File(this.getDataFolder(), path).exists()) {
         this.saveResource(path, false);
         this.getLogger().info("File Created: " + path);
      }
   }

   public void backupFile(File file) {
      File parent = file.getParentFile();
      String name = file.getName();
      if (!file.renameTo(
         new File(
            parent, FilenameUtils.removeExtension(name) + "-old-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy--HH-mm-ss")) + ".yml"
         )
      )) {
         this.getLogger().severe("Failed to rename file '" + name + "'");
      }
   }
}
