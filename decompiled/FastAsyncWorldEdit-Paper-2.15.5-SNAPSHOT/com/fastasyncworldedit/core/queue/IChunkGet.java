package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.annotation.Nullable;

public interface IChunkGet extends IBlocks, Trimable, InputExtent, ITileInput {
   @Override
   BaseBlock getFullBlock(int x, int y, int z);

   @Override
   BiomeType getBiomeType(int x, int y, int z);

   @Override
   default BiomeType getBiome(BlockVector3 position) {
      return this.getBiomeType(position.x(), position.y(), position.z());
   }

   @Override
   BlockState getBlock(int x, int y, int z);

   @Override
   int getSkyLight(int x, int y, int z);

   @Override
   int getEmittedLight(int x, int y, int z);

   @Override
   int[] getHeightMap(HeightMapType type);

   default void optimize() {
   }

   <T extends Future<T>> T call(IQueueExtent<? extends IChunk> owner, IChunkSet set, Runnable finalize);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default CompoundTag getEntity(UUID uuid) {
      FaweCompoundTag entity = this.entity(uuid);
      return entity == null ? null : new CompoundTag(entity.linTag());
   }

   @Nullable
   FaweCompoundTag entity(UUID uuid);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   @Override
   default CompoundTag getTile(int x, int y, int z) {
      return IBlocks.super.getTile(x, y, z);
   }

   Set<Entity> getFullEntities();

   boolean isCreateCopy();

   int setCreateCopy(boolean createCopy);

   @Nullable
   default IChunkGet getCopy(int key) {
      return null;
   }

   default void lockCall() {
   }

   default void unlockCall() {
   }

   void setLightingToGet(char[][] lighting, int startSectionIndex, int endSectionIndex);

   void setSkyLightingToGet(char[][] lighting, int startSectionIndex, int endSectionIndex);

   void setHeightmapToGet(HeightMapType type, int[] data);

   int getMaxY();

   int getMinY();
}
