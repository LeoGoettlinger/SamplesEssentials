package com.earth2me.essentials.config.entities;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LazyLocation {
   private String world;
   private String worldName;
   private final double x;
   private final double y;
   private final double z;
   private final float yaw;
   private final float pitch;

   public LazyLocation(String worldId, String worldName, double x, double y, double z, float yaw, float pitch) {
      this.world = worldId;
      this.worldName = worldName;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public String world() {
      return this.world;
   }

   public String worldName() {
      return this.worldName;
   }

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }

   public Location location() {
      if (this.world != null && !this.world.isEmpty()) {
         World world = null;

         try {
            UUID worldId = UUID.fromString(this.world);
            world = Bukkit.getWorld(worldId);
         } catch (IllegalArgumentException var3) {
         }

         if (world == null) {
            world = Bukkit.getWorld(this.world);
         }

         if (world == null && this.worldName != null && !this.worldName.isEmpty()) {
            world = Bukkit.getWorld(this.worldName);
         }

         if (world == null) {
            return null;
         } else {
            this.world = world.getUID().toString();
            this.worldName = world.getName();
            return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
         }
      } else {
         return null;
      }
   }

   public static LazyLocation fromLocation(final Location location) {
      return new LazyLocation(
         location.getWorld().getUID().toString(),
         location.getWorld().getName(),
         location.getX(),
         location.getY(),
         location.getZ(),
         location.getYaw(),
         location.getPitch()
      );
   }
}
