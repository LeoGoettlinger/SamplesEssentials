package com.fastasyncworldedit.core.extent.clipboard;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EmptyClipboard implements Clipboard {
   private static final EmptyClipboard instance = new EmptyClipboard();

   public static EmptyClipboard getInstance() {
      return instance;
   }

   @Nonnull
   @Override
   public Region getRegion() {
      return new CuboidRegion(BlockVector3.ZERO, BlockVector3.ZERO);
   }

   @Nonnull
   @Override
   public BlockVector3 getDimensions() {
      return BlockVector3.ZERO;
   }

   @Nonnull
   @Override
   public BlockVector3 getOrigin() {
      return BlockVector3.ZERO;
   }

   @Override
   public void setOrigin(@Nonnull BlockVector3 origin) {
   }

   @Override
   public void removeEntity(@Nonnull Entity entity) {
   }

   @Nonnull
   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.ZERO;
   }

   @Nonnull
   @Override
   public BlockVector3 getMaximumPoint() {
      return BlockVector3.ZERO;
   }

   @Nonnull
   @Override
   public BaseBlock getFullBlock(@Nonnull BlockVector3 position) {
      return BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   @Nonnull
   @Override
   public BlockState getBlock(@Nonnull BlockVector3 position) {
      return BlockTypes.AIR.getDefaultState();
   }

   @Nullable
   @Override
   public BiomeType getBiome(@Nonnull BlockVector3 position) {
      return null;
   }

   @Nonnull
   @Override
   public int[] getHeightMap(@Nullable HeightMapType type) {
      return new int[256];
   }

   @Override
   public boolean setBlock(@Nonnull BlockVector3 position, BlockStateHolder block) throws WorldEditException {
      return false;
   }

   @Override
   public boolean setBlock(int x, int y, int z, BlockStateHolder block) throws WorldEditException {
      return false;
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      return false;
   }

   @Override
   public boolean setBiome(@Nonnull BlockVector3 position, @Nonnull BiomeType biome) {
      return false;
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return false;
   }

   @Override
   public boolean setBiome(int x, int y, int z, @Nonnull BiomeType biome) {
      return false;
   }

   private EmptyClipboard() {
   }
}
