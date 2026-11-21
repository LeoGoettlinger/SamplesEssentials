package com.magmaguy.elitemobs.mobconstructor.custombosses;

import org.bukkit.Location;

final class MusicPlaybackEligibility {
   private MusicPlaybackEligibility() {
   }

   static boolean isWithinRange(Location listener, Location source, double range) {
      if (listener == null || source == null || range < 0.0) {
         return false;
      } else if (listener.getWorld() != null && source.getWorld() != null) {
         return !listener.getWorld().equals(source.getWorld()) ? false : listener.distanceSquared(source) <= range * range;
      } else {
         return false;
      }
   }
}
