package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class NullExtent implements Extent {
   public static final NullExtent INSTANCE = new NullExtent();

   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.ZERO;
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return BlockVector3.ZERO;
   }

   @Override
   public List<Entity> getEntities(Region region) {
      return Collections.emptyList();
   }

   @Override
   public List<Entity> getEntities() {
      return Collections.emptyList();
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return null;
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return null;
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return BlockTypes.AIR.getDefaultState();
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.getBlock(position).toBaseBlock();
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return BiomeTypes.THE_VOID;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws WorldEditException {
      return false;
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return false;
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return false;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return false;
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return false;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return false;
   }

   @Override
   public Extent addProcessor(final IBatchProcessor processor) {
      return this;
   }

   @Override
   public Extent addPostProcessor(final IBatchProcessor processor) {
      return this;
   }

   @Nullable
   @Override
   public Operation commit() {
      return null;
   }
}
