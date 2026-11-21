package com.magmaguy.elitemobs.magmacore.location;

import org.bukkit.Location;

@FunctionalInterface
public interface DungeonLocator {
   boolean contains(Location var1);
}
