package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.magmacore.location.DungeonLocator;
import org.bukkit.Location;

public class EMDungeonLocator implements DungeonLocator {
   @Override
   public boolean contains(Location location) {
      if (location != null && location.getWorld() != null) {
         EMPackage pkg = EMPackage.getContent(location.getWorld().getName());
         return pkg instanceof WorldPackage;
      } else {
         return false;
      }
   }
}
