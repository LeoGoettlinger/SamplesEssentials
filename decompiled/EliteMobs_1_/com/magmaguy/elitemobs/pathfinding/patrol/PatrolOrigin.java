package com.magmaguy.elitemobs.pathfinding.patrol;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record PatrolOrigin(String worldName, double x, double y, double z, float yaw, float pitch) {
   public PatrolOrigin(String worldName, double x, double y, double z, float yaw, float pitch) {
      Objects.requireNonNull(worldName, "worldName");
      if (worldName.isBlank()) {
         throw new IllegalArgumentException("worldName cannot be blank");
      } else if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
         this.worldName = worldName;
         this.x = x;
         this.y = y;
         this.z = z;
         this.yaw = yaw;
         this.pitch = pitch;
      } else {
         throw new IllegalArgumentException("Patrol origin coordinates must be finite");
      }
   }

   public static PatrolOrigin from(Location location, String fallbackWorldName) {
      Objects.requireNonNull(location, "location");
      String worldName = location.getWorld() == null ? fallbackWorldName : location.getWorld().getName();
      return new PatrolOrigin(
         Objects.requireNonNull(worldName, "Patrol origin has no world"),
         location.getX(),
         location.getY(),
         location.getZ(),
         location.getYaw(),
         location.getPitch()
      );
   }

   public Location resolve() {
      World world = Bukkit.getWorld(this.worldName);
      return world == null ? null : new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
   }

   public String identityFragment() {
      return this.worldName + "|" + Double.toHexString(this.x) + "|" + Double.toHexString(this.y) + "|" + Double.toHexString(this.z);
   }
}
