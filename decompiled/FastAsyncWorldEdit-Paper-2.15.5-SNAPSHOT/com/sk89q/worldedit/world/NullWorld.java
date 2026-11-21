package com.sk89q.worldedit.world;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.blocks.NullChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class NullWorld extends AbstractWorld {
   private static final NullWorld INSTANCE = new NullWorld();

   protected NullWorld() {
   }

   @Override
   public String getName() {
      return "null";
   }

   @Override
   public String getNameUnsafe() {
      return "null";
   }

   @Override
   public String id() {
      return "null";
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
      return false;
   }

   @Override
   public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
      return ImmutableSet.of();
   }

   @Override
   public int getBlockLightLevel(BlockVector3 position) {
      return 0;
   }

   @Override
   public boolean clearContainerBlockContents(BlockVector3 position) {
      return false;
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return false;
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return BiomeTypes.THE_VOID;
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return BiomeTypes.THE_VOID;
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
   public void dropItem(Vector3 position, BaseItemStack item) {
   }

   @Override
   public void simulateBlockMine(BlockVector3 position) {
   }

   @Override
   public boolean regenerate(Region region, EditSession editSession) {
      return false;
   }

   @Override
   public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
      return false;
   }

   @Override
   public WeatherType getWeather() {
      return WeatherTypes.CLEAR;
   }

   @Override
   public long getRemainingWeatherDuration() {
      return 0L;
   }

   @Override
   public void setWeather(WeatherType weatherType) {
   }

   @Override
   public void setWeather(WeatherType weatherType, long duration) {
   }

   @Override
   public BlockVector3 getSpawnPosition() {
      return BlockVector3.ZERO;
   }

   @Override
   public void refreshChunk(int chunkX, int chunkZ) {
   }

   @Override
   public IChunkGet get(int x, int z) {
      return NullChunkGet.getInstance();
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.getBlock(position.x(), position.y(), position.z());
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BlockTypes.AIR.getDefaultState();
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return false;
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return false;
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.getBlock(position).toBaseBlock();
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

   public static NullWorld getInstance() {
      return INSTANCE;
   }

   @Override
   public void sendFakeChunk(@Nullable Player player, ChunkPacket packet) {
   }

   @Override
   public boolean regenerate(Region region, Extent extent, RegenOptions options) {
      return false;
   }

   @Override
   public void flush() {
   }
}
