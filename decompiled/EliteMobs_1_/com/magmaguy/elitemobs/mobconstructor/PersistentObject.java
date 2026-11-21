package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.Location;
import org.bukkit.World;

public interface PersistentObject {
   void chunkLoad();

   void chunkUnload();

   void worldLoad(World var1);

   void worldUnload();

   Location getPersistentLocation();

   String getWorldName();
}
