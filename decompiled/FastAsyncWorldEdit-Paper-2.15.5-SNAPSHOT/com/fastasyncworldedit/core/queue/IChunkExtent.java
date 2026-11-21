package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

public interface IChunkExtent<T extends IChunk> extends Extent {
   T getOrCreateChunk(int chunkX, int chunkZ);

   @Override
   default <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B state) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.setBlock(x & 15, y, z & 15, state);
   }

   @Override
   default boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.tile(x & 15, y, z & 15, tile);
   }

   @Override
   default boolean setBiome(int x, int y, int z, BiomeType biome) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.setBiome(x & 15, y, z & 15, biome);
   }

   @Override
   default boolean setBiome(BlockVector3 position, BiomeType biome) {
      IChunk chunk = this.getOrCreateChunk(position.x() >> 4, position.z() >> 4);
      return chunk.setBiome(position.x() & 15, position.y(), position.z() & 15, biome);
   }

   @Override
   default BlockState getBlock(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getBlock(x & 15, y, z & 15);
   }

   @Override
   default BaseBlock getFullBlock(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getFullBlock(x & 15, y, z & 15);
   }

   @Override
   default BiomeType getBiomeType(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getBiomeType(x & 15, y, z & 15);
   }

   @Override
   default BiomeType getBiome(BlockVector3 position) {
      IChunk chunk = this.getOrCreateChunk(position.x() >> 4, position.z() >> 4);
      return chunk.getBiomeType(position.x() & 15, position.y(), position.z() & 15);
   }

   @Override
   default void setSkyLight(int x, int y, int z, int value) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      chunk.setSkyLight(x & 15, y, z & 15, value);
   }

   @Override
   default void setBlockLight(int x, int y, int z, int value) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      chunk.setBlockLight(x & 15, y, z & 15, value);
   }

   @Override
   default int getSkyLight(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getSkyLight(x & 15, y, z & 15);
   }

   @Override
   default int getEmittedLight(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getEmittedLight(x & 15, y, z & 15);
   }

   @Override
   default int getBrightness(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getBrightness(x & 15, y, z & 15);
   }

   @Override
   default int getOpacity(int x, int y, int z) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      return chunk.getOpacity(x & 15, y, z & 15);
   }

   @Override
   default Entity createEntity(Location location, BaseEntity entity) {
      return this.createEntity(location, entity, UUID.randomUUID());
   }

   @Override
   default Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      IChunk chunk = this.getOrCreateChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
      Map<String, LinTag<?>> map = new HashMap<>(entity.getNbt().value());
      map.put("Id", LinStringTag.of(entity.getType().getName()));
      List<LinDoubleTag> posList = new ArrayList<>();
      posList.add(LinDoubleTag.of(location.x()));
      posList.add(LinDoubleTag.of(location.y()));
      posList.add(LinDoubleTag.of(location.z()));
      map.put("Pos", LinListTag.of(LinTagType.doubleTag(), posList));
      NbtUtils.addUUIDToMap(map, uuid);
      chunk.entity(FaweCompoundTag.of(LinCompoundTag.of(map)));
      return new IChunkExtent.IChunkEntity(this, location, uuid, entity);
   }

   @Override
   default void removeEntity(int x, int y, int z, UUID uuid) {
      IChunk chunk = this.getOrCreateChunk(x >> 4, z >> 4);
      chunk.removeEntity(uuid);
   }

   public record IChunkEntity(Extent extent, Location location, UUID uuid, BaseEntity base) implements Entity {
      @Override
      public BaseEntity getState() {
         return this.base;
      }

      @Override
      public boolean remove() {
         this.extent.removeEntity(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ(), this.uuid);
         return true;
      }

      @Override
      public <T> T getFacet(Class<? extends T> cls) {
         return null;
      }

      @Override
      public Location getLocation() {
         return this.location;
      }

      @Override
      public boolean setLocation(Location location) {
         return false;
      }

      @Override
      public Extent getExtent() {
         return this.extent;
      }
   }
}
