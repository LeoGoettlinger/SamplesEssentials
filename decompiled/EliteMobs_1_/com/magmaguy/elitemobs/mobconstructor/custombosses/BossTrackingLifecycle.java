package com.magmaguy.elitemobs.mobconstructor.custombosses;

import java.util.Objects;
import java.util.function.Supplier;

final class BossTrackingLifecycle<T extends BossTrackingLifecycle.Handle> {
   private final Runnable register;
   private final Runnable unregister;
   private final Supplier<T> handleFactory;
   private T current;

   BossTrackingLifecycle(Runnable register, Runnable unregister, Supplier<T> handleFactory) {
      this.register = Objects.requireNonNull(register, "register");
      this.unregister = Objects.requireNonNull(unregister, "unregister");
      this.handleFactory = Objects.requireNonNull(handleFactory, "handleFactory");
   }

   void activate(boolean notifyPlayers) {
      if (this.current != null) {
         this.deactivate();
      }

      this.register.run();

      try {
         this.current = Objects.requireNonNull(this.handleFactory.get(), "handleFactory returned null");
      } catch (Error | RuntimeException var3) {
         this.unregister.run();
         throw var3;
      }

      if (notifyPlayers) {
         this.current.notifyPlayers();
      }
   }

   void deactivate() {
      if (this.current != null) {
         T previous = this.current;
         this.current = null;
         previous.dispose();
      }

      this.unregister.run();
   }

   void deactivate(T expected) {
      if (expected != null) {
         if (this.current == expected) {
            this.deactivate();
         } else {
            expected.dispose();
         }
      }
   }

   T current() {
      return this.current;
   }

   interface Handle {
      void dispose();

      void notifyPlayers();
   }
}
