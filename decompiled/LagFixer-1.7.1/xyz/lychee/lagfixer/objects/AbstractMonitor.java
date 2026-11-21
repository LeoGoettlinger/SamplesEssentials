package xyz.lychee.lagfixer.objects;

import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.managers.SupportManager;

public abstract class AbstractMonitor implements Runnable {
   private final boolean async;
   private final String name;
   private BukkitTask task;

   public AbstractMonitor(boolean async, String name) {
      this.async = async;
      this.name = name;
   }

   @Override
   public abstract void run();

   public void start() {
      FileConfiguration config = SupportManager.getInstance().getPlugin().getConfig();
      int interval = config.getBoolean("main.monitor." + this.name + ".enabled") ? config.getInt("main.monitor." + this.name + ".interval") : 0;
      if (interval > 0) {
         this.task = SupportManager.getInstance().getFork().runTimer(this.async, this, interval / 2, interval, TimeUnit.SECONDS);
      }
   }

   public void stop() {
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }
   }

   @Generated
   public boolean isAsync() {
      return this.async;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }
}
