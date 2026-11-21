package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public final class GameClock {
   private static final Map<Integer, GameClock.ScheduledEntry> scheduledEntries = new LinkedHashMap<>();
   private static BukkitTask clockTask = null;
   private static long currentTick = 0L;
   private static int nextTaskId = 1;

   private GameClock() {
   }

   public static void initialize() {
      if (clockTask == null) {
         clockTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, GameClock::tick, 1L, 1L);
      }
   }

   public static void shutdown() {
      if (clockTask != null) {
         clockTask.cancel();
         clockTask = null;
      }

      scheduledEntries.clear();
      currentTick = 0L;
      nextTaskId = 1;
   }

   public static long getCurrentTick() {
      return currentTick;
   }

   public static int scheduleLater(long delayTicks, Runnable runnable) {
      return scheduleRepeating(delayTicks, 0L, runnable);
   }

   public static int scheduleRepeating(long initialDelayTicks, long intervalTicks, Runnable runnable) {
      initialize();
      int taskId = nextTaskId++;
      long normalizedInitialDelay = Math.max(1L, initialDelayTicks);
      long normalizedInterval = Math.max(0L, intervalTicks);
      scheduledEntries.put(taskId, new GameClock.ScheduledEntry(taskId, runnable, currentTick + normalizedInitialDelay, normalizedInterval));
      return taskId;
   }

   public static void cancel(int taskId) {
      scheduledEntries.remove(taskId);
   }

   private static void tick() {
      currentTick++;
      ArrayList<GameClock.ScheduledEntry> dueEntries = new ArrayList<>();

      for (GameClock.ScheduledEntry scheduledEntry : new ArrayList<>(scheduledEntries.values())) {
         if (scheduledEntry.nextRunTick <= currentTick) {
            dueEntries.add(scheduledEntry);
         }
      }

      for (GameClock.ScheduledEntry scheduledEntryx : dueEntries) {
         if (scheduledEntries.containsKey(scheduledEntryx.taskId)) {
            try {
               scheduledEntryx.runnable.run();
            } catch (Exception var4) {
               Logger.warn("A GameClock task failed and was cancelled.");
               var4.printStackTrace();
               scheduledEntries.remove(scheduledEntryx.taskId);
               continue;
            }

            if (scheduledEntries.containsKey(scheduledEntryx.taskId)) {
               if (scheduledEntryx.intervalTicks < 1L) {
                  scheduledEntries.remove(scheduledEntryx.taskId);
               } else {
                  scheduledEntryx.nextRunTick = currentTick + scheduledEntryx.intervalTicks;
               }
            }
         }
      }
   }

   private static final class ScheduledEntry {
      private final int taskId;
      private final Runnable runnable;
      private long nextRunTick;
      private final long intervalTicks;

      private ScheduledEntry(int taskId, Runnable runnable, long nextRunTick, long intervalTicks) {
         this.taskId = taskId;
         this.runnable = runnable;
         this.nextRunTick = nextRunTick;
         this.intervalTicks = intervalTicks;
      }
   }
}
