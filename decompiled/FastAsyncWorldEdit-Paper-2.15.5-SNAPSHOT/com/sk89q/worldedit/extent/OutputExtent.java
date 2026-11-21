package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import javax.annotation.Nullable;

public interface OutputExtent {
   @Deprecated
   default <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      return this.setBlock(position.x(), position.y(), position.z(), block);
   }

   default <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      return this.setBlock(MutableBlockVector3.get(x, y, z), block);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default boolean setTile(int x, int y, int z, CompoundTag tile) throws WorldEditException {
      return this.tile(x, y, z, FaweCompoundTag.of(tile.toLinTag()));
   }

   boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException;

   default boolean fullySupports3DBiomes() {
      return true;
   }

   @Deprecated
   default boolean setBiome(BlockVector2 position, BiomeType biome) {
      boolean result = false;
      int minY = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY();
      int maxY = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY();

      for (int y = minY; y < maxY; y++) {
         result |= this.setBiome(position.toBlockVector3().mutY(y), biome);
      }

      return result;
   }

   @NonAbstractForCompatibility(
      delegateName = "setBiome",
      delegateParams = {int.class, int.class, int.class, BiomeType.class}
   )
   default boolean setBiome(int x, int y, int z, BiomeType biome) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.setBiome(MutableBlockVector3.get(x, y, z), biome);
   }

   @NonAbstractForCompatibility(
      delegateName = "setBiome",
      delegateParams = {BlockVector3.class, BiomeType.class}
   )
   default boolean setBiome(BlockVector3 position, BiomeType biome) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.setBiome(position.toBlockVector2(), biome);
   }

   default void setBlockLight(BlockVector3 position, int value) {
      this.setBlockLight(position.x(), position.y(), position.z(), value);
   }

   default void setBlockLight(int x, int y, int z, int value) {
   }

   default void setSkyLight(BlockVector3 position, int value) {
      this.setSkyLight(position.x(), position.y(), position.z(), value);
   }

   default void setSkyLight(int x, int y, int z, int value) {
   }

   default void setHeightMap(HeightMapType type, int[] heightMap) {
   }

   @Nullable
   Operation commit();
}
