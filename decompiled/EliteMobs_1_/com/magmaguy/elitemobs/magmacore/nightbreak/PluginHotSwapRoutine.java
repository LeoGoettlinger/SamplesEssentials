package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class PluginHotSwapRoutine implements Runnable {
   static final String ACTIVE_PROPERTY = "nightbreak.pluginHotSwap.active";
   private final String targetName;
   private final String[] unloadOrder;
   private final String[] loadOrder;
   private final File[] loadFiles;
   private final File stagedJar;
   private final File[] heldBack;
   private final File workDirectory;
   private final File routineDirectory;
   private final CommandSender sender;

   public PluginHotSwapRoutine(
      String targetName,
      String[] unloadOrder,
      String[] loadOrder,
      File[] loadFiles,
      File stagedJar,
      File[] heldBack,
      File workDirectory,
      File routineDirectory,
      CommandSender sender
   ) {
      this.targetName = targetName;
      this.unloadOrder = unloadOrder;
      this.loadOrder = loadOrder;
      this.loadFiles = loadFiles;
      this.stagedJar = stagedJar;
      this.heldBack = heldBack;
      this.workDirectory = workDirectory;
      this.routineDirectory = routineDirectory;
      this.sender = sender;
   }

   @Override
   public void run() {
      try {
         this.swap();
      } catch (Throwable var12) {
         Bukkit.getLogger().log(Level.SEVERE, "[" + this.targetName + "] Plugin hot swap failed unexpectedly. Restart the server to recover.", var12);
         this.report("§cThe hot swap failed unexpectedly. Restart the server to recover. Details are in the console.");
      } finally {
         System.clearProperty("nightbreak.pluginHotSwap.active");
         deleteRecursively(this.routineDirectory);
         this.workDirectory.delete();
         if (this.getClass().getClassLoader() instanceof Closeable closeable) {
            try {
               closeable.close();
            } catch (IOException var11) {
            }
         }
      }
   }

   static void deleteRecursively(File file) {
      if (file != null && file.exists()) {
         File[] children = file.listFiles();
         if (children != null) {
            for (File child : children) {
               deleteRecursively(child);
            }
         }

         file.delete();
      }
   }

   private void swap() {
      long started = System.nanoTime();
      PluginManager pluginManager = Bukkit.getPluginManager();
      File targetJar = this.loadFiles[0];
      File incomingJar = new File(this.workDirectory, targetJar.getName() + ".incoming");
      File previousJar = new File(this.workDirectory, targetJar.getName() + ".previous");
      Plugin running = pluginManager.getPlugin(this.targetName);
      String fromVersion = running == null ? "?" : running.getDescription().getVersion();

      for (String name : this.unloadOrder) {
         Plugin plugin = pluginManager.getPlugin(name);
         if (plugin != null) {
            this.unload(pluginManager, plugin);
         }
      }

      boolean taken = this.workDirectory.isDirectory() || this.workDirectory.mkdirs();
      File[] heldBackCopies = new File[this.heldBack.length];

      for (int i = 0; i < this.heldBack.length; i++) {
         File aside = new File(this.workDirectory, this.heldBack[i].getName() + ".held");
         if (this.moveQuietly(this.heldBack[i], aside)) {
            heldBackCopies[i] = aside;
         }
      }

      taken = taken && this.moveQuietly(this.stagedJar, incomingJar);
      boolean replaced = taken && this.copyQuietly(targetJar, previousJar) && this.moveQuietly(incomingJar, targetJar);
      Plugin swapped = replaced ? this.load(pluginManager, targetJar) : null;
      boolean updated = swapped != null && swapped.isEnabled();
      if (!updated) {
         Plugin stray = pluginManager.getPlugin(this.targetName);
         if (stray != null) {
            this.unload(pluginManager, stray);
         }

         boolean restored = !replaced || this.moveQuietly(targetJar, incomingJar) && this.moveQuietly(previousJar, targetJar);
         swapped = restored ? this.load(pluginManager, targetJar) : null;
         if (incomingJar.isFile()) {
            this.moveQuietly(incomingJar, this.stagedJar);
         }

         if (!restored) {
            Bukkit.getLogger()
               .severe(
                  "[" + this.targetName + "] The previous jar is at " + previousJar.getPath() + ". Copy it to " + targetJar.getPath() + " before restarting."
               );
         }
      }

      for (int ix = 1; ix < this.loadOrder.length; ix++) {
         Plugin dependent = this.load(pluginManager, this.loadFiles[ix]);
         if (dependent == null || !dependent.isEnabled()) {
            this.report("§c" + this.loadOrder[ix] + " did not enable again after the swap. Check the console.");
         }
      }

      for (int ixx = 0; ixx < this.heldBack.length; ixx++) {
         if (heldBackCopies[ixx] != null) {
            this.moveQuietly(heldBackCopies[ixx], this.heldBack[ixx]);
         }
      }

      this.syncCommands();
      long elapsedMillis = (System.nanoTime() - started) / 1000000L;
      String cycled = this.loadOrder.length > 1 ? " Also reloaded: " + join(this.loadOrder, 1) + "." : "";
      boolean enabled = swapped != null && swapped.isEnabled();
      if (enabled) {
         previousJar.delete();
      }

      if (updated) {
         this.report(
            "§a"
               + this.targetName
               + " updated from "
               + fromVersion
               + " to "
               + swapped.getDescription().getVersion()
               + " without a restart in "
               + elapsedMillis
               + " ms."
               + cycled
         );
      } else if (enabled) {
         this.report(
            "§c"
               + this.targetName
               + " could not be updated without a restart, so "
               + fromVersion
               + " was loaded again. The update stays staged for the next restart. Check the console for the cause."
               + cycled
         );
      } else {
         this.report("§c" + this.targetName + " could not be updated or restored. Restart the server. Check the console for the cause." + cycled);
      }
   }

   private Plugin load(PluginManager pluginManager, File jar) {
      try {
         Plugin plugin = pluginManager.loadPlugin(jar);
         if (plugin == null) {
            Bukkit.getLogger().warning("[" + this.targetName + "] The server returned no plugin for " + jar.getName() + ".");
            return null;
         } else {
            if (paperPluginManager(pluginManager) == null) {
               plugin.onLoad();
            }

            pluginManager.enablePlugin(plugin);
            return plugin;
         }
      } catch (Throwable var4) {
         Bukkit.getLogger().log(Level.SEVERE, "[" + this.targetName + "] Could not load " + jar.getName() + ".", var4);
         return null;
      }
   }

   private void unload(PluginManager pluginManager, Plugin plugin) {
      try {
         if (plugin.isEnabled()) {
            pluginManager.disablePlugin(plugin);
         }
      } catch (Throwable var8) {
         Bukkit.getLogger().log(Level.WARNING, "[" + this.targetName + "] Error while disabling " + plugin.getName() + ".", var8);
      }

      this.removeRegistration(pluginManager, plugin);
      Object paperManager = paperPluginManager(pluginManager);
      if (paperManager != null) {
         Object instanceManager = readField(paperManager, "instanceManager");
         if (instanceManager != null) {
            this.removeRegistration(instanceManager, plugin);
            this.removeFromDependencyTree(readField(instanceManager, "dependencyTree"), plugin);
         }

         this.removeLaunchProviders(plugin);
      }

      this.removeCommands(plugin);
      this.removeHelpTopics(plugin);

      for (Permission permission : plugin.getDescription().getPermissions()) {
         pluginManager.removePermission(permission);
      }

      if (plugin.getClass().getClassLoader() instanceof Closeable closeable) {
         try {
            closeable.close();
         } catch (IOException var7) {
         }
      }
   }

   private void removeRegistration(Object holder, Plugin plugin) {
      if (readField(holder, "plugins") instanceof List list) {
         list.remove(plugin);
      }

      if (readField(holder, "lookupNames") instanceof Map map) {
         Iterator iterator = map.values().iterator();

         while (iterator.hasNext()) {
            if (iterator.next() == plugin) {
               iterator.remove();
            }
         }
      }
   }

   private void removeFromDependencyTree(Object dependencyTree, Plugin plugin) {
      if (dependencyTree != null) {
         try {
            Object meta = plugin.getClass().getMethod("getPluginMeta").invoke(plugin);

            for (Method method : dependencyTree.getClass().getMethods()) {
               if (method.getName().equals("remove") && method.getParameterCount() == 1 && method.getParameterTypes()[0].isInstance(meta)) {
                  method.invoke(dependencyTree, meta);
                  return;
               }
            }
         } catch (ReflectiveOperationException var8) {
            Bukkit.getLogger()
               .log(Level.WARNING, "[" + this.targetName + "] Could not remove " + plugin.getName() + " from Paper's dependency tree.", (Throwable)var8);
         }
      }
   }

   private void removeLaunchProviders(Plugin plugin) {
      try {
         ClassLoader serverClassLoader = Bukkit.getServer().getClass().getClassLoader();
         Class<?> handlerClass = Class.forName("io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler", false, serverClassLoader);
         Method getMeta = Class.forName("io.papermc.paper.plugin.provider.PluginProvider", false, serverClassLoader).getMethod("getMeta");
         Method getName = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta", false, serverClassLoader).getMethod("getName");
         if (!(readField(handlerClass.getField("INSTANCE").get(null), "storage") instanceof Map map)) {
            return;
         }

         for (Object providerStorage : map.values()) {
            if (readField(providerStorage, "providers") instanceof List providers) {
               List stale = new ArrayList();

               for (Object provider : providers) {
                  Object meta = getMeta.invoke(provider);
                  if (meta != null && plugin.getName().equals(getName.invoke(meta))) {
                     stale.add(provider);
                  }
               }

               providers.removeAll(stale);
            }
         }
      } catch (RuntimeException | ReflectiveOperationException var15) {
         Bukkit.getLogger()
            .log(Level.WARNING, "[" + this.targetName + "] Could not remove " + plugin.getName() + " from Paper's launch provider storage.", (Throwable)var15);
      }
   }

   private void removeCommands(Plugin plugin) {
      try {
         Object server = Bukkit.getServer();
         CommandMap commandMap = (CommandMap)server.getClass().getMethod("getCommandMap").invoke(server);
         if (!(readField(commandMap, "knownCommands") instanceof Map map)) {
            return;
         }

         ClassLoader classLoader = plugin.getClass().getClassLoader();
         List<Command> removed = new ArrayList<>();

         for (Object entryObject : new ArrayList(map.entrySet())) {
            Entry entry = (Entry)entryObject;
            if (entry.getValue() instanceof Command command && ownedBy(command, plugin, classLoader)) {
               map.remove(entry.getKey());
               if (!removed.contains(command)) {
                  removed.add(command);
               }
            }
         }

         for (Command commandx : removed) {
            commandx.unregister(commandMap);
         }
      } catch (RuntimeException | ReflectiveOperationException var13) {
         Bukkit.getLogger().log(Level.WARNING, "[" + this.targetName + "] Could not remove the commands of " + plugin.getName() + ".", (Throwable)var13);
      }
   }

   private void removeHelpTopics(Plugin plugin) {
      if (readField(Bukkit.getHelpMap(), "helpTopics") instanceof Map map) {
         ClassLoader classLoader = plugin.getClass().getClassLoader();

         try {
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry entry = (Entry)iterator.next();
               boolean pluginIndex = plugin.getName().equalsIgnoreCase(String.valueOf(entry.getKey()));
               Object command = readField(entry.getValue(), "command");
               if (pluginIndex || command instanceof Command owned && ownedBy(owned, plugin, classLoader)) {
                  iterator.remove();
               }
            }
         } catch (RuntimeException var10) {
            Bukkit.getLogger().log(Level.WARNING, "[" + this.targetName + "] Could not remove the help topics of " + plugin.getName() + ".", (Throwable)var10);
         }
      }
   }

   private static boolean ownedBy(Command command, Plugin plugin, ClassLoader classLoader) {
      return command.getClass().getClassLoader() == classLoader
         || command instanceof PluginIdentifiableCommand identifiable && identifiable.getPlugin() == plugin;
   }

   private void syncCommands() {
      try {
         Object server = Bukkit.getServer();
         server.getClass().getMethod("syncCommands").invoke(server);
      } catch (RuntimeException | ReflectiveOperationException var4) {
         Bukkit.getLogger().log(Level.WARNING, "[" + this.targetName + "] Could not resend the command tree.", (Throwable)var4);
      }

      try {
         Object helpMap = Bukkit.getHelpMap();
         helpMap.getClass().getMethod("initializeCommands").invoke(helpMap);
      } catch (RuntimeException | ReflectiveOperationException var3) {
         Bukkit.getLogger().log(Level.WARNING, "[" + this.targetName + "] Could not rebuild the help topics.", (Throwable)var3);
      }

      for (Player player : Bukkit.getOnlinePlayers()) {
         player.updateCommands();
      }
   }

   private static Object paperPluginManager(PluginManager pluginManager) {
      return readField(pluginManager, "paperPluginManager");
   }

   private static Object readField(Object holder, String name) {
      if (holder == null) {
         return null;
      } else {
         Class<?> type = holder.getClass();

         while (type != null) {
            try {
               Field field = type.getDeclaredField(name);
               field.setAccessible(true);
               return field.get(holder);
            } catch (NoSuchFieldException var4) {
               type = type.getSuperclass();
            } catch (RuntimeException | ReflectiveOperationException var5) {
               return null;
            }
         }

         return null;
      }
   }

   private boolean moveQuietly(File source, File target) {
      try {
         try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (IOException var4) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }

         return true;
      } catch (IOException var5) {
         Bukkit.getLogger()
            .log(Level.WARNING, "[" + this.targetName + "] Could not move " + source.getPath() + " to " + target.getPath() + ".", (Throwable)var5);
         return false;
      }
   }

   private boolean copyQuietly(File source, File target) {
      try {
         Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         return true;
      } catch (IOException var4) {
         Bukkit.getLogger()
            .log(Level.WARNING, "[" + this.targetName + "] Could not copy " + source.getPath() + " to " + target.getPath() + ".", (Throwable)var4);
         return false;
      }
   }

   private static String join(String[] values, int from) {
      StringBuilder builder = new StringBuilder();

      for (int i = from; i < values.length; i++) {
         if (builder.length() > 0) {
            builder.append(", ");
         }

         builder.append(values[i]);
      }

      return builder.toString();
   }

   private void report(String message) {
      Bukkit.getLogger().info("[" + this.targetName + "] " + message.replaceAll("§.", ""));
      if (this.sender != null && !(this.sender instanceof ConsoleCommandSender)) {
         this.sender.sendMessage(message);
      }
   }
}
