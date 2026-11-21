package com.magmaguy.elitemobs.advancedcombat.abilities;

record GuardianFlightInputState(boolean jumpHeld, boolean sneakHeld) {
   GuardianFlightInputState.Transition update(boolean jump, boolean sneak) {
      GuardianFlightInputState.Intent intent;
      if (jump && !this.jumpHeld) {
         intent = GuardianFlightInputState.Intent.FORWARD_CANCEL;
      } else if (sneak && !this.sneakHeld) {
         intent = GuardianFlightInputState.Intent.UPWARD_CANCEL;
      } else {
         intent = GuardianFlightInputState.Intent.NONE;
      }

      return new GuardianFlightInputState.Transition(new GuardianFlightInputState(jump, sneak), intent);
   }

   static enum Intent {
      NONE,
      FORWARD_CANCEL,
      UPWARD_CANCEL;
   }

   record Transition(GuardianFlightInputState next, GuardianFlightInputState.Intent intent) {
   }
}
