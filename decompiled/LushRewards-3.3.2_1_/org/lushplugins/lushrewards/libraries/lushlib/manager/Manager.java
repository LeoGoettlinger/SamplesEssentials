package org.lushplugins.lushrewards.libraries.lushlib.manager;

public abstract class Manager {
   private boolean enabled = false;

   public void onEnable() {
   }

   public void onDisable() {
   }

   public final boolean isEnabled() {
      return this.enabled;
   }

   public final void enable() {
      if (!this.isEnabled()) {
         this.enabled = true;
         this.onEnable();
      } else {
         throw new IllegalStateException("This Manager is already enabled");
      }
   }

   public final void disable() {
      if (this.isEnabled()) {
         this.enabled = false;
         this.onDisable();
      }
   }
}
