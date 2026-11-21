package com.sk89q.worldedit.world;

import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.weather.WeatherType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

public interface World extends Extent, Keyed, IChunkCache<IChunkGet> {
   String getName();

   String getNameUnsafe();

   @Nullable
   Path getStoragePath();

   @Override
   int getMinY();

   @Override
   int getMaxY();

   Mask createLiquidMask();

   boolean useItem(BlockVector3 position, BaseItem item, Direction face);

   @Deprecated
   default <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
      return this.setBlock(position, block, notifyAndLight ? SideEffectSet.defaults() : SideEffectSet.none());
   }

   <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException;

   @Deprecated
   default boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
      return !this.applySideEffects(position, previousType, SideEffectSet.defaults()).isEmpty();
   }

   Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException;

   default int getBlockLightLevel(BlockVector3 position) {
      return this.getBlock(position).getMaterial().getLightValue();
   }

   boolean clearContainerBlockContents(BlockVector3 position);

   void dropItem(Vector3 position, BaseItemStack item, int count);

   void dropItem(Vector3 position, BaseItemStack item);

   void simulateBlockMine(BlockVector3 position);

   default Collection<BaseItemStack> getBlockDrops(BlockVector3 position) {
      return Collections.emptyList();
   }

   default boolean canPlaceAt(BlockVector3 position, BlockState blockState) {
      return true;
   }

   default boolean regenerate(Region region, EditSession editSession) {
      return this.regenerate(region, editSession, RegenOptions.builder().build());
   }

   default boolean regenerate(Region region, Extent extent) {
      return this.regenerate(region, extent, RegenOptions.builder().build());
   }

   @NonAbstractForCompatibility(
      delegateName = "regenerate",
      delegateParams = {Region.class, EditSession.class}
   )
   default boolean regenerate(Region region, Extent extent, RegenOptions options) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      if (extent instanceof EditSession) {
         return this.regenerate(region, (EditSession)extent);
      } else {
         throw new UnsupportedOperationException(
            "This World class (" + this.getClass().getName() + ") does not implement the general Extent variant of this method"
         );
      }
   }

   @Deprecated
   default boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
      return false;
   }

   boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException;

   default boolean generateStructure(StructureType type, EditSession editSession, BlockVector3 position) {
      return false;
   }

   default boolean generateFeature(ConfiguredFeatureType type, EditSession editSession, BlockVector3 position) {
      return false;
   }

   void checkLoadedChunk(BlockVector3 position);

   void fixAfterFastMode(Iterable<BlockVector2> chunks);

   void fixLighting(Iterable<BlockVector2> chunks);

   boolean playEffect(Vector3 position, int type, int data);

   boolean playBlockBreakEffect(Vector3 position, BlockType type);

   boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority);

   WeatherType getWeather();

   long getRemainingWeatherDuration();

   void setWeather(WeatherType weatherType);

   void setWeather(WeatherType weatherType, long duration);

   BlockVector3 getSpawnPosition();

   @Override
   boolean equals(Object other);

   @Override
   int hashCode();

   @Override
   default boolean isWorld() {
      return true;
   }

   @Override
   default String id() {
      return this.getName().replace(" ", "_").toLowerCase(Locale.ROOT);
   }

   void refreshChunk(final int chunkX, final int chunkZ);

   IChunkGet get(int x, int z);

   void sendFakeChunk(@Nullable Player player, ChunkPacket packet);

   @Nullable
   @Override
   default BiomeType getBiome(BlockVector3 position) {
      return null;
   }

   @Override
   default boolean setBiome(int x, int y, int z, BiomeType biome) {
      return false;
   }

   @Override
   default boolean setBiome(BlockVector3 position, BiomeType biome) {
      return false;
   }

   void flush();
}
