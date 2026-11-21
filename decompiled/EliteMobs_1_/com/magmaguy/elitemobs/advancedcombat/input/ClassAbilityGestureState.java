package com.magmaguy.elitemobs.advancedcombat.input;

import java.util.Objects;

public final class ClassAbilityGestureState {
   public static final long CHORD_WINDOW_TICKS = 12L;
   private static final long CLOSED_TICK = Long.MIN_VALUE;
   private static final ClassAbilityGestureState CLOSED = new ClassAbilityGestureState(Long.MIN_VALUE);
   private final long openedAtTick;

   private ClassAbilityGestureState(long openedAtTick) {
      this.openedAtTick = openedAtTick;
   }

   public static ClassAbilityGestureState closed() {
      return CLOSED;
   }

   public ClassAbilityGestureState.Transition pressF(long currentTick, boolean sneaking) {
      return !sneaking && this.isOpenAt(currentTick)
         ? new ClassAbilityGestureState.Transition(CLOSED, ClassAbilityGestureState.Outcome.MOBILITY)
         : new ClassAbilityGestureState.Transition(new ClassAbilityGestureState(currentTick), ClassAbilityGestureState.Outcome.CHORD_OPENED);
   }

   public ClassAbilityGestureState.Transition leftClick(long currentTick) {
      return this.inWindowSelection(currentTick, ClassAbilityGestureState.Outcome.SIGNATURE);
   }

   public ClassAbilityGestureState.Transition rightClick(long currentTick) {
      return this.inWindowSelection(currentTick, ClassAbilityGestureState.Outcome.UTILITY);
   }

   private ClassAbilityGestureState.Transition inWindowSelection(long currentTick, ClassAbilityGestureState.Outcome outcome) {
      return this.isOpenAt(currentTick)
         ? new ClassAbilityGestureState.Transition(CLOSED, outcome)
         : new ClassAbilityGestureState.Transition(CLOSED, ClassAbilityGestureState.Outcome.PASS_THROUGH);
   }

   public ClassAbilityGestureState.Transition selectHotbar(long currentTick, int zeroBasedHotbarSlot) {
      if (!this.isOpenAt(currentTick)) {
         return new ClassAbilityGestureState.Transition(CLOSED, ClassAbilityGestureState.Outcome.PASS_THROUGH);
      } else {
         ClassAbilityGestureState.Outcome outcome = switch (zeroBasedHotbarSlot) {
            case 0, 6 -> ClassAbilityGestureState.Outcome.MOBILITY;
            case 1, 7 -> ClassAbilityGestureState.Outcome.SIGNATURE;
            case 2, 8 -> ClassAbilityGestureState.Outcome.UTILITY;
            default -> ClassAbilityGestureState.Outcome.PASS_THROUGH;
         };
         return new ClassAbilityGestureState.Transition(CLOSED, outcome);
      }
   }

   public boolean isOpenAt(long currentTick) {
      return this.openedAtTick != Long.MIN_VALUE && currentTick >= this.openedAtTick ? currentTick - this.openedAtTick <= 12L : false;
   }

   public static enum Outcome {
      CHORD_OPENED,
      MOBILITY,
      SIGNATURE,
      UTILITY,
      PASS_THROUGH;
   }

   public record Transition(ClassAbilityGestureState next, ClassAbilityGestureState.Outcome outcome) {
      public Transition(ClassAbilityGestureState next, ClassAbilityGestureState.Outcome outcome) {
         Objects.requireNonNull(next, "next");
         Objects.requireNonNull(outcome, "outcome");
         this.next = next;
         this.outcome = outcome;
      }

      public boolean consumesInput() {
         return this.outcome == ClassAbilityGestureState.Outcome.MOBILITY
            || this.outcome == ClassAbilityGestureState.Outcome.SIGNATURE
            || this.outcome == ClassAbilityGestureState.Outcome.UTILITY;
      }
   }
}
