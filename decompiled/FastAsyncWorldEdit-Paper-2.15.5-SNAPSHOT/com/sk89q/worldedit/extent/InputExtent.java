package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

public interface InputExtent {
   default BlockState getBlock(BlockVector3 position) {
      return this.getBlock(position.x(), position.y(), position.z());
   }

   default BlockState getBlock(int x, int y, int z) {
      return this.getBlock(MutableBlockVector3.get(x, y, z));
   }

   default BaseBlock getFullBlock(BlockVector3 position) {
      return this.getFullBlock(position.x(), position.y(), position.z());
   }

   default BaseBlock getFullBlock(int x, int y, int z) {
      return this.getFullBlock(MutableBlockVector3.get(x, y, z));
   }

   @Deprecated
   default BiomeType getBiome(BlockVector2 position) {
      return this.getBiomeType(position.x(), 0, position.z());
   }

   default BiomeType getBiomeType(int x, int y, int z) {
      return this.getBiome(MutableBlockVector3.get(x, y, z));
   }

   @NonAbstractForCompatibility(
      delegateName = "getBiome",
      delegateParams = {BlockVector2.class}
   )
   default BiomeType getBiome(BlockVector3 position) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getBiome(position.toBlockVector2());
   }

   default int getEmittedLight(BlockVector3 position) {
      return this.getEmittedLight(position.x(), position.y(), position.z());
   }

   default int getEmittedLight(int x, int y, int z) {
      return 0;
   }

   default int getSkyLight(MutableBlockVector3 position) {
      return this.getSkyLight(position.x(), position.y(), position.z());
   }

   default int getSkyLight(int x, int y, int z) {
      return 0;
   }

   default int getBrightness(MutableBlockVector3 position) {
      return this.getBrightness(position.x(), position.y(), position.z());
   }

   default int getBrightness(int x, int y, int z) {
      return this.getFullBlock(x, y, z).getMaterial().getLightValue();
   }

   default int getOpacity(MutableBlockVector3 position) {
      return this.getOpacity(position.x(), position.y(), position.z());
   }

   default int getOpacity(int x, int y, int z) {
      return this.getFullBlock(x, y, z).getMaterial().getLightOpacity();
   }

   default int[] getHeightMap(HeightMapType type) {
      return new int[256];
   }
}
