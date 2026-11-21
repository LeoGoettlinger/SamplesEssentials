package com.magmaguy.elitemobs.easyminecraftgoals;

import java.util.List;
import org.bukkit.Location;

public interface PathfindingHandle extends AutoCloseable {
   boolean moveTo(Location var1, double var2);

   void clearDestination();

   PathfindingStatus status();

   default List<Location> routePreview() {
      return List.of();
   }

   @Override
   void close();
}
