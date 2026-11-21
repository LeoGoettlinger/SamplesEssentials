package com.ranull.graves.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

public class LocationData implements Serializable {
   private static final long serialVersionUID = 1L;
   private final UUID uuid;
   private final String worldKey;
   private final String worldName;
   private final float yaw;
   private final float pitch;
   private final double x;
   private final double y;
   private final double z;

   public LocationData(Location location) {
      World w = location.getWorld();
      this.uuid = w != null ? w.getUID() : null;
      this.worldKey = w != null && safeGetKey(w) != null ? Objects.requireNonNull(safeGetKey(w)).toString() : null;
      this.worldName = w != null ? w.getName() : null;
      this.yaw = location.getYaw();
      this.pitch = location.getPitch();
      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();
   }

   public Location getLocation() {
      World world = null;
      if (this.uuid != null) {
         world = Bukkit.getWorld(this.uuid);
      }

      if (world == null && this.worldKey != null) {
         try {
            NamespacedKey key = NamespacedKey.fromString(this.worldKey);
            if (key != null) {
               world = Bukkit.getWorld(String.valueOf(key));
            }
         } catch (Throwable var3) {
         }
      }

      if (world == null && this.worldName != null) {
         world = Bukkit.getWorld(this.worldName);
      }

      return world != null ? new Location(world, this.x, this.y, this.z, this.yaw, this.pitch) : null;
   }

   private static NamespacedKey safeGetKey(World world) {
      try {
         return world.getKey();
      } catch (Throwable var2) {
         return null;
      }
   }
}
