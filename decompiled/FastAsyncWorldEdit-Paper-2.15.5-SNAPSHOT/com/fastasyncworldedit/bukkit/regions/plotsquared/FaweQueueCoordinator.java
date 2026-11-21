package com.fastasyncworldedit.bukkit.regions.plotsquared;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.plotsquared.core.queue.LightingMode;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FaweQueueCoordinator extends QueueCoordinator {
   public final IQueueExtent<IQueueChunk> instance;
   private final World world;
   private final BlockVector3 mutable = new MutableBlockVector3();
   private boolean setbiome = false;

   public FaweQueueCoordinator(World world) {
      super(world);
      this.world = world;
      this.instance = Fawe.instance().getQueueHandler().getQueue(world);
      Fawe.instance().getQueueHandler().unCache();
   }

   public int size() {
      return this.instance.isEmpty() ? 0 : 1;
   }

   public void setModified(long l) {
   }

   public boolean setBlock(final int x, final int y, final int z, final BlockState id) {
      return this.instance.setBlock(x, y, z, id);
   }

   public boolean setBlock(int x, int y, int z, Pattern pattern) {
      this.mutable.setComponents(x, y, z);
      return pattern.apply(this.instance, this.mutable, this.mutable);
   }

   public boolean setBlock(final int x, final int y, final int z, final BaseBlock id) {
      return this.instance.setBlock(x, y, z, id);
   }

   public BlockState getBlock(int x, int y, int z) {
      return this.instance.getBlock(x, y, z);
   }

   public boolean setBiome(int x, int z, BiomeType biomeType) {
      this.setbiome = true;
      return this.instance.setBiome(BlockVector2.at(x, z), biomeType);
   }

   public boolean setBiome(int x, int y, int z, @Nonnull BiomeType biome) {
      return false;
   }

   public boolean isSettingBiomes() {
      return false;
   }

   public void setBiomesEnabled(final boolean enabled) {
   }

   public boolean setEntity(@Nonnull Entity entity) {
      return false;
   }

   @Nonnull
   public List<BlockVector2> getReadChunks() {
      return null;
   }

   public void addReadChunks(@Nonnull Set<BlockVector2> readChunks) {
   }

   public void addReadChunk(@Nonnull BlockVector2 chunk) {
   }

   public boolean isUnloadAfter() {
      return false;
   }

   public void setUnloadAfter(boolean unloadAfter) {
   }

   @Nullable
   public CuboidRegion getRegenRegion() {
      return null;
   }

   public void setRegenRegion(@Nonnull CuboidRegion regenRegion) {
   }

   public boolean enqueue() {
      boolean val = super.enqueue();
      this.instance.enableQueue();
      return val;
   }

   public void start() {
   }

   public void cancel() {
   }

   public Runnable getCompleteTask() {
      return null;
   }

   public void setCompleteTask(@Nullable Runnable whenDone) {
   }

   @Nullable
   public Consumer<BlockVector2> getChunkConsumer() {
      return null;
   }

   public void setChunkConsumer(@Nonnull Consumer<BlockVector2> consumer) {
   }

   public void addProgressSubscriber(@Nonnull ProgressSubscriber progressSubscriber) {
   }

   @Nonnull
   public LightingMode getLightingMode() {
      return null;
   }

   public void setLightingMode(@Nullable LightingMode mode) {
   }

   @Nullable
   public SideEffectSet getSideEffectSet() {
      return null;
   }

   public void setSideEffectSet(@Nullable final SideEffectSet sideEffectSet) {
   }

   public void regenChunk(int x, int z) {
      this.instance.regenerateChunk(x, z, null, null);
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public boolean setTile(int x, int y, int z, CompoundTag tag) {
      this.instance.setTile(x, y, z, (CompoundTag)FaweCache.INSTANCE.asTag(tag));
      return true;
   }

   public boolean isSettingTiles() {
      return false;
   }
}
