package com.magmaguy.elitemobs.easyminecraftgoals;

import org.bukkit.Location;

public interface TransientMovementOverride extends AutoCloseable {
   boolean isActive();

   boolean retarget(Location var1);

   @Override
   void close();
}
