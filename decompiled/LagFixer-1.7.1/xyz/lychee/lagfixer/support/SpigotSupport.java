package xyz.lychee.lagfixer.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.objects.AbstractFork;

public class SpigotSupport extends AbstractFork {
   public SpigotSupport(Plugin plugin) {
      super(plugin);
   }

   @Override
   public boolean isSupported() {
      return true;
   }

   @Override
   public int getPriority() {
      return -1;
   }

   @Override
   public double getMspt() {
      return 0.0;
   }

   @Override
   public boolean isSupportMspt() {
      return false;
   }

   @Override
   public PluginCommand registerCommand(Plugin plugin, String name, List<String> alliases, CommandExecutor executor) {
      try {
         Constructor<PluginCommand> declaredConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
         declaredConstructor.setAccessible(true);
         PluginCommand cmd = declaredConstructor.newInstance(name, plugin);
         cmd.setAliases(alliases);
         cmd.setExecutor(executor);
         if (executor instanceof TabCompleter) {
            cmd.setTabCompleter((TabCompleter)executor);
         }

         Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         bukkitCommandMap.setAccessible(true);
         cmd.register((CommandMap)bukkitCommandMap.get(Bukkit.getServer()));
         return cmd;
      } catch (Exception var8) {
         return null;
      }
   }

   @Override
   public void unregisterCommand(String name) {
      try {
         PluginCommand cmd = Bukkit.getServer().getPluginCommand(name);
         if (cmd == null) {
            return;
         }

         Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         bukkitCommandMap.setAccessible(true);
         cmd.unregister((CommandMap)bukkitCommandMap.get(Bukkit.getServer()));
      } catch (Exception var4) {
      }
   }

   @Override
   public BukkitTask runNow(boolean async, Location loc, Runnable run) {
      if (this.getPlugin().isEnabled()) {
         return async ? Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), run) : Bukkit.getScheduler().runTask(this.getPlugin(), run);
      } else {
         return null;
      }
   }

   @Override
   public BukkitTask runLater(boolean async, Runnable run, long delayInMs) {
      if (this.checkTask(delayInMs)) {
         return null;
      } else {
         return async
            ? Bukkit.getScheduler().runTaskLaterAsynchronously(this.getPlugin(), run, delayInMs / 50L)
            : Bukkit.getScheduler().runTaskLater(this.getPlugin(), run, delayInMs / 50L);
      }
   }

   @Override
   public BukkitTask runTimer(boolean async, Runnable run, long initialDelayInMs, long delayInMs) {
      if (this.checkTask(initialDelayInMs, delayInMs)) {
         return null;
      } else {
         return async
            ? Bukkit.getScheduler().runTaskTimerAsynchronously(this.getPlugin(), run, initialDelayInMs / 50L, delayInMs / 50L)
            : Bukkit.getScheduler().runTaskTimer(this.getPlugin(), run, initialDelayInMs / 50L, delayInMs / 50L);
      }
   }

   @Override
   public BukkitTask runLater(boolean async, Runnable run, long delay, TimeUnit unit) {
      if (this.checkTask(delay)) {
         return null;
      } else {
         return async
            ? Bukkit.getScheduler().runTaskLaterAsynchronously(this.getPlugin(), run, unit.toMillis(delay) / 50L)
            : Bukkit.getScheduler().runTaskLater(this.getPlugin(), run, unit.toMillis(delay) / 50L);
      }
   }

   @Override
   public BukkitTask runTimer(boolean async, Runnable run, long initialDelay, long delay, TimeUnit unit) {
      if (this.checkTask(initialDelay, delay)) {
         return null;
      } else {
         return async
            ? Bukkit.getScheduler().runTaskTimerAsynchronously(this.getPlugin(), run, unit.toMillis(initialDelay) / 50L, unit.toMillis(delay) / 50L)
            : Bukkit.getScheduler().runTaskTimer(this.getPlugin(), run, unit.toMillis(initialDelay) / 50L, unit.toMillis(delay) / 50L);
      }
   }
}
