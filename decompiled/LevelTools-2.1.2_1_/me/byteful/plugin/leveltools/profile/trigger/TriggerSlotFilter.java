package me.byteful.plugin.leveltools.profile.trigger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerSlotFilter {
   private static final TriggerSlotFilter ALL = new TriggerSlotFilter(false, Collections.emptySet());
   private final boolean restricted;
   private final Set<TriggerSlot> slots;

   private TriggerSlotFilter(boolean restricted, @NotNull Set<TriggerSlot> slots) {
      this.restricted = restricted;
      this.slots = Collections.unmodifiableSet(slots);
   }

   @NotNull
   public static TriggerSlotFilter all() {
      return ALL;
   }

   @NotNull
   public static TriggerSlotFilter restricted(@NotNull Set<TriggerSlot> slots) {
      return slots.isEmpty() ? all() : new TriggerSlotFilter(true, EnumSet.copyOf(slots));
   }

   public boolean isRestricted() {
      return this.restricted;
   }

   @NotNull
   public Set<TriggerSlot> getSlots() {
      return this.slots;
   }

   public boolean matches(@Nullable TriggerSlot slot) {
      if (!this.restricted) {
         return true;
      } else {
         return slot == null ? true : this.slots.contains(slot);
      }
   }
}
