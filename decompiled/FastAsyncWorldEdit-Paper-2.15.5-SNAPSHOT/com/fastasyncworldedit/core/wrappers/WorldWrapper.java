package com.fastasyncworldedit.core.wrappers;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.weather.WeatherType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class WorldWrapper extends AbstractWorld {
   private final World parent;

   private WorldWrapper(World parent) {
      this.parent = parent;
   }

   public static WorldWrapper wrap(World world) {
      if (world == null) {
         return null;
      } else {
         return world instanceof WorldWrapper ? (WorldWrapper)world : new WorldWrapper(world);
      }
   }

   public static World unwrap(World world) {
      if (world instanceof WorldWrapper) {
         return unwrap(((WorldWrapper)world).getParent());
      } else {
         return world instanceof EditSession ? unwrap(((EditSession)world).getWorld()) : world;
      }
   }

   public static World unwrap(Extent extent) {
      if (extent.isWorld()) {
         if (extent instanceof World) {
            return unwrap((World)extent);
         }

         if (extent instanceof AbstractDelegateExtent) {
            return unwrap(new ExtentTraverser<>(extent).find(World.class).get());
         }
      }

      return null;
   }

   public World getParent() {
      return this.parent instanceof WorldWrapper ? ((WorldWrapper)this.parent).getParent() : this.parent;
   }

   @Override
   public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
      return this.parent.useItem(position, item, face);
   }

   @Override
   public Path getStoragePath() {
      return this.parent.getStoragePath();
   }

   @Override
   public int getMaxY() {
      return this.parent.getMaxY();
   }

   @Override
   public int getMinY() {
      return this.parent.getMinY();
   }

   @Override
   public Mask createLiquidMask() {
      return this.parent.createLiquidMask();
   }

   @Override
   public void dropItem(Vector3 pt, BaseItemStack item, int times) {
      this.parent.dropItem(pt, item, times);
   }

   @Override
   public void checkLoadedChunk(BlockVector3 pt) {
      this.parent.checkLoadedChunk(pt);
   }

   @Override
   public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
      this.parent.fixAfterFastMode(chunks);
   }

   @Override
   public void fixLighting(Iterable<BlockVector2> chunks) {
      this.parent.fixLighting(chunks);
   }

   @Override
   public boolean playEffect(Vector3 position, int type, int data) {
      return this.parent.playEffect(position, type, data);
   }

   @Override
   public boolean playBlockBreakEffect(Vector3 position, BlockType type) {
      return this.parent.playBlockBreakEffect(position, type);
   }

   @Override
   public boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority) {
      return this.parent.queueBlockBreakEffect(server, position, blockType, priority);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.parent.getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.parent.getMaximumPoint();
   }

   @Nullable
   @Override
   public Operation commit() {
      return this.parent.commit();
   }

   @Override
   public WeatherType getWeather() {
      return null;
   }

   @Override
   public long getRemainingWeatherDuration() {
      return this.parent.getRemainingWeatherDuration();
   }

   @Override
   public void setWeather(WeatherType weatherType) {
      this.parent.setWeather(weatherType);
   }

   @Override
   public void setWeather(WeatherType weatherType, long duration) {
      this.parent.setWeather(weatherType, duration);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.parent.setBlock(x, y, z, block);
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return this.parent.tile(x, y, z, tile);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.parent.setBiome(x, y, z, biome);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.parent.setBiome(position, biome);
   }

   @Override
   public String getName() {
      return this.parent.getName();
   }

   @Override
   public String getNameUnsafe() {
      return this.parent.getNameUnsafe();
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
      return this.parent.setBlock(position, block, notifyAndLight);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
      return this.parent.setBlock(position, block, sideEffects);
   }

   @Override
   public boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
      return this.parent.notifyAndLightBlock(position, previousType);
   }

   @Override
   public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
      return this.parent.applySideEffects(position, previousType, sideEffectSet);
   }

   @Override
   public int getBlockLightLevel(BlockVector3 position) {
      return this.parent.getBlockLightLevel(position);
   }

   @Override
   public boolean clearContainerBlockContents(BlockVector3 position) {
      return this.parent.clearContainerBlockContents(position);
   }

   @Override
   public void dropItem(Vector3 position, BaseItemStack item) {
      this.parent.dropItem(position, item);
   }

   @Override
   public void simulateBlockMine(BlockVector3 pt) {
      TaskManager.taskManager().sync(new RunnableVal<Object>() {
         @Override
         public void run(Object value) {
            WorldWrapper.this.parent.simulateBlockMine(pt);
         }
      });
   }

   @Override
   public Collection<BaseItemStack> getBlockDrops(final BlockVector3 position) {
      return TaskManager.taskManager().sync(() -> this.parent.getBlockDrops(position));
   }

   @Override
   public boolean canPlaceAt(final BlockVector3 position, final BlockState blockState) {
      return this.parent.canPlaceAt(position, blockState);
   }

   @Override
   public boolean regenerate(Region region, EditSession session) {
      return this.parent.regenerate(region, session);
   }

   @Override
   public boolean regenerate(Region region, Extent extent, RegenOptions options) {
      return this.parent.regenerate(region, extent, options);
   }

   @Override
   public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
      try {
         return this.parent.generateTree(type, editSession, position);
      } catch (MaxChangedBlocksException var5) {
         throw new RuntimeException(var5);
      }
   }

   @Override
   public boolean generateTree(final TreeType type, final EditSession editSession, final BlockVector3 position) throws MaxChangedBlocksException {
      return this.parent.generateTree(type, editSession, position);
   }

   @Override
   public boolean generateStructure(final StructureType type, final EditSession editSession, final BlockVector3 position) {
      return this.parent.generateStructure(type, editSession, position);
   }

   @Override
   public boolean generateFeature(final ConfiguredFeatureType type, final EditSession editSession, final BlockVector3 position) {
      return this.parent.generateFeature(type, editSession, position);
   }

   @Override
   public BlockVector3 getSpawnPosition() {
      return this.parent.getSpawnPosition();
   }

   @Override
   public void refreshChunk(int chunkX, int chunkZ) {
      this.parent.refreshChunk(chunkX, chunkZ);
   }

   @Override
   public IChunkGet get(int x, int z) {
      return this.parent.get(x, z);
   }

   @Override
   public void sendFakeChunk(@Nullable Player player, ChunkPacket packet) {
      this.parent.sendFakeChunk(player, packet);
   }

   @Override
   public int hashCode() {
      return this.parent.hashCode();
   }

   @Override
   public boolean equals(Object other) {
      return this.parent.equals(other);
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.parent.getEntities(region);
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.parent.getEntities();
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return this.parent.createEntity(location, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return this.parent.createEntity(location, entity, uuid);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.parent.getBlock(position);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.parent.getFullBlock(position);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.parent.getBiome(position);
   }

   @Override
   public void flush() {
      this.parent.flush();
   }
}
