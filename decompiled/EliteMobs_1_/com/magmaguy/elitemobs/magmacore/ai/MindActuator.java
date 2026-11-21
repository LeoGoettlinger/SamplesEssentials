package com.magmaguy.elitemobs.magmacore.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface MindActuator {
   boolean moveTo(Location var1, double var2);

   default boolean steerFlight(Vector velocity) {
      return false;
   }

   void stopMoving();

   void lookAt(Location var1);

   void jump();

   void setTarget(LivingEntity var1);

   void clearTarget();

   void attack(LivingEntity var1);

   void stopAll();
}
