package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;

public interface Locatable {
   Location getLocation();

   default Location getBlockLocation() {
      Location location = this.getLocation();
      return location.setPosition(location.toVector().floor());
   }

   boolean setLocation(Location location);

   @Deprecated
   default void setPosition(Vector3 pos) {
      this.trySetPosition(pos);
   }

   default boolean trySetPosition(Vector3 pos) {
      return this.setLocation(new Location(this.getExtent(), pos));
   }

   Extent getExtent();
}
