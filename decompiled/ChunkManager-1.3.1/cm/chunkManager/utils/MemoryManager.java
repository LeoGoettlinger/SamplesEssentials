package cm.chunkManager.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class MemoryManager {
   private final JavaPlugin plugin;
   private final Logger logger;
   private final AtomicLong lastGCTime = new AtomicLong(0L);
   private final long minGCInterval;
   private volatile boolean lowMemoryMode = false;
   private final double lowMemoryThreshold;
   private final double criticalMemoryThreshold;

   public MemoryManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.minGCInterval = plugin.getConfig().getLong("memory.gc-min-interval", 30000L);
      this.lowMemoryThreshold = plugin.getConfig().getDouble("memory.low-threshold", 0.85);
      this.criticalMemoryThreshold = plugin.getConfig().getDouble("memory.critical-threshold", 0.95);
      this.registerMemoryNotifications();
   }

   private void registerMemoryNotifications() {
      try {
         for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType().toString().equals("HEAP") && pool.isUsageThresholdSupported()) {
               long maxMemory = pool.getUsage().getMax();
               if (maxMemory > 0L) {
                  pool.setUsageThreshold((long)(maxMemory * this.lowMemoryThreshold));
               }
            }
         }
      } catch (Exception var6) {
         this.logger.warning("Could not set memory threshold: " + var6.getMessage());
      }
   }

   public boolean isLowMemoryMode() {
      return this.lowMemoryMode;
   }

   public void checkMemoryStatus() {
      double usagePercentage = this.getMemoryUsagePercentage();
      if (usagePercentage > this.criticalMemoryThreshold) {
         this.logger.warning("Critical memory usage detected: " + String.format("%.2f%%", usagePercentage * 100.0));
         this.suggestGarbageCollection();
         this.lowMemoryMode = true;
      } else if (usagePercentage > this.lowMemoryThreshold) {
         if (!this.lowMemoryMode) {
            this.logger.info("Entering low memory mode: " + String.format("%.2f%%", usagePercentage * 100.0));
            this.lowMemoryMode = true;
         }
      } else if (this.lowMemoryMode && usagePercentage < 0.7) {
         this.logger.info("Exiting low memory mode: " + String.format("%.2f%%", usagePercentage * 100.0));
         this.lowMemoryMode = false;
      }
   }

   public double getMemoryUsagePercentage() {
      Runtime runtime = Runtime.getRuntime();
      long maxMemory = runtime.maxMemory();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long usedMemory = totalMemory - freeMemory;
      return (double)usedMemory / maxMemory;
   }

   public void requestGarbageCollection() {
      long currentTime = System.currentTimeMillis();
      long lastGC = this.lastGCTime.get();
      if (currentTime - lastGC > this.minGCInterval && this.lastGCTime.compareAndSet(lastGC, currentTime)) {
         this.suggestGarbageCollection();
         this.logger.fine("Garbage collection suggested");
      }
   }

   public void suggestGarbageCollection() {
      new WeakReference<>(new byte[1048576]);
      this.lastGCTime.set(System.currentTimeMillis());
   }

   public void forceGarbageCollection() {
      this.suggestGarbageCollection();
      System.gc();
      this.lastGCTime.set(System.currentTimeMillis());
      this.logger.info("Garbage collection requested");
   }

   public MemoryManager.MemoryStats getMemoryStats() {
      Runtime runtime = Runtime.getRuntime();
      long maxMemory = runtime.maxMemory();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long usedMemory = totalMemory - freeMemory;
      return new MemoryManager.MemoryStats(
         usedMemory / 1048576.0, totalMemory / 1048576.0, maxMemory / 1048576.0, freeMemory / 1048576.0, this.getMemoryUsagePercentage() * 100.0
      );
   }

   public void cleanup() {
   }

   public static class MemoryStats {
      public final double usedMB;
      public final double totalMB;
      public final double maxMB;
      public final double freeMB;
      public final double usagePercentage;

      public MemoryStats(double usedMB, double totalMB, double maxMB, double freeMB, double usagePercentage) {
         this.usedMB = usedMB;
         this.totalMB = totalMB;
         this.maxMB = maxMB;
         this.freeMB = freeMB;
         this.usagePercentage = usagePercentage;
      }
   }
}
