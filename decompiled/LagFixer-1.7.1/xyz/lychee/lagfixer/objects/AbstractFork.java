package xyz.lychee.lagfixer.objects;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class AbstractFork {
   private final Plugin plugin;

   public AbstractFork(Plugin plugin) {
      this.plugin = plugin;
   }

   public boolean checkTask(long delay) {
      return !this.getPlugin().isEnabled() || delay <= 0L;
   }

   public boolean checkTask(long initialDelay, long delay) {
      return !this.getPlugin().isEnabled() || initialDelay <= 0L || delay <= 0L;
   }

   public abstract boolean isSupported();

   public abstract int getPriority();

   public abstract double getMspt();

   public abstract boolean isSupportMspt();

   public abstract PluginCommand registerCommand(Plugin var1, String var2, List<String> var3, CommandExecutor var4);

   public abstract void unregisterCommand(String var1);

   public abstract BukkitTask runNow(boolean var1, Location var2, Runnable var3);

   public abstract BukkitTask runLater(boolean var1, Runnable var2, long var3);

   public abstract BukkitTask runTimer(boolean var1, Runnable var2, long var3, long var5);

   public abstract BukkitTask runLater(boolean var1, Runnable var2, long var3, TimeUnit var5);

   public abstract BukkitTask runTimer(boolean var1, Runnable var2, long var3, long var5, TimeUnit var7);

   @Generated
   public Plugin getPlugin() {
      return this.plugin;
   }
}
