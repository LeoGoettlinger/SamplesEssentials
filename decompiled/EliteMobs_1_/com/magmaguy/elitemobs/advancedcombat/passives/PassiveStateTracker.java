package com.magmaguy.elitemobs.advancedcombat.passives;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

final class PassiveStateTracker {
   static final long RECENT_HIT_NANOS = 3000000000L;
   static final long RECENT_ELITE_KILL_NANOS = 6000000000L;
   private static final long NANOS_PER_TICK = 50000000L;
   private final LongSupplier nanoTime;
   private final Map<UUID, Long> recentlyHitUntil = new HashMap<>();
   private final Map<UUID, Long> recentEliteKillUntil = new HashMap<>();
   private final Map<UUID, Long> wardBrokenUntil = new HashMap<>();

   PassiveStateTracker() {
      this(System::nanoTime);
   }

   PassiveStateTracker(LongSupplier nanoTime) {
      this.nanoTime = nanoTime;
   }

   void recordHit(UUID playerId) {
      long now = this.nanoTime.getAsLong();
      this.recentlyHitUntil.put(playerId, saturatingAdd(now, 3000000000L));
   }

   boolean recentlyHit(UUID playerId) {
      return this.active(this.recentlyHitUntil, playerId);
   }

   void recordEliteKill(UUID playerId) {
      this.recentEliteKillUntil.put(playerId, saturatingAdd(this.nanoTime.getAsLong(), 6000000000L));
   }

   boolean recentEliteKill(UUID playerId) {
      return this.active(this.recentEliteKillUntil, playerId);
   }

   void recordWardBroken(UUID playerId, int durationTicks) {
      if (durationTicks > 0) {
         this.wardBrokenUntil.put(playerId, saturatingAdd(this.nanoTime.getAsLong(), nanosForTicks(durationTicks)));
      }
   }

   boolean wardBroken(UUID playerId) {
      return this.active(this.wardBrokenUntil, playerId);
   }

   void clear(UUID playerId) {
      this.recentlyHitUntil.remove(playerId);
      this.recentEliteKillUntil.remove(playerId);
      this.wardBrokenUntil.remove(playerId);
   }

   void clearAll() {
      this.recentlyHitUntil.clear();
      this.recentEliteKillUntil.clear();
      this.wardBrokenUntil.clear();
   }

   static long nanosForTicks(int ticks) {
      if (ticks <= 0) {
         return 0L;
      } else {
         return ticks > 184467440737L ? Long.MAX_VALUE : ticks * 50000000L;
      }
   }

   private boolean active(Map<UUID, Long> deadlines, UUID playerId) {
      Long deadline = deadlines.get(playerId);
      if (deadline == null) {
         return false;
      } else if (deadline > this.nanoTime.getAsLong()) {
         return true;
      } else {
         deadlines.remove(playerId);
         return false;
      }
   }

   private static long saturatingAdd(long left, long right) {
      return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
   }
}
