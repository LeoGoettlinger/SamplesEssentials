package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.mind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class NativeMindServerBudget {
   private int serverTick = Integer.MIN_VALUE;
   private long spentNanos;
   private int nextFirstSession;

   <T> List<T> fairOrder(List<T> sessions) {
      Objects.requireNonNull(sessions, "sessions");
      if (sessions.isEmpty()) {
         return List.of();
      } else {
         int first = Math.floorMod(this.nextFirstSession, sessions.size());
         this.nextFirstSession = first + 1 == sessions.size() ? 0 : first + 1;
         ArrayList<T> ordered = new ArrayList<>(sessions.size());

         for (int offset = 0; offset < sessions.size(); offset++) {
            ordered.add(sessions.get((first + offset) % sessions.size()));
         }

         return Collections.unmodifiableList(ordered);
      }
   }

   boolean permits(int currentServerTick, long maximumNanos) {
      this.resetIfNeeded(currentServerTick);
      return this.spentNanos < maximumNanos;
   }

   void record(int currentServerTick, long elapsedNanos) {
      this.resetIfNeeded(currentServerTick);
      long next = this.spentNanos + Math.max(0L, elapsedNanos);
      this.spentNanos = next < this.spentNanos ? Long.MAX_VALUE : next;
   }

   private void resetIfNeeded(int currentServerTick) {
      if (this.serverTick != currentServerTick) {
         this.serverTick = currentServerTick;
         this.spentNanos = 0L;
      }
   }
}
