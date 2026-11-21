package com.magmaguy.elitemobs.magmacore.location;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionProtectionProvider {
   boolean isProtected(Location var1);

   default boolean canBuild(Player player, Location location) {
      return !this.isProtected(location);
   }

   String providerName();
}
