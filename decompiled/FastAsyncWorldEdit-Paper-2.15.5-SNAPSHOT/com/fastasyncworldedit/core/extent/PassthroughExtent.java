package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.function.generator.GenBase;
import com.fastasyncworldedit.core.function.generator.Resource;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class PassthroughExtent extends AbstractDelegateExtent {
   public PassthroughExtent(Extent extent) {
      super(extent);
   }

   @Override
   public boolean regenerateChunk(int x, int z, @Nullable BiomeType type, @Nullable Long seed) {
      return this.getExtent().regenerateChunk(x, z, type, seed);
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
      return this.getExtent().getHighestTerrainBlock(x, z, minY, maxY);
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY, Mask filter) {
      return this.getExtent().getHighestTerrainBlock(x, z, minY, maxY, filter);
   }

   @Override
   public int getNearestSurfaceLayer(int x, int z, int y, int minY, int maxY) {
      return this.getExtent().getNearestSurfaceLayer(x, z, y, minY, maxY);
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, boolean ignoreAir) {
      return this.getExtent().getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, ignoreAir);
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY) {
      return this.getExtent().getNearestSurfaceTerrainBlock(x, z, y, minY, maxY);
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax) {
      return this.getExtent().getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax);
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, Mask mask) {
      return this.getExtent().getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax, mask);
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, boolean ignoreAir) {
      return this.getExtent().getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax, ignoreAir);
   }

   @Override
   public void addCaves(Region region) throws WorldEditException {
      this.getExtent().addCaves(region);
   }

   @Override
   public void generate(Region region, GenBase gen) throws WorldEditException {
      this.getExtent().generate(region, gen);
   }

   @Override
   public void addSchems(Region region, Mask mask, List<ClipboardHolder> clipboards, int rarity, boolean rotate) throws WorldEditException {
      this.getExtent().addSchems(region, mask, clipboards, rarity, rotate);
   }

   @Override
   public void spawnResource(Region region, Resource gen, int rarity, int frequency) throws WorldEditException {
      this.getExtent().spawnResource(region, gen, rarity, frequency);
   }

   @Override
   public boolean contains(BlockVector3 pt) {
      return this.getExtent().contains(pt);
   }

   @Override
   public void addOre(Region region, Mask mask, Pattern material, int size, int frequency, int rarity, int minY, int maxY) throws WorldEditException {
      this.getExtent().addOre(region, mask, material, size, frequency, rarity, minY, maxY);
   }

   @Override
   public List<Countable<BlockType>> getBlockDistribution(Region region) {
      return this.getExtent().getBlockDistribution(region);
   }

   @Override
   public List<Countable<BlockState>> getBlockDistributionWithData(Region region) {
      return this.getExtent().getBlockDistributionWithData(region);
   }

   @Override
   public Clipboard lazyCopy(Region region) {
      return this.getExtent().lazyCopy(region);
   }

   @Override
   public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
      return this.getExtent().countBlocks(region, searchBlocks);
   }

   @Override
   public int countBlocks(Region region, Mask searchMask) {
      return this.getExtent().countBlocks(region, searchMask);
   }

   @Override
   public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
      return this.getExtent().setBlocks(region, block);
   }

   @Override
   public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      return this.getExtent().setBlocks(region, pattern);
   }

   @Override
   public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
      return this.getExtent().replaceBlocks(region, filter, replacement);
   }

   @Override
   public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
      return this.getExtent().replaceBlocks(region, filter, pattern);
   }

   @Override
   public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
      return this.getExtent().replaceBlocks(region, mask, pattern);
   }

   @Override
   public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
      return this.getExtent().center(region, pattern);
   }

   @Override
   public int setBlocks(Set<BlockVector3> vset, Pattern pattern) {
      return this.getExtent().setBlocks(vset, pattern);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.getExtent().getBlock(position);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getExtent().getBlock(x, y, z);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.getExtent().getFullBlock(position);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.getExtent().getFullBlock(x, y, z);
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.getExtent().getEntities(region);
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.getExtent().getEntities();
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return this.getExtent().createEntity(location, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return this.getExtent().createEntity(location, entity, uuid);
   }

   @Deprecated
   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      return this.getExtent().setBlock(position, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public boolean setTile(int x, int y, int z, CompoundTag tile) throws WorldEditException {
      return this.getExtent().setTile(x, y, z, tile);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.getExtent().setBiome(position, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public boolean cancel() {
      return this.getExtent().cancel();
   }

   @Override
   public void removeEntity(int x, int y, int z, UUID uuid) {
      this.getExtent().removeEntity(x, y, z, uuid);
   }

   @Override
   public boolean isQueueEnabled() {
      return this.getExtent().isQueueEnabled();
   }

   @Override
   public void enableQueue() {
      this.getExtent().enableQueue();
   }

   @Override
   public void disableQueue() {
      this.getExtent().disableQueue();
   }

   @Override
   public boolean isWorld() {
      return this.getExtent().isWorld();
   }

   @Override
   public <T extends Filter> T apply(Region region, T filter, boolean full) {
      return this.getExtent().apply(region, filter, full);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getExtent().getBiome(position);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.getExtent().getBiomeType(x, y, z);
   }

   @Override
   public <T extends Filter> T apply(Iterable<BlockVector3> positions, T filter) {
      return this.getExtent().apply(positions, filter);
   }
}
