package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_4;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;

public class FaweBlockStateListPopulator extends BlockStateListPopulator {
   private final ServerLevel world;

   public FaweBlockStateListPopulator(ServerLevel world) {
      super(world);
      this.world = world;
   }

   public long getSeed() {
      return this.world.getSeed();
   }

   @Nonnull
   public ServerLevel getLevel() {
      return this.world.getLevel();
   }

   @Nonnull
   public DifficultyInstance getCurrentDifficultyAt(final BlockPos pos) {
      return this.world.getCurrentDifficultyAt(pos);
   }

   public MinecraftServer getServer() {
      return this.world.getServer();
   }

   @Nonnull
   public ChunkSource getChunkSource() {
      return this.world.getChunkSource();
   }

   @Nonnull
   public RandomSource getRandom() {
      return this.world.getRandom();
   }

   public ChunkAccess getChunk(final int chunkX, final int chunkZ, final ChunkStatus leastStatus, final boolean create) {
      return this.world.getChunk(chunkX, chunkZ, leastStatus, create);
   }

   @Nonnull
   public BiomeManager getBiomeManager() {
      return this.world.getBiomeManager();
   }

   @Nonnull
   public Holder<Biome> getUncachedNoiseBiome(final int biomeX, final int biomeY, final int biomeZ) {
      return this.world.getUncachedNoiseBiome(biomeX, biomeY, biomeZ);
   }

   public int getSeaLevel() {
      return this.world.getSeaLevel();
   }

   @Nonnull
   public FeatureFlagSet enabledFeatures() {
      return this.world.enabledFeatures();
   }

   public float getShade(final Direction direction, final boolean shaded) {
      return this.world.getShade(direction, shaded);
   }

   @Nonnull
   public LevelLightEngine getLightEngine() {
      return this.world.getLightEngine();
   }

   @Nullable
   public ChunkAccess getChunkIfLoadedImmediately(final int x, final int z) {
      return this.world.getChunkIfLoadedImmediately(x, z);
   }

   public BlockState getBlockStateIfLoaded(final BlockPos blockposition) {
      return this.world.getBlockStateIfLoaded(blockposition);
   }

   public FluidState getFluidIfLoaded(final BlockPos blockposition) {
      return this.world.getFluidIfLoaded(blockposition);
   }

   @Nonnull
   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int maxUpdateDepth) {
      return this.world.setBlock(pos, state, flags, maxUpdateDepth);
   }

   public boolean removeBlock(final BlockPos pos, final boolean move) {
      return this.world.removeBlock(pos, move);
   }

   public boolean destroyBlock(final BlockPos pos, final boolean drop, final Entity breakingEntity, final int maxUpdateDepth) {
      return this.world.destroyBlock(pos, drop, breakingEntity, maxUpdateDepth);
   }

   @Nonnull
   public BlockState getBlockState(final BlockPos pos) {
      return this.world.getBlockState(pos);
   }

   public boolean setBlock(final BlockPos pos, final BlockState state, final int flags) {
      return this.world.setBlock(pos, state, flags);
   }
}
