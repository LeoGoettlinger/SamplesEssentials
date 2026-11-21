package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClassHudPresentation {
   private static final long REMINDER_INTERVAL_NANOS = Duration.ofMinutes(3L).toNanos();
   private static final long NANOS_PER_TICK = 50000000L;
   private final Map<UUID, ClassHudPresentation.DisplayState> players = new HashMap<>();

   public String render(UUID playerId, ClassHudPresentation.Vitals vitals, String compact, String reminder, long now) {
      ClassHudPresentation.DisplayState state = this.players.get(playerId);
      if (state != null && state.vitals.equals(vitals)) {
         String expanded = compact + " &8| " + reminder;
         if (now - state.lastReminderOrChange >= REMINDER_INTERVAL_NANOS) {
            state.lastReminderOrChange = now;
            state.reminderUntil = now + ActionBarCompositor.readingDurationTicks(expanded) * 50000000L;
            state.showingReminder = true;
         }

         if (state.showingReminder && now - state.reminderUntil >= 0L) {
            state.showingReminder = false;
         }

         return state.showingReminder ? expanded : compact;
      } else {
         this.players.put(playerId, new ClassHudPresentation.DisplayState(vitals, now));
         return compact;
      }
   }

   public void discard(UUID playerId) {
      this.players.remove(playerId);
   }

   public void clear() {
      this.players.clear();
   }

   private static final class DisplayState {
      private final ClassHudPresentation.Vitals vitals;
      private long lastReminderOrChange;
      private long reminderUntil;
      private boolean showingReminder;

      private DisplayState(ClassHudPresentation.Vitals vitals, long now) {
         this.vitals = vitals;
         this.lastReminderOrChange = now;
      }
   }

   public record Vitals(double health, double maximumHealth, double absorption, double resource, double maximumResource, String classFormId) {
   }
}
