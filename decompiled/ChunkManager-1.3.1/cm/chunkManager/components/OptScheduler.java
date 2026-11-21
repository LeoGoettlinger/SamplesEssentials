package cm.chunkManager.components;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.Scheduler;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class OptScheduler {
   private final ChunkManager plugin;
   private final Set<String> executedToday = new HashSet<>();
   private LocalDate lastResetDate = LocalDate.now();
   private Scheduler.Handle timerHandle;

   public OptScheduler(ChunkManager plugin) {
      this.plugin = plugin;
      this.start();
   }

   private void start() {
      this.timerHandle = Scheduler.runGlobalTimer(this.plugin, this::check, 1200L, 1200L);
   }

   private void check() {
      LocalDate today = LocalDate.now();
      if (!today.equals(this.lastResetDate)) {
         this.executedToday.clear();
         this.lastResetDate = today;
      }

      if (this.plugin.getConfigManager().isScheduledOptEnabled()) {
         List<String> times = this.plugin.getConfigManager().getScheduledOptTimes();
         String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

         for (String time : times) {
            if (time.equals(now) && !this.executedToday.contains(time)) {
               this.executedToday.add(time);
               this.runOptimization();
               break;
            }
         }
      }
   }

   private void runOptimization() {
      int target = this.plugin.getConfigManager().getScheduledOptTarget();
      boolean log = this.plugin.getConfigManager().isScheduledOptLog();
      Scheduler.runAsync(this.plugin, () -> {
         int total = 0;

         for (World world : Bukkit.getWorlds()) {
            if (!this.plugin.isWorldBlacklisted(world)) {
               total += this.plugin.getSmartUnloader().performSmartUnload(world, target);
            }
         }

         if (log) {
            int finalTotal = total;
            Scheduler.runGlobal(this.plugin, () -> this.plugin.getLogger().info("Scheduled optimization complete: unloaded " + finalTotal + " chunks"));
         }
      });
   }

   public void shutdown() {
      if (this.timerHandle != null) {
         this.timerHandle.cancel();
      }
   }
}
