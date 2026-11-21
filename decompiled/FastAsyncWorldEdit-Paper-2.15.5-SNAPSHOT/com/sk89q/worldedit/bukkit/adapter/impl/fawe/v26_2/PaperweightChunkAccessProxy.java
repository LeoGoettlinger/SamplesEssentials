package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2;

import ca.spottedleaf.moonrise.patches.starlight.light.SWMRNibbleArray;
import com.fastasyncworldedit.core.util.ReflectionUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.ChunkAccess.PackedTicks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickContainerAccess;
import org.bukkit.block.Block;
import sun.misc.Unsafe;

public final class PaperweightChunkAccessProxy extends ChunkAccess {
   ChunkAccess parent;

   private PaperweightChunkAccessProxy() {
      super(null, null, null, null, -1L, null, null);
      throw new IllegalStateException("Cannot be instantiated");
   }

   public static PaperweightChunkAccessProxy getInstance() {
      Unsafe unsafe = ReflectionUtils.getUnsafe();

      try {
         return (PaperweightChunkAccessProxy)unsafe.allocateInstance(PaperweightChunkAccessProxy.class);
      } catch (InstantiationException var2) {
         throw new RuntimeException(var2);
      }
   }

   public int hashCode() {
      return this.parent.hashCode();
   }

   public boolean equals(final Object obj) {
      return this.parent.equals(obj instanceof PaperweightChunkAccessProxy ? ((PaperweightChunkAccessProxy)obj).parent : obj);
   }

   public String toString() {
      return this.parent.toString();
   }

   @Nonnull
   public BlockState getBlockState(@Nonnull final BlockPos pos) {
      return this.parent.getBlockState(pos);
   }

   @Nullable
   public BlockState getBlockStateIfLoaded(@Nonnull final BlockPos blockPos) {
      return this.parent.getBlockStateIfLoaded(blockPos);
   }

   @Nullable
   public net.minecraft.world.level.block.Block getBlockIfLoaded(@Nonnull final BlockPos blockposition) {
      return this.parent.getBlockIfLoaded(blockposition);
   }

   @Nullable
   public FluidState getFluidIfLoaded(@Nonnull final BlockPos blockPos) {
      return this.parent.getFluidIfLoaded(blockPos);
   }

   @Nonnull
   public FluidState getFluidState(@Nonnull final BlockPos pos) {
      return this.parent.getFluidState(pos);
   }

   public int getLightEmission(@Nonnull final BlockPos pos) {
      return this.parent.getLightEmission(pos);
   }

   @Nonnull
   public Stream<BlockState> getBlockStates(@Nonnull final AABB box) {
      return this.parent.getBlockStates(box);
   }

   @Nonnull
   public BlockHitResult isBlockInLine(@Nonnull final ClipBlockStateContext context) {
      return this.parent.isBlockInLine(context);
   }

   @Nonnull
   public BlockHitResult clip(@Nonnull final ClipContext raytrace1, @Nonnull final BlockPos blockposition) {
      return this.parent.clip(raytrace1, blockposition);
   }

   @Nonnull
   public BlockHitResult clip(@Nonnull final ClipContext raytrace1, @Nonnull final BlockPos blockposition, @Nullable final Predicate<? super Block> canCollide) {
      return this.parent.clip(raytrace1, blockposition, canCollide);
   }

   @Nonnull
   public BlockHitResult clip(@Nonnull final ClipContext context) {
      return this.parent.clip(context);
   }

   @Nonnull
   public BlockHitResult clip(@Nonnull final ClipContext context, @Nullable final Predicate<? super Block> canCollide) {
      return this.parent.clip(context, canCollide);
   }

   @Nullable
   public BlockHitResult clipWithInteractionOverride(
      @Nonnull final Vec3 start, @Nonnull final Vec3 end, @Nonnull final BlockPos pos, @Nonnull final VoxelShape shape, @Nonnull final BlockState state
   ) {
      return this.parent.clipWithInteractionOverride(start, end, pos, shape, state);
   }

   public double getBlockFloorHeight(@Nonnull final VoxelShape blockCollisionShape, @Nonnull final Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
      return this.parent.getBlockFloorHeight(blockCollisionShape, belowBlockCollisionShapeGetter);
   }

   public double getBlockFloorHeight(@Nonnull final BlockPos pos) {
      return this.parent.getBlockFloorHeight(pos);
   }

   public BlockEntity getBlockEntity(@Nonnull final BlockPos pos) {
      return this.parent.getBlockEntity(pos);
   }

   @Nonnull
   public <T extends BlockEntity> Optional<T> getBlockEntity(@Nonnull final BlockPos pos, @Nonnull final BlockEntityType<T> type) {
      return this.parent.getBlockEntity(pos, type);
   }

   @Nonnull
   public SWMRNibbleArray[] starlight$getBlockNibbles() {
      return this.parent.starlight$getBlockNibbles();
   }

   public void starlight$setBlockNibbles(@Nonnull final SWMRNibbleArray[] nibbles) {
      this.parent.starlight$setBlockNibbles(nibbles);
   }

   @Nonnull
   public SWMRNibbleArray[] starlight$getSkyNibbles() {
      return this.parent.starlight$getSkyNibbles();
   }

   public void starlight$setSkyNibbles(@Nonnull final SWMRNibbleArray[] nibbles) {
      this.parent.starlight$setSkyNibbles(nibbles);
   }

   @Nonnull
   public boolean[] starlight$getSkyEmptinessMap() {
      return this.parent.starlight$getSkyEmptinessMap();
   }

   public void starlight$setSkyEmptinessMap(@Nonnull final boolean[] emptinessMap) {
      this.parent.starlight$setSkyEmptinessMap(emptinessMap);
   }

   @Nonnull
   public boolean[] starlight$getBlockEmptinessMap() {
      return this.parent.starlight$getBlockEmptinessMap();
   }

   public void starlight$setBlockEmptinessMap(@Nonnull final boolean[] emptinessMap) {
      this.parent.starlight$setBlockEmptinessMap(emptinessMap);
   }

   @Nonnull
   public GameEventListenerRegistry getListenerRegistry(final int sectionY) {
      return this.parent.getListenerRegistry(sectionY);
   }

   @Nonnull
   public BlockState getBlockState(final int i, final int i1, final int i2) {
      return this.parent.getBlockState(i, i1, i2);
   }

   @Nullable
   public BlockState setBlockState(@Nonnull final BlockPos pos, @Nonnull final BlockState state) {
      return this.parent.setBlockState(pos, state);
   }

   @Nullable
   public BlockState setBlockState(@Nonnull final BlockPos blockPos, @Nonnull final BlockState blockState, final int i) {
      return this.parent.setBlockState(blockPos, blockState, i);
   }

   public void setBlockEntity(@Nonnull final BlockEntity blockEntity) {
      this.parent.setBlockEntity(blockEntity);
   }

   public void addEntity(@Nonnull final Entity entity) {
      this.parent.addEntity(entity);
   }

   public int getHighestFilledSectionIndex() {
      return this.parent.getHighestFilledSectionIndex();
   }

   public int getHighestSectionPosition() {
      return this.parent.getHighestSectionPosition();
   }

   @Nonnull
   public Set<BlockPos> getBlockEntitiesPos() {
      return this.parent.getBlockEntitiesPos();
   }

   @Nonnull
   public LevelChunkSection[] getSections() {
      return this.parent.getSections();
   }

   @Nonnull
   public LevelChunkSection getSection(final int index) {
      return this.parent.getSection(index);
   }

   @Nonnull
   public Collection<Entry<Types, Heightmap>> getHeightmaps() {
      return this.parent.getHeightmaps();
   }

   public void setHeightmap(@Nonnull final Types type, @Nonnull final long[] data) {
      this.parent.setHeightmap(type, data);
   }

   @Nonnull
   public Heightmap getOrCreateHeightmapUnprimed(@Nonnull final Types type) {
      return this.parent.getOrCreateHeightmapUnprimed(type);
   }

   public boolean hasPrimedHeightmap(@Nonnull final Types type) {
      return this.parent.hasPrimedHeightmap(type);
   }

   public int getHeight(@Nonnull final Types type, final int x, final int z) {
      return this.parent.getHeight(type, x, z);
   }

   @Nonnull
   public ChunkPos getPos() {
      return this.parent.getPos();
   }

   @Nullable
   public StructureStart getStartForStructure(@Nonnull final Structure structure) {
      return this.parent.getStartForStructure(structure);
   }

   public void setStartForStructure(@Nonnull final Structure structure, @Nonnull final StructureStart structureStart) {
      this.parent.setStartForStructure(structure, structureStart);
   }

   @Nonnull
   public Map<Structure, StructureStart> getAllStarts() {
      return this.parent.getAllStarts();
   }

   public void setAllStarts(@Nonnull final Map<Structure, StructureStart> structureStarts) {
      this.parent.setAllStarts(structureStarts);
   }

   @Nonnull
   public LongSet getReferencesForStructure(@Nonnull final Structure structure) {
      return this.parent.getReferencesForStructure(structure);
   }

   public void addReferenceForStructure(@Nonnull final Structure structure, final long reference) {
      this.parent.addReferenceForStructure(structure, reference);
   }

   @Nonnull
   public Map<Structure, LongSet> getAllReferences() {
      return this.parent.getAllReferences();
   }

   public void setAllReferences(@Nonnull final Map<Structure, LongSet> structureReferencesMap) {
      this.parent.setAllReferences(structureReferencesMap);
   }

   public boolean isYSpaceEmpty(final int startY, final int endY) {
      return this.parent.isYSpaceEmpty(startY, endY);
   }

   public void markUnsaved() {
      this.parent.markUnsaved();
   }

   public boolean tryMarkSaved() {
      return this.parent.tryMarkSaved();
   }

   public boolean isUnsaved() {
      return this.parent.isUnsaved();
   }

   @Nonnull
   public ChunkStatus getPersistedStatus() {
      return this.parent.getPersistedStatus();
   }

   @Nonnull
   public ChunkStatus getHighestGeneratedStatus() {
      return this.parent.getHighestGeneratedStatus();
   }

   public void removeBlockEntity(@Nonnull final BlockPos blockPos) {
      this.parent.removeBlockEntity(blockPos);
   }

   public void markPosForPostProcessing(@Nonnull final BlockPos pos) {
   }

   @Nonnull
   public ShortList[] getPostProcessing() {
      return this.parent.getPostProcessing();
   }

   public void addPackedPostProcess(@Nonnull final ShortList offsets, final int index) {
      this.parent.addPackedPostProcess(offsets, index);
   }

   public void setBlockEntityNbt(@Nonnull final CompoundTag tag) {
      this.parent.setBlockEntityNbt(tag);
   }

   @Nullable
   public CompoundTag getBlockEntityNbt(@Nonnull final BlockPos pos) {
      return this.parent.getBlockEntityNbt(pos);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(@Nonnull final BlockPos blockPos, @Nonnull final Provider provider) {
      return this.parent.getBlockEntityNbtForSaving(blockPos, provider);
   }

   public void findBlocks(@Nonnull final Predicate<BlockState> predicate, @Nonnull final BiConsumer<BlockPos, BlockState> output) {
      this.parent.findBlocks(predicate, output);
   }

   @Nonnull
   public TickContainerAccess<net.minecraft.world.level.block.Block> getBlockTicks() {
      return this.parent.getBlockTicks();
   }

   @Nonnull
   public TickContainerAccess<Fluid> getFluidTicks() {
      return this.parent.getFluidTicks();
   }

   public boolean canBeSerialized() {
      return this.parent.canBeSerialized();
   }

   @Nonnull
   public PackedTicks getTicksForSerialization(final long l) {
      return this.parent.getTicksForSerialization(l);
   }

   @Nonnull
   public UpgradeData getUpgradeData() {
      return this.parent.getUpgradeData();
   }

   public boolean isOldNoiseGeneration() {
      return this.parent.isOldNoiseGeneration();
   }

   @Nullable
   public BlendingData getBlendingData() {
      return this.parent.getBlendingData();
   }

   public long getInhabitedTime() {
      return this.parent.getInhabitedTime();
   }

   public void incrementInhabitedTime(final long amount) {
      this.parent.incrementInhabitedTime(amount);
   }

   public void setInhabitedTime(final long inhabitedTime) {
      this.parent.setInhabitedTime(inhabitedTime);
   }

   public boolean isLightCorrect() {
      return this.parent.isLightCorrect();
   }

   public void setLightCorrect(final boolean lightCorrect) {
      this.parent.setLightCorrect(lightCorrect);
   }

   public int getMinY() {
      return this.parent.getMinY();
   }

   public int getHeight() {
      return this.parent.getHeight();
   }

   @Nonnull
   public NoiseChunk getOrCreateNoiseChunk(@Nonnull final Function<ChunkAccess, NoiseChunk> noiseChunkCreator) {
      return this.parent.getOrCreateNoiseChunk(noiseChunkCreator);
   }

   @Nonnull
   public BiomeGenerationSettings carverBiome(@Nonnull final Supplier<BiomeGenerationSettings> caverBiomeSettingsSupplier) {
      return this.parent.carverBiome(caverBiomeSettingsSupplier);
   }

   @Nonnull
   public Holder<Biome> getNoiseBiome(final int x, final int y, final int z) {
      return this.parent.getNoiseBiome(x, y, z);
   }

   public void fillBiomesFromNoise(@Nonnull final BiomeResolver resolver, @Nonnull final Sampler sampler) {
      this.parent.fillBiomesFromNoise(resolver, sampler);
   }

   public boolean hasAnyStructureReferences() {
      return this.parent.hasAnyStructureReferences();
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return this.parent.getBelowZeroRetrogen();
   }

   public boolean isUpgrading() {
      return this.parent.isUpgrading();
   }

   @Nonnull
   public LevelHeightAccessor getHeightAccessorForGeneration() {
      return this.parent.getHeightAccessorForGeneration();
   }

   public void initializeLightSources() {
      this.parent.initializeLightSources();
   }

   @Nonnull
   public ChunkSkyLightSources getSkyLightSources() {
      return this.parent.getSkyLightSources();
   }

   @Nonnull
   public PathElement problemPath() {
      return this.parent.problemPath();
   }

   public int getMaxY() {
      return this.parent.getMaxY();
   }

   public int getSectionsCount() {
      return this.parent.getSectionsCount();
   }

   public int getMinSectionY() {
      return this.parent.getMinSectionY();
   }

   public int getMaxSectionY() {
      return this.parent.getMaxSectionY();
   }

   public boolean isInsideBuildHeight(final int y) {
      return this.parent.isInsideBuildHeight(y);
   }

   public boolean isOutsideBuildHeight(@Nonnull final BlockPos pos) {
      return this.parent.isOutsideBuildHeight(pos);
   }

   public boolean isOutsideBuildHeight(final int y) {
      return this.parent.isOutsideBuildHeight(y);
   }

   public int getSectionIndex(final int y) {
      return this.parent.getSectionIndex(y);
   }

   public int getSectionIndexFromSectionY(final int coord) {
      return this.parent.getSectionIndexFromSectionY(coord);
   }

   public int getSectionYFromSectionIndex(final int index) {
      return this.parent.getSectionYFromSectionIndex(index);
   }
}
