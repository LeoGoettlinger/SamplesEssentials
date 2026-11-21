package github.nighter.smartspawner.utils;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record BlockPos(int x, int y, int z, UUID worldId) {
   public BlockPos(Location location) {
      this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
   }

   public World getWorld() {
      return Bukkit.getWorld(this.worldId);
   }

   public Location toLocation() {
      World world = this.getWorld();
      return world == null ? null : new Location(world, this.x, this.y, this.z);
   }

   public int getChunkX() {
      return this.x >> 4;
   }

   public int getChunkZ() {
      return this.z >> 4;
   }

   public long getChunkKey() {
      return ChunkUtil.getChunkKey(this.getChunkX(), this.getChunkZ());
   }

   public BlockPos above() {
      return new BlockPos(this.x, this.y + 1, this.z, this.worldId);
   }

   public BlockPos below() {
      return new BlockPos(this.x, this.y - 1, this.z, this.worldId);
   }
}
