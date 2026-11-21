package com.magmaguy.elitemobs.advancedcombat;

final class AdvancedCombatShutdownPolicy {
   private boolean softReloadPrepared;

   void prepareSoftReload() {
      this.softReloadPrepared = true;
   }

   AdvancedCombatShutdownPolicy.HealthCleanup consumeHealthCleanup() {
      AdvancedCombatShutdownPolicy.HealthCleanup cleanup = this.softReloadPrepared
         ? AdvancedCombatShutdownPolicy.HealthCleanup.PRESERVE_FOR_SOFT_RELOAD
         : AdvancedCombatShutdownPolicy.HealthCleanup.RESTORE_ORDINARY_HEALTH;
      this.softReloadPrepared = false;
      return cleanup;
   }

   static enum HealthCleanup {
      RESTORE_ORDINARY_HEALTH,
      PRESERVE_FOR_SOFT_RELOAD;
   }
}
