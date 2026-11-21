package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.scheduler.BukkitRunnable;

public class WorkloadRunnable extends BukkitRunnable {
   private final Deque<Workload> workloadDeque = new ArrayDeque<>();
   private final long maxMillisPerTick;
   private final Runnable onComplete;

   public WorkloadRunnable(double percentageOfTick, Runnable onComplete) {
      this.maxMillisPerTick = Math.max((long)(0.05 * percentageOfTick * 1000.0), 2L);
      this.onComplete = onComplete;
   }

   public void addWorkload(Workload workload) {
      this.workloadDeque.add(workload);
   }

   public void startSync() {
      this.runTaskTimer(MagmaCore.getInstance().getRequestingPlugin(), 0L, 1L);
   }

   public void startAsync() {
      this.runTaskTimerAsynchronously(MagmaCore.getInstance().getRequestingPlugin(), 0L, 1L);
   }

   public void run() {
      long stopTime = System.currentTimeMillis() + this.maxMillisPerTick;

      while (!this.workloadDeque.isEmpty() && System.currentTimeMillis() < stopTime) {
         Workload workload = this.workloadDeque.poll();
         if (workload != null) {
            workload.compute();
         }
      }

      if (this.workloadDeque.isEmpty()) {
         if (this.onComplete != null) {
            this.onComplete.run();
         }

         this.cancel();
      }
   }
}
