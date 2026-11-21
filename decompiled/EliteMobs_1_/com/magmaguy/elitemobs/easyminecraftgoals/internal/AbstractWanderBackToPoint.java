package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface AbstractWanderBackToPoint {
   double getMaximumDistanceFromPoint();

   long getLastTime();

   LivingEntity getLivingEntity();

   int getPriority();

   int getMaxDurationTicks();

   float getSpeed();

   AbstractWanderBackToPoint setSpeed(float var1);

   Location getReturnLocation();

   AbstractWanderBackToPoint setReturnLocation(Location var1);

   int getStopReturnDistance();

   AbstractWanderBackToPoint setStopReturnDistance(int var1);

   int getGoalRefreshCooldownTicks();

   AbstractWanderBackToPoint setGoalRefreshCooldownTicks(int var1);

   boolean isHardObjective();

   AbstractWanderBackToPoint setHardObjective(boolean var1);

   default boolean isReturnDuringCombat() {
      return false;
   }

   default AbstractWanderBackToPoint setReturnDuringCombat(boolean returnDuringCombat) {
      return this;
   }

   boolean isTeleportOnFail();

   AbstractWanderBackToPoint setTeleportOnFail(boolean var1);

   boolean isStartWithCooldown();

   AbstractWanderBackToPoint setStartWithCooldown(boolean var1);

   void updateCooldown();

   void register();

   void unregister();
}
