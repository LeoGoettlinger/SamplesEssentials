package com.magmaguy.elitemobs.pathfinding.patrol;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

interface PatrolActor {
   UUID runtimeId();

   String canonicalIdentity();

   String displayName();

   PatrolOrigin origin();

   PatrolRoute route();

   LivingEntity body();

   boolean persistsWhileDetached();

   boolean isInCombat();

   default boolean isNearPlayer(long tick) {
      return false;
   }

   default void tickFacing(boolean paused) {
   }

   boolean prepareBody();

   String ineligibleReason();

   double currentMovementSpeed();

   void updatePersistentLocation(Location var1);

   void materialize();

   void syncVisuals();

   Object owner();
}
