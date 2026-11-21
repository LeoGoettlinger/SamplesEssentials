package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IChunkSet extends IBlocks, OutputExtent {
   @Override
   boolean setBiome(int x, int y, int z, BiomeType biome);

   @Override
   default boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   @Override
   <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T holder);

   void setBlocks(int layer, char[] data);

   boolean isEmpty();

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   @Override
   default boolean setTile(int x, int y, int z, CompoundTag tile) throws WorldEditException {
      return this.tile(x, y, z, FaweCompoundTag.of(tile.toLinTag()));
   }

   @Override
   boolean tile(int x, int y, int z, FaweCompoundTag tag);

   @Override
   void setBlockLight(int x, int y, int z, int value);

   @Override
   void setSkyLight(int x, int y, int z, int value);

   @Override
   void setHeightMap(HeightMapType type, int[] heightMap);

   void setLightLayer(int layer, char[] toSet);

   void setSkyLightLayer(int layer, char[] toSet);

   void setFullBright(int layer);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default void setEntity(CompoundTag tag) {
      this.entity(FaweCompoundTag.of(tag::toLinTag));
   }

   void entity(FaweCompoundTag tag);

   void removeEntity(UUID uuid);

   Set<UUID> getEntityRemoves();

   BiomeType[][] getBiomes();

   default boolean hasBiomes() {
      return this.getBiomes() != null;
   }

   char[][] getLight();

   char[][] getSkyLight();

   default boolean hasLight() {
      return this.getLight() != null;
   }

   default void setFastMode(boolean fastMode) {
   }

   default boolean isFastMode() {
      return false;
   }

   default void setBitMask(int bitMask) {
   }

   @Override
   default int getBitMask() {
      return -1;
   }

   default Map<HeightMapType, int[]> getHeightMaps() {
      return new EnumMap<>(HeightMapType.class);
   }

   IChunkSet reset();

   @Nullable
   @Override
   default Operation commit() {
      return null;
   }

   boolean hasBiomes(int layer);

   @Nonnull
   default IChunkSet createCopy() {
      return this;
   }

   void setSideEffectSet(@Nonnull SideEffectSet sideEffectSet);

   @Nonnull
   SideEffectSet getSideEffectSet();
}
