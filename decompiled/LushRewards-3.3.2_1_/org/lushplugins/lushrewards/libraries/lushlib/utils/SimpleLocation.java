package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

public record SimpleLocation(World world, double x, double y, double z) {
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SimpleLocation that = (SimpleLocation)o;
         return Double.compare(that.x, this.x) == 0
            && Double.compare(that.y, this.y) == 0
            && Double.compare(that.z, this.z) == 0
            && Objects.equals(this.world, that.world);
      } else {
         return false;
      }
   }

   public static SimpleLocation adapt(Location location) {
      return new SimpleLocation(location.getWorld(), location.getX(), location.getY(), location.getZ());
   }
}
