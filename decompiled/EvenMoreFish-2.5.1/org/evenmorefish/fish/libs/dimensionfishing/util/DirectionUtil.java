package org.evenmorefish.fish.libs.dimensionfishing.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DirectionUtil {
   public static Location forwardFlat(@NotNull Location location, double distance) {
      Location loc = location.clone();
      loc.setPitch(0.0F);
      Vector vector = loc.getDirection().multiply(distance);
      return location.clone().add(vector);
   }
}
