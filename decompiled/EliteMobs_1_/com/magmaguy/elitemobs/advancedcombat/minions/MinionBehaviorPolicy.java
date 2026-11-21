package com.magmaguy.elitemobs.advancedcombat.minions;

public final class MinionBehaviorPolicy {
   static final double SETTLED_RADIUS = 4.0;

   private MinionBehaviorPolicy() {
   }

   public static MinionBehaviorPolicy.Intent choose(boolean combatTargetAvailable, double ownerDistance, boolean wanderReady) {
      if (combatTargetAvailable) {
         return MinionBehaviorPolicy.Intent.COMBAT;
      } else if (Double.isFinite(ownerDistance) && ownerDistance > 4.0) {
         return MinionBehaviorPolicy.Intent.FOLLOW;
      } else {
         return wanderReady ? MinionBehaviorPolicy.Intent.WANDER : MinionBehaviorPolicy.Intent.IDLE;
      }
   }

   public static enum Intent {
      COMBAT,
      FOLLOW,
      WANDER,
      IDLE;
   }
}
