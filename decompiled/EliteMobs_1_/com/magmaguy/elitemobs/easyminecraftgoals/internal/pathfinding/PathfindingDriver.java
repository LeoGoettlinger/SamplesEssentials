package com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding;

import org.bukkit.Location;

public interface PathfindingDriver {
   boolean canNavigate();

   boolean hasCombatTarget();

   boolean preparePath(Location var1);

   boolean startPreparedPath(double var1);

   boolean navigationDone();

   void stopNavigation();

   void unregisterGoal();
}
