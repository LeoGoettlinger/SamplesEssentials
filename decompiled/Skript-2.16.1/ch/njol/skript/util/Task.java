package ch.njol.skript.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.util.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public abstract class Task implements Runnable, Closeable {
   private final boolean async;
   private final Plugin plugin;
   private boolean useScriptLoaderExecutor;
   private long period = -1L;
   private int taskID = -1;

   public Task(Plugin plugin, long delay, long period) {
      this(plugin, delay, period, false);
   }

   public Task(Plugin plugin, long delay, long period, boolean async) {
      this.plugin = plugin;
      this.period = period;
      this.async = async;
      this.schedule(delay);
   }

   public Task(Plugin plugin, long delay) {
      this(plugin, false, delay, false);
   }

   public Task(Plugin plugin, boolean useScriptLoaderExecutor) {
      this(plugin, useScriptLoaderExecutor, 0L, false);
   }

   public Task(Plugin plugin, long delay, boolean async) {
      this(plugin, delay, -1L, async);
   }

   public Task(Plugin plugin, boolean useScriptLoaderExecutor, long delay) {
      this(plugin, useScriptLoaderExecutor, delay, false);
   }

   private Task(Plugin plugin, boolean useScriptLoaderExecutor, long delay, boolean async) {
      this.useScriptLoaderExecutor = useScriptLoaderExecutor;
      this.plugin = plugin;
      this.async = async;
      this.schedule(delay);
   }

   private void schedule(long delay) {
      assert !this.isAlive();

      if (Skript.getInstance().isEnabled()) {
         if (this.useScriptLoaderExecutor) {
            Executor executor = ScriptLoader.getExecutor();
            if (delay > 0L) {
               this.taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> executor.execute(this), delay);
            } else {
               executor.execute(this);
            }
         } else {
            if (this.period == -1L) {
               if (this.async) {
                  this.taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, this, delay).getTaskId();
               } else {
                  this.taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, this, delay);
               }
            } else if (this.async) {
               this.taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this, delay, this.period).getTaskId();
            } else {
               this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this, delay, this.period);
            }

            assert this.taskID != -1;
         }
      }
   }

   public final boolean isAlive() {
      return this.taskID == -1 ? false : Bukkit.getScheduler().isQueued(this.taskID) || Bukkit.getScheduler().isCurrentlyRunning(this.taskID);
   }

   public final void cancel() {
      if (this.taskID != -1) {
         Bukkit.getScheduler().cancelTask(this.taskID);
         this.taskID = -1;
      }
   }

   @Override
   public void close() {
      this.cancel();
   }

   public void setNextExecution(long delay) {
      assert delay >= 0L;

      this.cancel();
      this.schedule(delay);
   }

   public void setPeriod(long period) {
      assert period == -1L || period > 0L;

      if (period != this.period) {
         this.period = period;
         if (this.isAlive()) {
            this.cancel();
            if (period != -1L) {
               this.schedule(period);
            }
         }
      }
   }

   @Nullable
   public static <T> T callSync(Callable<T> c) {
      return callSync(c, Skript.getInstance());
   }

   @Nullable
   public static <T> T callSync(Callable<T> c, Plugin p) {
      if (Bukkit.isPrimaryThread()) {
         try {
            return c.call();
         } catch (Exception var7) {
            Skript.exception(var7);
         }
      }

      Future<T> f = Bukkit.getScheduler().callSyncMethod(p, c);

      try {
         while (true) {
            try {
               return f.get();
            } catch (InterruptedException var4) {
            }
         }
      } catch (ExecutionException var5) {
         Skript.exception(var5);
      } catch (CancellationException var6) {
      }

      return null;
   }
}
