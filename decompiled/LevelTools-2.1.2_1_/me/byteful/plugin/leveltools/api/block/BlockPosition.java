package me.byteful.plugin.leveltools.api.block;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockPosition {
   private final String world;
   private final int x;
   private final int y;
   private final int z;

   public BlockPosition(String world, int x, int y, int z) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static BlockPosition fromBukkit(Block block) {
      Location l = block.getLocation();
      return new BlockPosition(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
   }

   public Block toBukkit() {
      World bw = Bukkit.getWorld(this.world);
      return bw == null ? null : new Location(bw, this.x, this.y, this.z).getBlock();
   }

   public String getWorld() {
      return this.world;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         BlockPosition that = (BlockPosition)o;
         return this.x == that.x && this.y == that.y && this.z == that.z && Objects.equals(this.world, that.world);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.world, this.x, this.y, this.z);
   }

   @Override
   public String toString() {
      return "BlockPosition{world='" + this.world + '\'' + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
   }
}
