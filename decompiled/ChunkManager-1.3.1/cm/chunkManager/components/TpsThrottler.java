package cm.chunkManager.components;

import org.bukkit.plugin.java.JavaPlugin;

public class TpsThrottler {
   private final JavaPlugin plugin;
   private final PerformanceMonitor monitor;
   private final double minTps;
   private final double targetTps;

   public TpsThrottler(JavaPlugin plugin, PerformanceMonitor monitor, double minTps, double targetTps) {
      this.plugin = plugin;
      this.monitor = monitor;
      this.minTps = minTps;
      this.targetTps = targetTps;
   }

   public double getCurrentTps() {
      double tickTime = this.monitor.getAverageTickTime();
      return tickTime <= 0.0 ? 20.0 : Math.min(20.0, 1000.0 / tickTime);
   }

   public double getThrottleFactor() {
      double tps = this.getCurrentTps();
      if (tps >= this.targetTps) {
         return 1.0;
      } else if (tps <= this.minTps) {
         return 0.1;
      } else {
         double range = this.targetTps - this.minTps;
         double offset = tps - this.minTps;
         return 0.1 + offset / range * 0.9;
      }
   }

   public boolean shouldThrottle() {
      return this.getCurrentTps() < this.minTps;
   }

   public int throttle(int desired) {
      double factor = this.getThrottleFactor();
      return Math.max(1, (int)Math.round(desired * factor));
   }
}
