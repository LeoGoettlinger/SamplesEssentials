package com.magmaguy.elitemobs.easyminecraftgoals.v26.flee;

import java.util.Objects;

final class TransientGoalRegistration implements AutoCloseable {
   private final Runnable registerAction;
   private final Runnable unregisterAction;
   private boolean registered;

   TransientGoalRegistration(Runnable registerAction, Runnable unregisterAction) {
      this.registerAction = Objects.requireNonNull(registerAction, "registerAction");
      this.unregisterAction = Objects.requireNonNull(unregisterAction, "unregisterAction");
   }

   void register() {
      if (!this.registered) {
         this.registerAction.run();
         this.registered = true;
      }
   }

   @Override
   public void close() {
      if (this.registered) {
         this.registered = false;
         this.unregisterAction.run();
      }
   }
}
