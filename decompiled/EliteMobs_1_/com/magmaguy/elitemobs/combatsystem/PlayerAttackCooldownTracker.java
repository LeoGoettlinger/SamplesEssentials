package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.utils.GameClock;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class PlayerAttackCooldownTracker {
   public static final long SWEEP_INTERVAL_TICKS = 6000L;
   public static final long IDLE_EVICTION_TICKS = 6000L;
   public static final long NO_PREVIOUS_HIT = -1L;
   private static final Map<UUID, Long> LAST_HIT_TICK = new ConcurrentHashMap<>();
   private static volatile int sweepTaskId = -1;

   private PlayerAttackCooldownTracker() {
   }

   public static long recordHit(Player player) {
      ensureSweepRegistered();
      long now = GameClock.getCurrentTick();
      Long previous = LAST_HIT_TICK.put(player.getUniqueId(), now);
      return previous == null ? -1L : now - previous;
   }

   public static void forget(UUID uuid) {
      LAST_HIT_TICK.remove(uuid);
   }

   public static void clearAll() {
      LAST_HIT_TICK.clear();
   }

   public static void shutdown() {
      int taskId = sweepTaskId;
      if (taskId != -1) {
         GameClock.cancel(taskId);
         sweepTaskId = -1;
      }

      clearAll();
   }

   private static void ensureSweepRegistered() {
      if (sweepTaskId == -1) {
         synchronized (PlayerAttackCooldownTracker.class) {
            if (sweepTaskId == -1) {
               sweepTaskId = GameClock.scheduleRepeating(6000L, 6000L, PlayerAttackCooldownTracker::sweep);
            }
         }
      }
   }

   private static void sweep() {
      long now = GameClock.getCurrentTick();
      LAST_HIT_TICK.entrySet().removeIf(entry -> now - entry.getValue() > 6000L);
   }
}
