package com.ranull.graves.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkData implements Serializable {
   private static final long serialVersionUID = 1L;
   private final World world;
   private final int x;
   private final int z;
   private final Map<Location, BlockData> blockDataMap;
   private final Map<UUID, EntityData> entityDataMap;

   public ChunkData(Location location) {
      this.world = location.getWorld();
      this.x = location.getBlockX() >> 4;
      this.z = location.getBlockZ() >> 4;
      this.blockDataMap = new HashMap<>();
      this.entityDataMap = new HashMap<>();
   }

   public ChunkData(String worldName, int chunkX, int chunkZ) {
      this.world = Bukkit.getWorld(worldName);
      this.x = chunkX;
      this.z = chunkZ;
      this.blockDataMap = new HashMap<>();
      this.entityDataMap = new HashMap<>();
   }

   public World getWorld() {
      return this.world;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public boolean hasData() {
      return !this.blockDataMap.isEmpty() || !this.entityDataMap.isEmpty();
   }

   public boolean isLoaded() {
      return this.world != null && this.world.isChunkLoaded(this.x, this.z);
   }

   public Location getLocation() {
      return new Location(this.world, this.x << 4, 0.0, this.z << 4);
   }

   public Map<Location, BlockData> getBlockDataMap() {
      return this.blockDataMap;
   }

   public void addBlockData(BlockData blockData) {
      this.blockDataMap.put(blockData.getLocation(), blockData);
   }

   public void removeBlockData(Location location) {
      this.blockDataMap.remove(location);
   }

   public Map<UUID, EntityData> getEntityDataMap() {
      return this.entityDataMap;
   }

   public void addEntityData(EntityData entityData) {
      if (entityData != null && entityData.getUUIDEntity() != null) {
         this.entityDataMap.put(entityData.getUUIDEntity(), entityData);
      }
   }

   public void removeEntityData(EntityData entityData) {
      if (entityData != null && entityData.getUUIDEntity() != null) {
         this.entityDataMap.remove(entityData.getUUIDEntity());
      }
   }
}
