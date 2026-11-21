package com.magmaguy.elitemobs.utils;

import org.bukkit.entity.EnderDragon.Phase;

public class EnderDragonPhaseSimplifier {
   private EnderDragonPhaseSimplifier() {
   }

   public static boolean isFlying(Phase phase, boolean strict) {
      switch (phase) {
         case HOVER:
         case CIRCLING:
         case STRAFING:
            return true;
         default:
            return !strict ? false : phase.equals(Phase.LAND_ON_PORTAL) || phase.equals(Phase.LEAVE_PORTAL) || phase.equals(Phase.CHARGE_PLAYER);
      }
   }
}
