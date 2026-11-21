package com.artillexstudios.axtrade.libs.axapi.selection;

import org.bukkit.Location;
import org.bukkit.World;

public class Cuboid {
   private final int maxX;
   private final int minX;
   private final int maxZ;
   private final int minZ;
   private final int maxY;
   private final int minY;
   private final World world;

   public Cuboid(World world, int x1, int x2, int z1, int z2, int y1, int y2) {
      this.world = world;
      this.minX = Math.min(x1, x2);
      this.maxX = Math.max(x1, x2);
      this.minZ = Math.min(z1, z2);
      this.maxZ = Math.max(z1, z2);
      this.minY = Math.min(y1, y2);
      this.maxY = Math.max(y1, y2);
   }

   public int getMaxX() {
      return this.maxX;
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMaxZ() {
      return this.maxZ;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public int getMaxY() {
      return this.maxY;
   }

   public int getMinY() {
      return this.minY;
   }

   public World getWorld() {
      return this.world;
   }

   public boolean contains(Location location) {
      return location.getBlockX() <= this.getMaxX()
         && location.getBlockX() >= this.getMinX()
         && location.getBlockY() <= this.getMaxY()
         && location.getBlockY() >= this.getMinY()
         && location.getBlockZ() <= this.getMaxZ()
         && location.getBlockZ() >= this.getMinZ();
   }
}
