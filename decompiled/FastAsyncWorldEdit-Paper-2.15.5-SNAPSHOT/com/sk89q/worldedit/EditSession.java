package com.sk89q.worldedit;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.FaweRegionExtent;
import com.fastasyncworldedit.core.extent.PassthroughExtent;
import com.fastasyncworldedit.core.extent.ProcessedWEExtent;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.SingleRegionExtent;
import com.fastasyncworldedit.core.extent.SourceMaskExtent;
import com.fastasyncworldedit.core.extent.clipboard.WorldCopyClipboard;
import com.fastasyncworldedit.core.extent.processor.ExtentBatchProcessorHolder;
import com.fastasyncworldedit.core.extent.processor.lighting.NullRelighter;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.function.SurfaceRegionFunction;
import com.fastasyncworldedit.core.function.generator.GenBase;
import com.fastasyncworldedit.core.function.generator.OreGen;
import com.fastasyncworldedit.core.function.generator.SchemGen;
import com.fastasyncworldedit.core.function.mask.BlockMaskBuilder;
import com.fastasyncworldedit.core.function.mask.ResettableMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockTypeMask;
import com.fastasyncworldedit.core.function.mask.WallMakeMask;
import com.fastasyncworldedit.core.function.pattern.ExistingPattern;
import com.fastasyncworldedit.core.function.visitor.DirectionalVisitor;
import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.history.changeset.BlockBagChangeSet;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.math.MutableBlockVector2;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.math.random.SimplexNoise;
import com.fastasyncworldedit.core.queue.implementation.preloader.Preloader;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.ProcessorTraverser;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.extent.TracingExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.extent.world.SurvivalModeExtent;
import com.sk89q.worldedit.extent.world.WatchdogTickingExtent;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.block.Naturalizer;
import com.sk89q.worldedit.function.block.SnowSimulator;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.function.generator.GardenPatchGenerator;
import com.sk89q.worldedit.function.mask.BlockStateMask;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.BoundedHeightMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.WaterloggedRemover;
import com.sk89q.worldedit.function.util.RegionOffset;
import com.sk89q.worldedit.function.visitor.DownwardVisitor;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.function.visitor.NonRisingVisitor;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.ExpressionTimeoutException;
import com.sk89q.worldedit.internal.expression.LocalSlot;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import com.sk89q.worldedit.math.interpolation.KochanekBartelsInterpolation;
import com.sk89q.worldedit.math.interpolation.Node;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.regions.shape.ArbitraryBiomeShape;
import com.sk89q.worldedit.regions.shape.ArbitraryShape;
import com.sk89q.worldedit.regions.shape.RegionShape;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class EditSession extends PassthroughExtent implements AutoCloseable {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected final World world;
   @Nullable
   private final Actor actor;
   private final FaweLimit originalLimit;
   private final FaweLimit limit;
   private AbstractChangeSet changeSet;
   private boolean history;
   private final MutableBlockVector3 mutableBlockVector3 = new MutableBlockVector3();
   private int changes = 0;
   private final BlockBag blockBag;
   private final Extent bypassHistory;
   private final Extent bypassAll;
   private final int minY;
   private final int maxY;
   private final List<WatchdogTickingExtent> watchdogExtents = new ArrayList<>(2);
   @Nullable
   private final List<TracingExtent> tracingExtents;
   private final Relighter relighter;
   private final boolean wnaMode;
   @Nullable
   private final Region[] allowedRegions;
   private static final BlockVector3[] recurseDirections = new BlockVector3[]{
      Direction.NORTH.toBlockVector(),
      Direction.EAST.toBlockVector(),
      Direction.SOUTH.toBlockVector(),
      Direction.WEST.toBlockVector(),
      Direction.UP.toBlockVector(),
      Direction.DOWN.toBlockVector()
   };

   EditSession(EditSessionBuilder builder) {
      super(builder.compile().getExtent());
      this.world = builder.getWorld();
      this.bypassHistory = builder.getBypassHistory();
      this.bypassAll = builder.getBypassAll();
      this.originalLimit = builder.getLimit();
      this.limit = builder.getLimit().copy();
      this.actor = builder.getActor();
      this.changeSet = builder.getChangeTask();
      this.minY = this.world != null
         ? this.world.getMinY()
         : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY();
      this.maxY = this.world != null
         ? this.world.getMaxY()
         : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY();
      this.blockBag = builder.getBlockBag();
      this.history = this.changeSet != null;
      this.relighter = builder.getRelighter();
      this.wnaMode = builder.isWNAMode();
      if (builder.isTracing()) {
         this.tracingExtents = new ArrayList<>();
         Preconditions.checkNotNull(this.actor, "A player is required while tracing");
      } else {
         this.tracingExtents = null;
      }

      this.allowedRegions = builder.getAllowedRegions() != null ? (Region[])builder.getAllowedRegions().clone() : null;
   }

   public FaweLimit getLimit() {
      return this.originalLimit;
   }

   public void resetLimit() {
      this.limit.set(this.originalLimit);
      ExtentTraverser<ProcessedWEExtent> find = new ExtentTraverser<>(this.getExtent()).find(ProcessedWEExtent.class);
      if (find != null && find.get() != null) {
         find.get().setLimit(this.limit);
      }
   }

   public FaweLimit getLimitUsed() {
      return this.originalLimit.getLimitUsed(this.limit);
   }

   public FaweLimit getLimitLeft() {
      return this.limit;
   }

   public FaweRegionExtent getRegionExtent() {
      ExtentTraverser<FaweRegionExtent> traverser = new ExtentTraverser<>(this.getExtent()).find(FaweRegionExtent.class);
      return traverser == null ? null : traverser.get();
   }

   public Extent getBypassAll() {
      return this.bypassAll;
   }

   public Extent getBypassHistory() {
      return this.bypassHistory;
   }

   public void setExtent(AbstractDelegateExtent extent) {
      new ExtentTraverser<>(this).setNext(extent);
   }

   @Nullable
   public Actor getActor() {
      return this.actor;
   }

   private Extent traceIfNeeded(Extent input) {
      Extent output = input;
      if (this.tracingExtents != null) {
         TracingExtent newExtent = new TracingExtent(input);
         output = newExtent;
         this.tracingExtents.add(newExtent);
      }

      return output;
   }

   private boolean commitRequired() {
      return false;
   }

   private List<TracingExtent> getActiveTracingExtents() {
      return this.tracingExtents == null ? List.of() : this.tracingExtents.stream().filter(TracingExtent::isActive).collect(Collectors.toList());
   }

   public void enableStandardMode() {
      this.setBatchingChunks(true);
   }

   @Deprecated
   public void setReorderMode(EditSession.ReorderMode reorderMode) {
      switch (reorderMode) {
         case MULTI_STAGE:
            this.enableQueue();
            break;
         case FAST:
         case NONE:
            this.disableQueue();
            break;
         default:
            throw new UnsupportedOperationException("Not implemented: " + reorderMode);
      }
   }

   @Deprecated
   public EditSession.ReorderMode getReorderMode() {
      return this.isQueueEnabled() ? EditSession.ReorderMode.MULTI_STAGE : EditSession.ReorderMode.FAST;
   }

   public World getWorld() {
      return this.world;
   }

   public ChangeSet getChangeSet() {
      return this.changeSet;
   }

   public void setRawChangeSet(@Nullable AbstractChangeSet set) {
      this.changeSet = set;
      this.changes++;
   }

   @Deprecated
   public long getBlockChangeLimit() {
      return this.originalLimit.MAX_CHANGES.get();
   }

   public void setBlockChangeLimit(long limit) {
      this.limit.MAX_CHANGES.set(limit);
   }

   @Deprecated
   @Override
   public boolean isQueueEnabled() {
      return true;
   }

   @Deprecated
   @Override
   public void enableQueue() {
      super.enableQueue();
   }

   @Deprecated
   @Override
   public void disableQueue() {
      super.disableQueue();
   }

   public Mask getMask() {
      MaskingExtent maskingExtent = new ExtentTraverser<>(this.getExtent()).findAndGet(MaskingExtent.class);
      if (maskingExtent == null) {
         ExtentBatchProcessorHolder processorExtent = new ExtentTraverser<>(this.getExtent()).findAndGet(ExtentBatchProcessorHolder.class);
         if (processorExtent != null) {
            maskingExtent = new ProcessorTraverser<>(processorExtent.getProcessor()).find(MaskingExtent.class);
         }
      }

      return maskingExtent != null ? maskingExtent.getMask() : null;
   }

   public Mask getSourceMask() {
      ExtentTraverser<SourceMaskExtent> maskingExtent = new ExtentTraverser<>(this.getExtent()).find(SourceMaskExtent.class);
      return maskingExtent != null ? maskingExtent.get().getMask() : null;
   }

   @Nullable
   public Region[] getAllowedRegions() {
      return this.allowedRegions;
   }

   public void addTransform(ResettableExtent transform) {
      Preconditions.checkNotNull(transform);
      transform.setExtent(this.getExtent());
      new ExtentTraverser<>(this).setNext(transform);
   }

   @Nullable
   public ResettableExtent getTransform() {
      ExtentTraverser<ResettableExtent> traverser = new ExtentTraverser<>(this.getExtent()).find(ResettableExtent.class);
      return traverser != null ? traverser.get() : null;
   }

   public void setSourceMask(Mask mask) {
      if (mask == null) {
         mask = Masks.alwaysTrue();
      } else {
         new MaskTraverser(mask).reset(this);
      }

      ExtentTraverser<SourceMaskExtent> maskingExtent = new ExtentTraverser<>(this.getExtent()).find(SourceMaskExtent.class);
      if (maskingExtent != null && maskingExtent.get() != null) {
         Mask oldMask = maskingExtent.get().getMask();
         if (oldMask instanceof ResettableMask) {
            ((ResettableMask)oldMask).reset();
         }

         maskingExtent.get().setMask(mask);
      } else if (mask != Masks.alwaysTrue()) {
         SourceMaskExtent next = new SourceMaskExtent(this.getExtent(), mask);
         new ExtentTraverser<>(this).setNext(next);
      }
   }

   public void addSourceMask(Mask mask) {
      Preconditions.checkNotNull(mask);
      Mask existing = this.getSourceMask();
      if (existing != null) {
         MaskIntersection var4;
         if (existing instanceof MaskIntersection) {
            Collection<Mask> masks = new HashSet<>(((MaskIntersection)existing).getMasks());
            masks.add(mask);
            var4 = new MaskIntersection(masks);
         } else {
            var4 = new MaskIntersection(existing, mask);
         }

         mask = var4.optimize();
      }

      this.setSourceMask(mask);
   }

   public void setMask(@Nullable Mask mask) {
      if (mask == null) {
         mask = Masks.alwaysTrue();
      } else {
         new MaskTraverser(mask).reset(this);
      }

      MaskingExtent maskingExtent = new ExtentTraverser<>(this.getExtent()).findAndGet(MaskingExtent.class);
      if (maskingExtent == null && mask != Masks.alwaysTrue()) {
         ExtentBatchProcessorHolder processorExtent = new ExtentTraverser<>(this.getExtent()).findAndGet(ExtentBatchProcessorHolder.class);
         if (processorExtent != null) {
            maskingExtent = new ProcessorTraverser<>(processorExtent.getProcessor()).find(MaskingExtent.class);
         }
      }

      if (maskingExtent != null) {
         Mask oldMask = maskingExtent.getMask();
         if (oldMask instanceof ResettableMask) {
            ((ResettableMask)oldMask).reset();
         }

         maskingExtent.setMask(mask);
      } else if (mask != Masks.alwaysTrue()) {
         this.addProcessor(new MaskingExtent(this.getExtent(), mask));
      }
   }

   public SurvivalModeExtent getSurvivalExtent() {
      ExtentTraverser<SurvivalModeExtent> survivalExtent = new ExtentTraverser<>(this.getExtent()).find(SurvivalModeExtent.class);
      if (survivalExtent != null) {
         return survivalExtent.get();
      } else {
         SurvivalModeExtent survival = new SurvivalModeExtent(this.getExtent(), this.getWorld());
         this.setExtent(survival);
         return survival;
      }
   }

   @Deprecated
   public void setFastMode(boolean enabled) {
      this.disableHistory(enabled);
   }

   public void setSideEffectApplier(SideEffectSet sideEffectSet) {
   }

   public SideEffectSet getSideEffectApplier() {
      return SideEffectSet.defaults();
   }

   @Deprecated
   public void disableHistory(boolean disableHistory) {
      if (disableHistory) {
         if (this.history) {
            this.disableHistory();
            this.history = false;
         }
      } else if (this.history) {
         if (this.changeSet == null) {
            throw new IllegalArgumentException("History was never provided, cannot enable");
         }

         this.enableHistory(this.changeSet);
      }
   }

   @Deprecated
   public boolean hasFastMode() {
      return this.getChangeSet() == null;
   }

   public BlockBag getBlockBag() {
      return this.blockBag;
   }

   public void setBlockBag(BlockBag blockBag) {
      throw new UnsupportedOperationException("TODO - this is never called anyway");
   }

   @Override
   public String toString() {
      return super.toString() + ":" + this.getExtent();
   }

   public Map<BlockType, Integer> popMissingBlocks() {
      BlockBag bag = this.getBlockBag();
      if (bag != null) {
         bag.flushChanges();
         ChangeSet changeSet = this.getChangeSet();
         Map<BlockType, Integer> missingBlocks;
         if (changeSet instanceof BlockBagChangeSet) {
            missingBlocks = ((BlockBagChangeSet)changeSet).popMissing();
         } else {
            ExtentTraverser<BlockBagExtent> find = new ExtentTraverser<>(this.getExtent()).find(BlockBagExtent.class);
            if (find != null && find.get() != null) {
               missingBlocks = find.get().popMissing();
            } else {
               missingBlocks = null;
            }
         }

         if (missingBlocks != null && !missingBlocks.isEmpty()) {
            StringBuilder str = new StringBuilder();
            int size = missingBlocks.size();
            int i = 0;

            for (Entry<BlockType, Integer> entry : missingBlocks.entrySet()) {
               BlockType type = entry.getKey();
               int amount = entry.getValue();
               str.append(type.getName()).append(amount != 1 ? "x" + amount : "");
               if (++i != size) {
                  str.append(", ");
               }
            }

            this.actor.print(Caption.of("fawe.error.worldedit.some.fails.blockbag", str.toString()));
         }
      }

      return Collections.emptyMap();
   }

   public boolean isBatchingChunks() {
      return false;
   }

   public void setBatchingChunks(boolean batchingChunks) {
      if (batchingChunks) {
         this.enableQueue();
      } else {
         this.disableQueue();
      }
   }

   public boolean isBufferingEnabled() {
      return this.isBatchingChunks();
   }

   public void disableBuffering() {
      this.disableQueue();
   }

   public boolean isTickingWatchdog() {
      return this.watchdogExtents.stream().anyMatch(WatchdogTickingExtent::isEnabled);
   }

   public void setTickingWatchdog(boolean active) {
      for (WatchdogTickingExtent extent : this.watchdogExtents) {
         extent.setEnabled(active);
      }
   }

   public int getBlockChangeCount() {
      return this.changes;
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return this.getExtent().fullySupports3DBiomes();
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getExtent().getBiome(position);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;
         return this.getExtent().setBiome(position, biome);
      } else {
         return false;
      }
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      if (y >= this.minY && y <= this.maxY) {
         this.changes++;
         return this.getExtent().setBiome(x, y, z, biome);
      } else {
         return false;
      }
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
      for (int y = maxY; y >= minY; y--) {
         if (this.getBlock(x, y, z).getBlockType().getMaterial().isMovementBlocker()) {
            return y;
         }
      }

      return minY;
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY, Mask filter) {
      for (int y = maxY; y >= minY; y--) {
         if (filter.test(this.mutableBlockVector3.setComponents(x, y, z))) {
            return y;
         }
      }

      return minY;
   }

   public BlockType getBlockType(int x, int y, int z) {
      return this.getBlock(x, y, z).getBlockType();
   }

   @Deprecated
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, EditSession.Stage stage) throws WorldEditException {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;
         switch (stage) {
            case BEFORE_HISTORY:
               return this.getExtent().setBlock(position, block);
            case BEFORE_REORDER:
               return this.bypassAll.setBlock(position, block);
            case BEFORE_CHANGE:
               return this.bypassHistory.setBlock(position, block);
            default:
               throw new RuntimeException("New enum entry added that is unhandled here");
         }
      } else {
         return false;
      }
   }

   @Deprecated
   public <B extends BlockStateHolder<B>> boolean rawSetBlock(BlockVector3 position, B block) {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;

         try {
            return this.bypassAll.setBlock(position, block);
         } catch (WorldEditException var4) {
            throw new RuntimeException("Unexpected exception", var4);
         }
      } else {
         return false;
      }
   }

   public <B extends BlockStateHolder<B>> boolean smartSetBlock(BlockVector3 position, B block) {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;

         try {
            return this.setBlock(position, block, EditSession.Stage.BEFORE_REORDER);
         } catch (WorldEditException var4) {
            throw new RuntimeException("Unexpected exception", var4);
         }
      } else {
         return false;
      }
   }

   @Deprecated
   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws MaxChangedBlocksException {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;

         try {
            return this.getExtent().setBlock(position, block);
         } catch (MaxChangedBlocksException var4) {
            throw var4;
         } catch (WorldEditException var5) {
            throw new RuntimeException("Unexpected exception", var5);
         }
      } else {
         return false;
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) {
      if (y >= this.minY && y <= this.maxY) {
         this.changes++;

         try {
            return this.getExtent().setBlock(x, y, z, block);
         } catch (WorldEditException var6) {
            throw new RuntimeException("Unexpected exception", var6);
         }
      } else {
         return false;
      }
   }

   public boolean setBlock(int x, int y, int z, Pattern pattern) {
      if (y >= this.minY && y <= this.maxY) {
         this.changes++;

         try {
            BlockVector3 bv = this.mutableBlockVector3.setComponents(x, y, z);
            return pattern.apply(this.getExtent(), bv, bv);
         } catch (WorldEditException var6) {
            throw new RuntimeException("Unexpected exception", var6);
         }
      } else {
         return false;
      }
   }

   public boolean setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
      if (position.y() >= this.minY && position.y() <= this.maxY) {
         this.changes++;

         try {
            return pattern.apply(this.getExtent(), position, position);
         } catch (WorldEditException var4) {
            throw new RuntimeException(var4);
         }
      } else {
         return false;
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
      return this.changes = super.setBlocks(region, block);
   }

   @Override
   public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      return this.changes = super.setBlocks(region, pattern);
   }

   @Override
   public int setBlocks(Set<BlockVector3> vset, Pattern pattern) {
      return this.changes = super.setBlocks(vset, pattern);
   }

   public void undo(EditSession editSession) {
      UndoContext context = new UndoContext();
      context.setExtent(editSession.bypassAll);
      ChangeSet changeSet = this.getChangeSet();
      this.setChangeSet(null);
      Operations.completeBlindly(
         ChangeSetExecutor.create(changeSet, context, ChangeSetExecutor.Type.UNDO, editSession.getBlockBag(), editSession.getLimit().INVENTORY_MODE)
      );
      this.flushQueue();
      editSession.changes = 1;
   }

   public void setBlocks(ChangeSet changeSet, ChangeSetExecutor.Type type) {
      UndoContext context = new UndoContext();
      context.setExtent(this.bypassAll);
      Operations.completeBlindly(ChangeSetExecutor.create(changeSet, context, type, this.getBlockBag(), this.getLimit().INVENTORY_MODE));
      this.flushQueue();
      this.changes = 1;
   }

   public void redo(EditSession editSession) {
      UndoContext context = new UndoContext();
      context.setExtent(editSession.bypassAll);
      ChangeSet changeSet = this.getChangeSet();
      this.setChangeSet(null);
      Operations.completeBlindly(
         ChangeSetExecutor.create(changeSet, context, ChangeSetExecutor.Type.REDO, editSession.getBlockBag(), editSession.getLimit().INVENTORY_MODE)
      );
      this.flushQueue();
      editSession.changes = 1;
   }

   public boolean isTrackingHistory() {
      return this.history;
   }

   public void setTrackingHistory(boolean trackHistory) {
      if (trackHistory) {
         if (this.history) {
            if (this.changeSet == null) {
               throw new IllegalStateException("No ChangeSetExtent is available");
            }

            this.enableHistory(this.changeSet);
         }
      } else if (this.history) {
         this.disableHistory();
         this.history = false;
      }
   }

   public int size() {
      return this.getBlockChangeCount();
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.getWorld().getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.getWorld().getMaximumPoint();
   }

   public void setSize(int size) {
      this.changes = size;
   }

   @Override
   public void close() {
      this.flushQueue();
      this.dumpTracingInformation();
   }

   private void dumpTracingInformation() {
      if (this.tracingExtents != null) {
         List<TracingExtent> tracingExtents = this.getActiveTracingExtents();

         assert this.actor != null;

         if (tracingExtents.isEmpty()) {
            this.actor.print(TextComponent.of("worldedit.trace.no-tracing-extents"));
         } else {
            Set<List<TracingExtent>> stacks = new LinkedHashSet<>();
            Map<List<TracingExtent>, BlockVector3> stackToPosition = new HashMap<>();
            Set<BlockVector3> touchedLocations = Collections.newSetFromMap(BlockMap.create());

            for (TracingExtent tracingExtent : tracingExtents) {
               touchedLocations.addAll(tracingExtent.getTouchedLocations());
            }

            for (BlockVector3 loc : touchedLocations) {
               List<TracingExtent> stack = tracingExtents.stream().filter(it -> it.getTouchedLocations().contains(loc)).collect(Collectors.toList());
               boolean anyFailed = stack.stream().anyMatch(it -> it.getFailedActions().containsKey(loc));
               if (anyFailed && stacks.add(stack)) {
                  stackToPosition.put(stack, loc);
               }
            }

            stackToPosition.forEach(
               (stackx, position) -> {
                  TracingExtent failure = (TracingExtent)stackx.get(0);
                  this.actor
                     .printDebug(
                        Caption.of(
                           "worldedit.trace.action-failed",
                           failure.getFailedActions().get(position).toString(),
                           position.toString(),
                           failure.getExtent().getClass().getName()
                        )
                     );
               }
            );
         }
      }
   }

   @Deprecated
   public void flushSession() {
      this.flushQueue();
   }

   public void flushQueue() {
      Operations.completeBlindly(this.commit());
      FaweLimit used = this.getLimitUsed();
      if (used.MAX_FAILS.get() > 0) {
         if (used.MAX_CHANGES.get() <= 0L && used.MAX_ENTITIES.get() <= 0) {
            if (new ExtentTraverser<>(this.getExtent()).findAndGet(FaweRegionExtent.class) != null) {
               this.actor.print(Caption.of("fawe.cancel.reason.outside.region"));
            } else {
               this.actor.print(Caption.of("fawe.cancel.reason.outside.level"));
            }
         } else {
            this.actor.print(Caption.of("fawe.error.worldedit.some.fails", used.MAX_FAILS));
         }
      }

      if (this.wnaMode) {
         this.getWorld().flush();
      }

      this.limit.set(this.originalLimit);

      try {
         if (this.relighter != null && !(this.relighter instanceof NullRelighter) && !this.relighter.isFinished() && this.relighter.getLock().tryLock()) {
            try {
               if (Settings.settings().LIGHTING.REMOVE_FIRST) {
                  this.relighter.removeAndRelight(true);
               } else {
                  this.relighter.fixLightingSafe(true);
               }
            } finally {
               this.relighter.getLock().unlock();
            }
         }
      } catch (Throwable var8) {
         this.actor.print(Caption.of("fawe.error.lighting"));
         var8.printStackTrace();
      }

      if (this.getActor() instanceof Player) {
         Preloader preloader = Fawe.<IFawe>platform().getPreloader(false);
         if (preloader != null) {
            preloader.cancel(this.getActor());
         }
      }

      if (this.getChangeSet() != null) {
         if (Settings.settings().HISTORY.COMBINE_STAGES) {
            ((AbstractChangeSet)this.getChangeSet()).closeAsync();
         } else {
            try {
               this.getChangeSet().close();
            } catch (IOException var6) {
               throw new RuntimeException(var6);
            }
         }
      }
   }

   public <B extends BlockStateHolder<B>> int fall(final Region region, boolean fullHeight, final B replace) {
      FlatRegion flat = Regions.asFlatRegion(region);
      int startPerformY = region.getMinimumPoint().y();
      int startCheckY = fullHeight ? this.getMinY() : startPerformY;
      int endY = region.getMaximumPoint().y();
      RegionVisitor visitor = new RegionVisitor(flat, pos -> {
         int x = pos.x();
         int z = pos.z();
         int freeSpot = startCheckY;

         for (int y = startCheckY; y <= endY; y++) {
            if (y < startPerformY) {
               if (!this.getBlockType(x, y, z).getMaterial().isAir()) {
                  freeSpot = y + 1;
               }
            } else {
               BlockType block = this.getBlockType(x, y, z);
               if (!block.getMaterial().isAir()) {
                  if (freeSpot != y) {
                     this.setBlock(x, freeSpot, z, block);
                     this.setBlock(x, y, z, replace);
                  }

                  freeSpot++;
               }
            }
         }

         return true;
      }, this);
      Operations.completeBlindly(visitor);
      return this.changes;
   }

   @Override
   public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
      return this.changes = super.replaceBlocks(region, filter, replacement);
   }

   @Override
   public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
      return this.changes = super.replaceBlocks(region, filter, pattern);
   }

   @Override
   public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
      return this.changes = super.replaceBlocks(region, mask, pattern);
   }

   public int fillDirection(final BlockVector3 origin, final Pattern pattern, final double radius, final int depth, BlockVector3 direction) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(origin);
      Preconditions.checkNotNull(pattern);
      Preconditions.checkArgument(radius >= 0.0, "radius >= 0");
      Preconditions.checkArgument(depth >= 1, "depth >= 1");
      if (direction.equals(BlockVector3.UNIT_MINUS_Y)) {
         return this.fillXZ(origin, pattern, radius, depth, false);
      } else {
         Mask mask = new MaskIntersection(
            new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))), Masks.negate(new ExistingBlockMask(this))
         );
         BlockReplace replace = new BlockReplace(this, pattern);
         RecursiveVisitor visitor = new DirectionalVisitor(mask, replace, origin, direction, (int)(radius * 2.0 + 1.0), this.minY, this.maxY);
         visitor.visit(origin);
         Operations.completeBlindly(visitor);
         return this.changes = visitor.getAffected();
      }
   }

   public <B extends BlockStateHolder<B>> int fillXZ(BlockVector3 origin, B block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
      return this.fillXZ(origin, block, radius, depth, recursive);
   }

   public int fillXZ(BlockVector3 origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(origin);
      Preconditions.checkNotNull(pattern);
      Preconditions.checkArgument(radius >= 0.0, "radius >= 0");
      Preconditions.checkArgument(depth >= 1, "depth >= 1");
      int lowerBound = origin.y() - depth + 1;
      if (lowerBound > origin.y()) {
         lowerBound = Integer.MIN_VALUE;
      }

      Mask mask = new MaskIntersection(
         new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))),
         new BoundedHeightMask(Math.max(lowerBound, this.minY), Math.min(this.maxY, origin.y())),
         Masks.negate(new ExistingBlockMask(this))
      );
      BlockReplace replace = new BlockReplace(this, pattern);
      RecursiveVisitor visitor;
      if (recursive) {
         visitor = new RecursiveVisitor(mask, replace, (int)(radius * 2.0 + 1.0), this.minY, this.maxY, this);
      } else {
         visitor = new DownwardVisitor(mask, replace, origin.y(), (int)(radius * 2.0 + 1.0), this.minY, this.maxY, this);
      }

      visitor.visit(origin);
      Operations.completeLegacy(visitor);
      return this.changes = visitor.getAffected();
   }

   public int removeAbove(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(position);
      Preconditions.checkArgument(apothem >= 1, "apothem >= 1");
      Preconditions.checkArgument(height >= 1, "height >= 1");
      Region region = new CuboidRegion(this.getWorld(), position.add(-apothem + 1, 0, -apothem + 1), position.add(apothem - 1, height - 1, apothem - 1));
      return this.setBlocks(region, BlockTypes.AIR.getDefaultState());
   }

   public int removeBelow(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(position);
      Preconditions.checkArgument(apothem >= 1, "apothem >= 1");
      Preconditions.checkArgument(height >= 1, "height >= 1");
      Region region = new CuboidRegion(this.getWorld(), position.add(-apothem + 1, 0, -apothem + 1), position.add(apothem - 1, -height + 1, apothem - 1));
      return this.setBlocks(region, BlockTypes.AIR.getDefaultState());
   }

   public int removeNear(BlockVector3 position, Mask mask, int apothem) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(position);
      Preconditions.checkArgument(apothem >= 1, "apothem >= 1");
      BlockVector3 adjustment = BlockVector3.ONE.multiply(apothem - 1);
      Region region = new CuboidRegion(this.getWorld(), position.add(adjustment.multiply(-1)), position.add(adjustment));
      return this.replaceBlocks(region, mask, BlockTypes.AIR.getDefaultState());
   }

   @Override
   public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      Vector3 center = region.getCenter();
      Region centerRegion = new CuboidRegion(
         this.getWorld(),
         BlockVector3.at((int)center.x(), (int)center.y(), (int)center.z()),
         BlockVector3.at(MathUtils.roundHalfUp(center.x()), MathUtils.roundHalfUp(center.y()), MathUtils.roundHalfUp(center.z()))
      );
      return this.setBlocks(centerRegion, pattern);
   }

   @Deprecated
   public <B extends BlockStateHolder<B>> int makeCuboidFaces(Region region, B block) throws MaxChangedBlocksException {
      return this.makeCuboidFaces(region, block);
   }

   public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
      Region faces = cuboid.getFaces();
      return this.setBlocks(faces, pattern);
   }

   public int makeFaces(final Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      return region instanceof CuboidRegion ? this.makeCuboidFaces(region, pattern) : new RegionShape(region).generate(this, pattern, true);
   }

   public <B extends BlockStateHolder<B>> int makeCuboidWalls(Region region, B block) throws MaxChangedBlocksException {
      return this.makeCuboidWalls(region, block);
   }

   public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
      Region faces = cuboid.getWalls();
      return this.setBlocks((Set<BlockVector3>)faces, pattern);
   }

   public int makeWalls(final Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      if (region instanceof CuboidRegion) {
         return this.makeCuboidWalls(region, pattern);
      } else {
         this.replaceBlocks(region, new WallMakeMask(region), pattern);
         return this.changes;
      }
   }

   @Deprecated
   public <B extends BlockStateHolder<B>> int overlayCuboidBlocks(Region region, B block) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(block);
      return this.overlayCuboidBlocks(region, block);
   }

   public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      BlockReplace replace = new BlockReplace(this, pattern);
      RegionOffset offset = new RegionOffset(BlockVector3.UNIT_Y, replace);
      int minY = region.getMinimumPoint().y();
      int maxY = Math.min(this.getMaximumPoint().y(), region.getMaximumPoint().y() + 1);
      SurfaceRegionFunction surface = new SurfaceRegionFunction(this, offset, minY, maxY);
      FlatRegionVisitor visitor = new FlatRegionVisitor(Regions.asFlatRegion(region), surface, this);
      Operations.completeBlindly(visitor);
      return this.changes = visitor.getAffected();
   }

   public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Naturalizer naturalizer = new Naturalizer(this);
      FlatRegion flatRegion = Regions.asFlatRegion(region);
      LayerVisitor visitor = new LayerVisitor(flatRegion, Regions.minimumBlockY(region), Regions.maximumBlockY(region), naturalizer, this);
      Operations.completeBlindly(visitor);
      return this.changes = naturalizer.getAffected();
   }

   public int stackCuboidRegion(Region region, BlockVector3 dir, int count, boolean copyAir) throws MaxChangedBlocksException {
      return this.stackCuboidRegion(region, dir, count, true, false, copyAir ? null : new ExistingBlockMask(this));
   }

   public int stackCuboidRegion(Region region, BlockVector3 offset, int count, boolean copyEntities, boolean copyBiomes, Mask mask) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(offset);
      BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);

      try {
         return this.stackRegionBlockUnits(region, offset.multiply(size), count, copyEntities, copyBiomes, mask);
      } catch (RegionOperationException var9) {
         throw new AssertionError(var9);
      }
   }

   public int stackRegionBlockUnits(Region region, BlockVector3 offset, int count, boolean copyEntities, boolean copyBiomes, Mask mask) throws MaxChangedBlocksException, RegionOperationException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(offset);
      Preconditions.checkArgument(count >= 1, "count >= 1 required");
      BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
      BlockVector3 offsetAbs = offset.abs();
      if (offsetAbs.x() < size.x() && offsetAbs.y() < size.y() && offsetAbs.z() < size.z()) {
         throw new RegionOperationException(Caption.of("worldedit.stack.intersecting-region"));
      } else {
         BlockVector3 to = region.getMinimumPoint();
         ForwardExtentCopy copy = new ForwardExtentCopy(this, region, this, to);
         copy.setRepetitions(count);
         copy.setTransform(new AffineTransform().translate(offset));
         copy.setCopyingEntities(copyEntities);
         copy.setCopyingBiomes(copyBiomes);
         Region allowedRegion;
         if (this.allowedRegions != null && this.allowedRegions.length != 0) {
            allowedRegion = new RegionIntersection(this.allowedRegions);
         } else {
            allowedRegion = new NullRegion();
         }

         mask = MaskIntersection.of(this.getSourceMask(), mask, new RegionMask(allowedRegion)).optimize();
         if (mask != Masks.alwaysTrue()) {
            this.setSourceMask(null);
            copy.setSourceMask(mask);
         }

         Operations.completeLegacy(copy);
         return this.changes = copy.getAffected();
      }
   }

   public int moveRegion(Region region, BlockVector3 offset, int multiplier, boolean copyAir, boolean moveEntities, boolean copyBiomes, Pattern replacement) throws MaxChangedBlocksException {
      Mask mask = null;
      if (!copyAir) {
         mask = new ExistingBlockMask(this);
      }

      return this.moveRegion(region, offset, multiplier, moveEntities, copyBiomes, mask, replacement);
   }

   public int moveRegion(Region region, BlockVector3 offset, int multiplier, boolean moveEntities, boolean copyBiomes, Mask mask, Pattern replacement) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(offset);
      Preconditions.checkArgument(multiplier >= 1, "distance >= 1 required");
      Preconditions.checkArgument(!copyBiomes || region instanceof FlatRegion, "can't copy biomes from non-flat region");
      BlockVector3 to = region.getMinimumPoint().add(offset.multiply(multiplier));
      BlockVector3 displace = offset.multiply(multiplier);
      BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
      BlockVector3 disAbs = displace.abs();
      if (disAbs.x() < size.x() && disAbs.y() < size.y() && disAbs.z() < size.z()) {
         this.enableQueue();
      }

      ForwardExtentCopy copy = new ForwardExtentCopy(this, region, this, to);
      if (replacement == null) {
         replacement = BlockTypes.AIR.getDefaultState();
      }

      BlockReplace remove = replacement instanceof ExistingPattern ? null : new BlockReplace(this, replacement);
      copy.setSourceFunction(remove);
      copy.setCopyingEntities(moveEntities);
      copy.setRemovingEntities(moveEntities);
      copy.setCopyingBiomes(copyBiomes);
      copy.setRepetitions(1);
      Region allowedRegion;
      if (this.allowedRegions != null && this.allowedRegions.length != 0) {
         allowedRegion = new RegionIntersection(this.allowedRegions);
      } else {
         allowedRegion = new NullRegion();
      }

      Mask sourceMask = this.getSourceMask();
      mask = MaskIntersection.of(sourceMask, mask, new RegionMask(allowedRegion)).optimize();
      if (mask != Masks.alwaysTrue()) {
         copy.setSourceMask(mask);
         if (sourceMask != null && sourceMask.equals(mask)) {
            this.setSourceMask(null);
         }
      }

      Operations.completeBlindly(copy);
      return this.changes = copy.getAffected();
   }

   public int moveCuboidRegion(Region region, BlockVector3 dir, int distance, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException {
      return this.moveRegion(region, dir, distance, copyAir, true, false, replacement);
   }

   public int drainArea(BlockVector3 origin, double radius) throws MaxChangedBlocksException {
      return this.drainArea(origin, radius, false);
   }

   public int drainArea(BlockVector3 origin, double radius, boolean waterlogged) throws MaxChangedBlocksException {
      return this.drainArea(origin, radius, waterlogged, false);
   }

   public int drainArea(BlockVector3 origin, double radius, boolean waterlogged, boolean plants) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(origin);
      Preconditions.checkArgument(radius >= 0.0, "radius >= 0 required");
      Mask liquidMask;
      if (plants) {
         liquidMask = new BlockTypeMask(
            this,
            BlockTypes.LAVA,
            BlockTypes.WATER,
            BlockTypes.BUBBLE_COLUMN,
            BlockTypes.KELP_PLANT,
            BlockTypes.KELP,
            BlockTypes.SEAGRASS,
            BlockTypes.TALL_SEAGRASS
         );
      } else {
         liquidMask = new BlockMaskBuilder().addTypes(BlockTypes.WATER, BlockTypes.LAVA, BlockTypes.BUBBLE_COLUMN).build(this);
      }

      if (waterlogged) {
         Map<String, String> stateMap = new HashMap<>();
         stateMap.put("waterlogged", "true");
         liquidMask = new MaskUnion(liquidMask, new BlockStateMask(this, stateMap, true));
      }

      Mask mask = new MaskIntersection(
         new BoundedHeightMask(this.minY, this.maxY), new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))), liquidMask
      );
      BlockReplace replace;
      if (waterlogged) {
         replace = new BlockReplace(this, new WaterloggedRemover(this));
      } else {
         replace = new BlockReplace(this, BlockTypes.AIR.getDefaultState());
      }

      RecursiveVisitor visitor = new RecursiveVisitor(mask, replace, (int)(radius * 2.0 + 1.0), this.minY, this.maxY, this);

      for (BlockVector3 position : CuboidRegion.fromCenter(origin, 1)) {
         if (mask.test(position)) {
            visitor.visit(position);
         }
      }

      Operations.completeLegacy(visitor);
      return this.changes = visitor.getAffected();
   }

   public int fixLiquid(BlockVector3 origin, double radius, BlockType fluid) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(origin);
      Preconditions.checkArgument(radius >= 0.0, "radius >= 0 required");
      Mask liquidMask = new SingleBlockTypeMask(this, fluid);
      MaskIntersection blockMask = new MaskUnion(liquidMask, Masks.negate(new ExistingBlockMask(this)));
      Mask mask = new MaskIntersection(
         new BoundedHeightMask(this.minY, Math.min(origin.y(), this.maxY)),
         new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))),
         blockMask
      );
      BlockReplace replace = new BlockReplace(this, fluid.getDefaultState());
      NonRisingVisitor visitor = new NonRisingVisitor(mask, replace, Integer.MAX_VALUE, this.minY, this.maxY, this);

      for (BlockVector3 position : CuboidRegion.fromCenter(origin, 1)) {
         if (liquidMask.test(position)) {
            visitor.visit(position);
         }
      }

      Operations.completeLegacy(visitor);
      return visitor.getAffected();
   }

   public int makeCylinder(BlockVector3 pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
      return this.makeCylinder(pos, block, radius, radius, height, filled);
   }

   public int makeCylinder(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
      return this.makeCylinder(pos, block, radiusX, radiusZ, height, 0.0, filled);
   }

   public int makeHollowCylinder(BlockVector3 pos, final Pattern block, double radiusX, double radiusZ, int height, double thickness) throws MaxChangedBlocksException {
      return this.makeCylinder(pos, block, radiusX, radiusZ, height, thickness, false);
   }

   public int makeCylinder(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, double thickness, boolean filled) throws MaxChangedBlocksException {
      radiusX += 0.5;
      radiusZ += 0.5;
      MutableBlockVector3 mutableBlockVector3 = new MutableBlockVector3(pos);
      if (height == 0) {
         return 0;
      } else {
         if (height < 0) {
            height = -height;
            mutableBlockVector3.mutY(mutableBlockVector3.y() - height);
         }

         if (mutableBlockVector3.y() < this.getWorld().getMinY()) {
            mutableBlockVector3.mutY(this.world.getMinY());
         } else if (mutableBlockVector3.y() + height - 1 > this.maxY) {
            height = this.maxY - mutableBlockVector3.y() + 1;
         }

         double invRadiusX = 1.0 / radiusX;
         double invRadiusZ = 1.0 / radiusZ;
         int px = mutableBlockVector3.x();
         int py = mutableBlockVector3.y();
         int pz = mutableBlockVector3.z();
         int ceilRadiusX = (int)Math.ceil(radiusX);
         int ceilRadiusZ = (int)Math.ceil(radiusZ);
         double nextXn = 0.0;
         if (thickness != 0.0) {
            double nextMinXn = 0.0;
            double minInvRadiusX = 1.0 / (radiusX - thickness);
            double minInvRadiusZ = 1.0 / (radiusZ - thickness);

            for (int x = 0; x <= ceilRadiusX; x++) {
               double xn = nextXn;
               double dx2 = nextMinXn * nextMinXn;
               nextXn = (x + 1) * invRadiusX;
               nextMinXn = (x + 1) * minInvRadiusX;
               double nextZn = 0.0;
               double nextMinZn = 0.0;
               double xSqr = xn * xn;
               int xx = px + x;
               int x_x = px - x;

               for (int z = 0; z <= ceilRadiusZ; z++) {
                  double zSqr = nextZn * nextZn;
                  double distanceSq = xSqr + zSqr;
                  if (distanceSq > 1.0) {
                     if (z == 0) {
                        return this.changes;
                     }
                     break;
                  }

                  double dz2 = nextMinZn * nextMinZn;
                  nextZn = (z + 1) * invRadiusZ;
                  nextMinZn = (z + 1) * minInvRadiusZ;
                  if (!(dz2 + nextMinXn * nextMinXn <= 1.0) || !(nextMinZn * nextMinZn + dx2 <= 1.0)) {
                     int zz = pz + z;
                     int z_z = pz - z;

                     for (int y = 0; y < height; y++) {
                        int yy = py + y;
                        this.setBlock(xx, yy, zz, block);
                        this.setBlock(x_x, yy, zz, block);
                        this.setBlock(xx, yy, z_z, block);
                        this.setBlock(x_x, yy, z_z, block);
                     }
                  }
               }
            }
         } else {
            for (int x = 0; x <= ceilRadiusX; x++) {
               double xn = nextXn;
               nextXn = (x + 1) * invRadiusX;
               double nextZn = 0.0;
               double xSqr = xn * xn;
               int xx = px + x;
               int x_x = px - x;

               for (int z = 0; z <= ceilRadiusZ; z++) {
                  double zSqrx = nextZn * nextZn;
                  double distanceSqx = xSqr + zSqrx;
                  if (distanceSqx > 1.0) {
                     if (z == 0) {
                        return this.changes;
                     }
                     break;
                  }

                  nextZn = (z + 1) * invRadiusZ;
                  if (filled || !(zSqrx + nextXn * nextXn <= 1.0) || !(nextZn * nextZn + xSqr <= 1.0)) {
                     int zz = pz + z;
                     int z_z = pz - z;

                     for (int y = 0; y < height; y++) {
                        int yy = py + y;
                        this.setBlock(xx, yy, zz, block);
                        this.setBlock(x_x, yy, zz, block);
                        this.setBlock(xx, yy, z_z, block);
                        this.setBlock(x_x, yy, z_z, block);
                     }
                  }
               }
            }
         }

         return this.changes;
      }
   }

   public int makeCone(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled, double thickness) throws MaxChangedBlocksException {
      int affected = 0;
      int ceilRadiusX = (int)Math.ceil(radiusX);
      int ceilRadiusZ = (int)Math.ceil(radiusZ);
      double radiusXPow = Math.pow(radiusX, 2.0);
      double radiusZPow = Math.pow(radiusZ, 2.0);
      double heightPow = Math.pow(height, 2.0);
      int layers = Math.abs(height);
      int cx = pos.x();
      int cy = pos.y();
      int cz = pos.z();

      label71:
      for (int y = 0; y < layers; y++) {
         double ySquaredMinusHeightOverHeightSquared = Math.pow(y - layers, 2.0) / heightPow;
         int yy = height < 0 ? cy - y : cy + y;

         for (int x = 0; x <= ceilRadiusX; x++) {
            double xSquaredOverRadiusX = Math.pow(x, 2.0) / radiusXPow;

            for (int z = 0; z <= ceilRadiusZ; z++) {
               double zSquaredOverRadiusZ = Math.pow(z, 2.0) / radiusZPow;
               double distanceFromOriginMinusHeightSquared = xSquaredOverRadiusX + zSquaredOverRadiusZ - ySquaredMinusHeightOverHeightSquared;
               if (distanceFromOriginMinusHeightSquared > 1.0) {
                  if (z == 0) {
                     continue label71;
                  }
                  break;
               }

               if (!filled) {
                  double xNext = Math.pow(x + thickness, 2.0) / radiusXPow + zSquaredOverRadiusZ - ySquaredMinusHeightOverHeightSquared;
                  double yNext = xSquaredOverRadiusX + zSquaredOverRadiusZ - Math.pow(y + thickness - layers, 2.0) / radiusZPow;
                  double zNext = xSquaredOverRadiusX + Math.pow(z + thickness, 2.0) / heightPow - ySquaredMinusHeightOverHeightSquared;
                  if (xNext <= 0.0 && zNext <= 0.0 && yNext <= 0.0 && y + thickness != layers) {
                     continue;
                  }
               }

               if (distanceFromOriginMinusHeightSquared <= 0.0) {
                  if (this.setBlock(cx + x, yy, cz + z, block)) {
                     affected++;
                  }

                  if (this.setBlock(cx - x, yy, cz + z, block)) {
                     affected++;
                  }

                  if (this.setBlock(cx + x, yy, cz - z, block)) {
                     affected++;
                  }

                  if (this.setBlock(cx - x, yy, cz - z, block)) {
                     affected++;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int moveRegion(Region region, BlockVector3 dir, int distance, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException {
      return this.moveRegion(region, dir, distance, true, false, copyAir ? new ExistingBlockMask(this) : null, replacement);
   }

   public int makeCircle(BlockVector3 pos, final Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled, Vector3 normal) throws MaxChangedBlocksException {
      radiusX += 0.5;
      radiusY += 0.5;
      radiusZ += 0.5;
      normal = normal.normalize();
      double nx = normal.x();
      double ny = normal.y();
      double nz = normal.z();
      double invRadiusX = 1.0 / radiusX;
      double invRadiusY = 1.0 / radiusY;
      double invRadiusZ = 1.0 / radiusZ;
      int px = pos.x();
      int py = pos.y();
      int pz = pos.z();
      int ceilRadiusX = (int)Math.ceil(radiusX);
      int ceilRadiusY = (int)Math.ceil(radiusY);
      int ceilRadiusZ = (int)Math.ceil(radiusZ);
      double threshold = 0.5;
      double nextXn = 0.0;

      label81:
      for (int x = 0; x <= ceilRadiusX; x++) {
         double dx = nextXn * nextXn;
         nextXn = (x + 1) * invRadiusX;
         double nextXnSq = nextXn * nextXn;
         double nextYn = 0.0;
         int xx = px + x;
         int x_x = px - x;
         double xnx = x * nx;

         for (int y = 0; y <= ceilRadiusY; y++) {
            double dy = nextYn * nextYn;
            double dxy = dx + dy;
            nextYn = (y + 1) * invRadiusY;
            double nextYnSq = nextYn * nextYn;
            double nextZn = 0.0;
            int yy = py + y;
            int y_y = py - y;
            double yny = y * ny;

            for (int z = 0; z <= ceilRadiusZ; z++) {
               double dz = nextZn * nextZn;
               double dxyz = dxy + dz;
               double dxz = dx + dz;
               double dyz = dy + dz;
               nextZn = (z + 1) * invRadiusZ;
               double nextZnSq = nextZn * nextZn;
               if (dxyz > 1.0) {
                  if (z == 0) {
                     if (y == 0) {
                        return this.changes;
                     }
                     continue label81;
                  }
                  break;
               }

               if (filled || !(nextXnSq + dyz <= 1.0) || !(nextYnSq + dxz <= 1.0) || !(nextZnSq + dxy <= 1.0)) {
                  int zz = pz + z;
                  int z_z = pz - z;
                  double znz = z * nz;
                  if (Math.abs(xnx + yny + znz) < threshold) {
                     this.setBlock(xx, yy, zz, block);
                  }

                  if (Math.abs(-xnx + yny + znz) < threshold) {
                     this.setBlock(x_x, yy, zz, block);
                  }

                  if (Math.abs(xnx - yny + znz) < threshold) {
                     this.setBlock(xx, y_y, zz, block);
                  }

                  if (Math.abs(xnx + yny - znz) < threshold) {
                     this.setBlock(xx, yy, z_z, block);
                  }

                  if (Math.abs(-xnx - yny + znz) < threshold) {
                     this.setBlock(x_x, y_y, zz, block);
                  }

                  if (Math.abs(xnx - yny - znz) < threshold) {
                     this.setBlock(xx, y_y, z_z, block);
                  }

                  if (Math.abs(-xnx + yny - znz) < threshold) {
                     this.setBlock(x_x, yy, z_z, block);
                  }

                  if (Math.abs(-xnx - yny - znz) < threshold) {
                     this.setBlock(x_x, y_y, z_z, block);
                  }
               }
            }
         }
      }

      return this.changes;
   }

   public int makeSphere(BlockVector3 pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
      return this.makeSphere(pos, block, radius, radius, radius, filled);
   }

   public int makeSphere(BlockVector3 pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
      radiusX += 0.5;
      radiusY += 0.5;
      radiusZ += 0.5;
      double invRadiusX = 1.0 / radiusX;
      double invRadiusY = 1.0 / radiusY;
      double invRadiusZ = 1.0 / radiusZ;
      int px = pos.x();
      int py = pos.y();
      int pz = pos.z();
      int ceilRadiusX = (int)Math.ceil(radiusX);
      int ceilRadiusY = (int)Math.ceil(radiusY);
      int ceilRadiusZ = (int)Math.ceil(radiusZ);
      double nextXn = 0.0;

      label75:
      for (int x = 0; x <= ceilRadiusX; x++) {
         double dx = nextXn * nextXn;
         nextXn = (x + 1) * invRadiusX;
         double nextXnSq = nextXn * nextXn;
         int xx = px + x;
         int x_x = px - x;
         double nextZn = 0.0;

         for (int z = 0; z <= ceilRadiusZ; z++) {
            double dz = nextZn * nextZn;
            double dxz = dx + dz;
            nextZn = (z + 1) * invRadiusZ;
            double nextZnSq = nextZn * nextZn;
            int zz = pz + z;
            int z_z = pz - z;
            double nextYn = 0.0;

            for (int y = 0; y <= ceilRadiusY; y++) {
               double dy = nextYn * nextYn;
               double dxyz = dxz + dy;
               nextYn = (y + 1) * invRadiusY;
               if (dxyz > 1.0) {
                  if (y == 0) {
                     if (z == 0) {
                        return this.changes;
                     }
                     continue label75;
                  }
                  break;
               }

               double nextYnSq = nextYn * nextYn;
               double dxy = dx + dy;
               double dyz = dy + dz;
               if (filled || !(nextXnSq + dyz <= 1.0) || !(nextYnSq + dxz <= 1.0) || !(nextZnSq + dxy <= 1.0)) {
                  int yy = py + y;
                  if (yy <= this.maxY) {
                     this.setBlock(xx, yy, zz, block);
                     if (x != 0) {
                        this.setBlock(x_x, yy, zz, block);
                     }

                     if (z != 0) {
                        this.setBlock(xx, yy, z_z, block);
                        if (x != 0) {
                           this.setBlock(x_x, yy, z_z, block);
                        }
                     }
                  }

                  if (y != 0 && (yy = py - y) >= this.minY) {
                     this.setBlock(xx, yy, zz, block);
                     if (x != 0) {
                        this.setBlock(x_x, yy, zz, block);
                     }

                     if (z != 0) {
                        this.setBlock(xx, yy, z_z, block);
                        if (x != 0) {
                           this.setBlock(x_x, yy, z_z, block);
                        }
                     }
                  }
               }
            }
         }
      }

      return this.changes;
   }

   public int makePyramid(BlockVector3 position, Pattern block, int size, boolean filled) throws MaxChangedBlocksException {
      int bx = position.x();
      int by = position.y();
      int bz = position.z();
      int height = size;

      for (int y = 0; y <= height; y++) {
         size--;
         int yy = y + by;

         for (int x = 0; x <= size; x++) {
            int xx = bx + x;
            int x_x = bx - x;

            for (int z = 0; z <= size; z++) {
               int zz = bz + z;
               int z_z = bz - z;
               if (filled && z <= size && x <= size || z == size || x == size) {
                  this.setBlock(xx, yy, zz, block);
                  this.setBlock(x_x, yy, zz, block);
                  this.setBlock(xx, yy, z_z, block);
                  this.setBlock(x_x, yy, z_z, block);
               }
            }
         }
      }

      return this.changes;
   }

   @Deprecated
   public int thaw(BlockVector3 position, double radius) throws MaxChangedBlocksException {
      return this.thaw(position, radius, WorldEdit.getInstance().getConfiguration().defaultVerticalHeight);
   }

   public int thaw(BlockVector3 position, double radius, int height) throws MaxChangedBlocksException {
      int affected = 0;
      double radiusSq = radius * radius;
      int ox = position.x();
      int oy = position.y();
      int oz = position.z();
      BlockState air = BlockTypes.AIR.getDefaultState();
      BlockState water = BlockTypes.WATER.getDefaultState();
      int centerY = Math.max(this.minY, Math.min(this.maxY, oy));
      int minY = Math.max(this.minY, centerY - height);
      int maxY = Math.min(this.maxY, centerY + height);
      MutableBlockVector3 mutable = new MutableBlockVector3();
      MutableBlockVector3 mutable2 = new MutableBlockVector3();
      int ceilRadius = (int)Math.ceil(radius);

      for (int x = ox - ceilRadius; x <= ox + ceilRadius; x++) {
         for (int z = oz - ceilRadius; z <= oz + ceilRadius; z++) {
            if (!(mutable.setComponents(x, oy, z).distanceSq(position) > radiusSq)) {
               for (int y = maxY; y > minY; y--) {
                  mutable.setComponents(x, y, z);
                  mutable2.setComponents(x, y - 1, z);
                  BlockType id = this.getBlock(mutable).getBlockType();
                  if (id == BlockTypes.ICE) {
                     if (this.setBlock(mutable, water)) {
                        affected++;
                     }
                     break;
                  }

                  if (id == BlockTypes.SNOW) {
                     if (this.setBlock(mutable, air)) {
                        if (y > this.getMinY()) {
                           BlockState block = this.getBlock(mutable2);
                           if (block.getBlockType().hasProperty(SnowSimulator.SNOWY) && this.setBlock(mutable2, block.with(SnowSimulator.SNOWY, false))) {
                              affected++;
                           }
                        }

                        affected++;
                     }
                     break;
                  }

                  if (!id.getMaterial().isAir()) {
                     break;
                  }
               }
            }
         }
      }

      return affected;
   }

   @Deprecated
   public int simulateSnow(BlockVector3 position, double radius) throws MaxChangedBlocksException {
      return this.simulateSnow(position, radius, WorldEdit.getInstance().getConfiguration().defaultVerticalHeight);
   }

   public int simulateSnow(BlockVector3 position, double radius, int height) throws MaxChangedBlocksException {
      return this.simulateSnow(new CylinderRegion(position, Vector2.at(radius, radius), position.y(), height), false);
   }

   public int simulateSnow(FlatRegion region, boolean stack) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      SnowSimulator snowSimulator = new SnowSimulator(this, stack);
      LayerVisitor layerVisitor = new LayerVisitor(region, region.getMinimumY(), region.getMaximumY(), snowSimulator, this);
      Operations.completeLegacy(layerVisitor);
      return snowSimulator.getAffected();
   }

   @Deprecated
   public int green(BlockVector3 position, double radius, boolean onlyNormalDirt) throws MaxChangedBlocksException {
      return this.green(position, radius, WorldEdit.getInstance().getConfiguration().defaultVerticalHeight, onlyNormalDirt);
   }

   public int green(BlockVector3 position, double radius, int height, boolean onlyNormalDirt) throws MaxChangedBlocksException {
      int affected = 0;
      double radiusSq = radius * radius;
      int ox = position.x();
      int oy = position.y();
      int oz = position.z();
      BlockState grass = BlockTypes.GRASS_BLOCK.getDefaultState();
      int centerY = Math.max(this.minY, Math.min(this.maxY, oy));
      int minY = Math.max(this.minY, centerY - height);
      int maxY = Math.min(this.maxY, centerY + height);
      MutableBlockVector3 mutable = new MutableBlockVector3();
      int ceilRadius = (int)Math.ceil(radius);

      for (int x = ox - ceilRadius; x <= ox + ceilRadius; x++) {
         for (int z = oz - ceilRadius; z <= oz + ceilRadius; z++) {
            if (!(mutable.setComponents(x, oy, z).distanceSq(position) > radiusSq)) {
               for (int y = maxY; y > minY; y--) {
                  BlockState block = this.getBlock(mutable.mutY(y));
                  if (block.getBlockType() == BlockTypes.DIRT || !onlyNormalDirt && block.getBlockType() == BlockTypes.COARSE_DIRT) {
                     if (this.setBlock(mutable.mutY(y), grass)) {
                        affected++;
                     }
                     break;
                  }

                  if (block.getBlockType() == BlockTypes.WATER
                     || block.getBlockType() == BlockTypes.LAVA
                     || block.getBlockType().getMaterial().isMovementBlocker()) {
                     break;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int makePumpkinPatches(BlockVector3 position, int apothem) throws MaxChangedBlocksException {
      return this.makePumpkinPatches(position, apothem, 0.02);
   }

   public int makePumpkinPatches(BlockVector3 position, int apothem, double density) throws MaxChangedBlocksException {
      GardenPatchGenerator generator = new GardenPatchGenerator(this);
      generator.setPlant(GardenPatchGenerator.getPumpkinPattern());
      FlatRegion region = new CuboidRegion(this.getWorld(), position.add(-apothem, -5, -apothem), position.add(apothem, 10, apothem));
      GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), generator);
      LayerVisitor visitor = new LayerVisitor(region, Regions.minimumBlockY(region), Regions.maximumBlockY(region), ground, this);
      visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
      Operations.completeLegacy(visitor);
      return this.changes = ground.getAffected();
   }

   @Deprecated
   public int makeForest(BlockVector3 basePosition, int size, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException {
      return this.makeForest(CuboidRegion.fromCenter(basePosition, size), density, treeType);
   }

   @Deprecated
   public int makeForest(Region region, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException {
      ForestGenerator generator = new ForestGenerator(this, treeType);
      GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), generator);
      LayerVisitor visitor = new LayerVisitor(Regions.asFlatRegion(region), Regions.minimumBlockY(region), Regions.maximumBlockY(region), ground, this);
      visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
      Operations.completeLegacy(visitor);
      return ground.getAffected();
   }

   public int makeForest(BlockVector3 basePosition, int size, double density, TreeType treeType) throws MaxChangedBlocksException {
      return this.makeForest(CuboidRegion.fromCenter(basePosition, size), density, treeType);
   }

   public int makeForest(Region region, double density, TreeType treeType) throws MaxChangedBlocksException {
      com.sk89q.worldedit.function.generator.TreeGenerator generator = new com.sk89q.worldedit.function.generator.TreeGenerator(this, treeType);
      GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), generator);
      LayerVisitor visitor = new LayerVisitor(Regions.asFlatRegion(region), Regions.minimumBlockY(region), Regions.maximumBlockY(region), ground);
      visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
      Operations.completeLegacy(visitor);
      return ground.getAffected();
   }

   public List<Countable<BlockState>> getBlockDistribution(Region region, boolean separateStates) {
      if (separateStates) {
         List<Countable<BlockState>> distr = this.getBlockDistributionWithData(region);
         Collections.reverse(distr);
         return distr;
      } else {
         List<Countable<BlockType>> normalDistr = this.getBlockDistribution(region);
         List<Countable<BlockState>> distribution = new ArrayList<>();

         for (Countable<BlockType> count : normalDistr) {
            distribution.add(new Countable<>(count.getID().getDefaultState(), count.getAmount()));
         }

         Collections.reverse(distribution);
         return distribution;
      }
   }

   public int makeShape(final Region region, final Vector3 zero, final Vector3 unit, final Pattern pattern, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException {
      return this.makeShape(region, zero, unit, pattern, expressionString, hollow, WorldEdit.getInstance().getConfiguration().calculationTimeout);
   }

   public int makeShape(
      final Region region,
      final Vector3 zero,
      final Vector3 unit,
      final Pattern pattern,
      final String expressionString,
      final boolean hollow,
      final int timeout
   ) throws ExpressionException, MaxChangedBlocksException {
      final Expression expression = Expression.compile(expressionString, "x", "y", "z", "type", "data");
      expression.optimize();
      final LocalSlot.Variable typeVariable = expression.getSlots().getVariable("type").orElseThrow(IllegalStateException::new);
      final LocalSlot.Variable dataVariable = expression.getSlots().getVariable("data").orElseThrow(IllegalStateException::new);
      final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
      expression.setEnvironment(environment);
      final int[] timedOut = new int[]{0};
      ArbitraryShape shape = new ArbitraryShape(region) {
         @Override
         protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
            Vector3 current = Vector3.at(x, y, z);
            environment.setCurrentBlock(current);
            Vector3 scaled = current.subtract(zero).divide(unit);

            try {
               int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(defaultMaterial.toImmutableState());
               int typeVar = 0;
               int dataVar = 0;
               if (legacy != null) {
                  typeVar = legacy[0];
                  if (legacy.length > 1) {
                     dataVar = legacy[1];
                  }
               }

               if (expression.evaluate(new double[]{scaled.x(), scaled.y(), scaled.z(), typeVar, dataVar}, timeout) <= 0.0) {
                  return null;
               } else {
                  int newType = (int)typeVariable.value();
                  int newData = (int)dataVariable.value();
                  if (newType == typeVar && newData == dataVar) {
                     return defaultMaterial;
                  } else {
                     BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(newType, newData);
                     return state == null ? defaultMaterial : state.toBaseBlock();
                  }
               }
            } catch (ExpressionTimeoutException var13) {
               timedOut[0]++;
               return null;
            } catch (RuntimeException var14) {
               throw var14;
            } catch (Exception var15) {
               throw new RuntimeException(var15);
            }
         }
      };
      int changed = shape.generate(this, pattern, hollow);
      if (timedOut[0] > 0) {
         throw new ExpressionTimeoutException(
            String.format("%d blocks changed. %d blocks took too long to evaluate (increase with //timeout).", changed, timedOut[0])
         );
      } else {
         return changed;
      }
   }

   public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final String expressionString) throws ExpressionException, MaxChangedBlocksException {
      return this.deformRegion(region, zero, unit, expressionString, WorldEdit.getInstance().getConfiguration().calculationTimeout);
   }

   public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final String expressionString, final int timeout) throws ExpressionException, MaxChangedBlocksException {
      Expression expression = Expression.compile(expressionString, "x", "y", "z");
      expression.optimize();
      return this.deformRegion(region, zero, unit, expression, timeout);
   }

   public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final Expression expression, final int timeout) throws ExpressionException, MaxChangedBlocksException {
      LocalSlot.Variable x = expression.getSlots().getVariable("x").orElseThrow(IllegalStateException::new);
      LocalSlot.Variable y = expression.getSlots().getVariable("y").orElseThrow(IllegalStateException::new);
      LocalSlot.Variable z = expression.getSlots().getVariable("z").orElseThrow(IllegalStateException::new);
      WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
      expression.setEnvironment(environment);
      Vector3 zero2 = zero.add(0.5, 0.5, 0.5);
      RegionVisitor visitor = new RegionVisitor(region, position -> {
         try {
            Vector3 scaled = position.toVector3().subtract(zero).divide(unit);
            expression.evaluate(new double[]{scaled.x(), scaled.y(), scaled.z()}, timeout);
            int xv = (int)Math.floor(x.value() * unit.x() + zero2.x());
            int yv = (int)Math.floor(y.value() * unit.y() + zero2.y());
            int zv = (int)Math.floor(z.value() * unit.z() + zero2.z());
            BlockState get;
            if (yv >= this.minY && yv <= this.maxY) {
               get = this.getBlock(xv, yv, zv);
            } else {
               get = BlockTypes.AIR.getDefaultState();
            }

            return this.setBlock(position, get);
         } catch (EvaluationException var15) {
            throw new RuntimeException(var15);
         }
      }, this);
      Operations.completeBlindly(visitor);
      this.changes = this.changes + visitor.getAffected();
      return this.changes;
   }

   public int hollowOutRegion(Region region, int thickness, Pattern pattern, Mask mask) {
      try {
         Set<BlockVector3> outside = BlockVector3Set.getAppropriateVectorSet(region);
         BlockVector3 min = region.getMinimumPoint();
         BlockVector3 max = region.getMaximumPoint();
         int minX = min.x();
         int minY = min.y();
         int minZ = min.z();
         int maxX = max.x();
         int maxY = max.y();
         int maxZ = max.z();
         MutableBlockVector3 mutable = new MutableBlockVector3();

         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
               this.recurseHollow(region, mutable.setComponents(x, y, minZ), outside, mask);
               this.recurseHollow(region, mutable.setComponents(x, y, maxZ), outside, mask);
            }
         }

         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               this.recurseHollow(region, mutable.setComponents(minX, y, z), outside, mask);
               this.recurseHollow(region, mutable.setComponents(maxX, y, z), outside, mask);
            }
         }

         for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
               this.recurseHollow(region, mutable.setComponents(x, minY, z), outside, mask);
               this.recurseHollow(region, mutable.setComponents(x, maxY, z), outside, mask);
            }
         }

         for (int i = 1; i < thickness; i++) {
            Set<BlockVector3> newOutside = BlockVector3Set.getAppropriateVectorSet(region);
            Iterator var17 = region.iterator();

            label91:
            while (true) {
               if (var17.hasNext()) {
                  BlockVector3 position = (BlockVector3)var17.next();
                  BlockVector3[] var19 = recurseDirections;
                  int recurseDirection = var19.length;
                  int neighbor = 0;

                  while (true) {
                     if (neighbor >= recurseDirection) {
                        continue label91;
                     }

                     BlockVector3 recurseDirectionx = var19[neighbor];
                     BlockVector3 neighborx = mutable.setComponents(position).add(recurseDirectionx);
                     if (outside.contains(neighborx)) {
                        newOutside.add(position);
                        continue label91;
                     }

                     neighbor++;
                  }
               }

               outside.addAll(newOutside);
               break;
            }
         }

         label106:
         for (BlockVector3 position : region) {
            for (BlockVector3 recurseDirection : recurseDirections) {
               BlockVector3 neighbor = mutable.setComponents(position).add(recurseDirection);
               if (outside.contains(neighbor)) {
                  continue label106;
               }
            }

            this.changes++;
            pattern.apply(this.getExtent(), position, position);
         }
      } catch (WorldEditException var24) {
         throw new RuntimeException(var24);
      }

      return this.changes;
   }

   public int drawLine(Pattern pattern, BlockVector3 pos1, BlockVector3 pos2, double radius, boolean filled) throws MaxChangedBlocksException {
      return this.drawLine(pattern, pos1, pos2, radius, filled, false);
   }

   public int drawLine(Pattern pattern, BlockVector3 pos1, BlockVector3 pos2, double radius, boolean filled, boolean flat) throws MaxChangedBlocksException {
      int x1 = pos1.x();
      int y1 = pos1.y();
      int z1 = pos1.z();
      int x2 = pos2.x();
      int y2 = pos2.y();
      int z2 = pos2.z();
      int dx = Math.abs(x2 - x1);
      int dy = Math.abs(y2 - y1);
      int dz = Math.abs(z2 - z1);
      BlockVector3Set vset = BlockVector3Set.getAppropriateVectorSet(new CuboidRegion(pos1, pos2));
      boolean notdrawn = true;
      if (dx + dy + dz == 0) {
         vset.add(x1, y1, z1);
         notdrawn = false;
      }

      int dMax = Math.max(Math.max(dx, dy), dz);
      if (dMax == dx && notdrawn) {
         for (int domstep = 0; domstep <= dx; domstep++) {
            int var25 = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
            int var27 = (int)Math.round(y1 + (double)domstep * dy / dx * (y2 - y1 > 0 ? 1 : -1));
            int var29 = (int)Math.round(z1 + (double)domstep * dz / dx * (z2 - z1 > 0 ? 1 : -1));
            vset.add(var25, var27, var29);
         }
      } else if (dMax == dy && notdrawn) {
         for (int domstep = 0; domstep <= dy; domstep++) {
            int var26 = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
            int var24 = (int)Math.round(x1 + (double)domstep * dx / dy * (x2 - x1 > 0 ? 1 : -1));
            int var28 = (int)Math.round(z1 + (double)domstep * dz / dy * (z2 - z1 > 0 ? 1 : -1));
            vset.add(var24, var26, var28);
         }
      } else if (dMax == dz && notdrawn) {
         for (int domstep = 0; domstep <= dz; domstep++) {
            int tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
            int tipy = (int)Math.round(y1 + (double)domstep * dy / dz * (y2 - y1 > 0 ? 1 : -1));
            int tipx = (int)Math.round(x1 + (double)domstep * dx / dz * (x2 - x1 > 0 ? 1 : -1));
            vset.add(tipx, tipy, tipz);
         }
      }

      Set<BlockVector3> newVset;
      if (flat) {
         newVset = getStretched(vset, radius);
         if (!filled) {
            newVset = this.getOutline(newVset);
         }
      } else {
         newVset = getBallooned(vset, radius);
         if (!filled) {
            newVset = this.getHollowed(newVset);
         }
      }

      return this.changes = this.changes + this.setBlocks(newVset, pattern);
   }

   public int drawLine(Pattern pattern, List<BlockVector3> vectors, double radius, boolean filled) throws MaxChangedBlocksException {
      Set<BlockVector3> vset = new HashSet<>();

      for (int i = 0; vectors.size() != 0 && i < vectors.size() - 1; i++) {
         BlockVector3 pos1 = vectors.get(i);
         BlockVector3 pos2 = vectors.get(i + 1);
         int x1 = pos1.x();
         int y1 = pos1.y();
         int z1 = pos1.z();
         int x2 = pos2.x();
         int y2 = pos2.y();
         int z2 = pos2.z();
         int dx = Math.abs(x2 - x1);
         int dy = Math.abs(y2 - y1);
         int dz = Math.abs(z2 - z1);
         if (dx + dy + dz == 0) {
            vset.add(BlockVector3.at(x1, y1, z1));
         } else {
            int dMax = Math.max(Math.max(dx, dy), dz);
            if (dMax == dx) {
               for (int domstep = 0; domstep <= dx; domstep++) {
                  int tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                  int tipy = (int)Math.round(y1 + (double)domstep * dy / dx * (y2 - y1 > 0 ? 1 : -1));
                  int tipz = (int)Math.round(z1 + (double)domstep * dz / dx * (z2 - z1 > 0 ? 1 : -1));
                  vset.add(BlockVector3.at(tipx, tipy, tipz));
               }
            } else if (dMax == dy) {
               for (int domstep = 0; domstep <= dy; domstep++) {
                  int var27 = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
                  int var25 = (int)Math.round(x1 + (double)domstep * dx / dy * (x2 - x1 > 0 ? 1 : -1));
                  int var29 = (int)Math.round(z1 + (double)domstep * dz / dy * (z2 - z1 > 0 ? 1 : -1));
                  vset.add(BlockVector3.at(var25, var27, var29));
               }
            } else {
               for (int domstep = 0; domstep <= dz; domstep++) {
                  int var30 = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                  int var28 = (int)Math.round(y1 + (double)domstep * dy / dz * (y2 - y1 > 0 ? 1 : -1));
                  int var26 = (int)Math.round(x1 + (double)domstep * dx / dz * (x2 - x1 > 0 ? 1 : -1));
                  vset.add(BlockVector3.at(var26, var28, var30));
               }
            }
         }
      }

      vset = getBallooned(vset, radius);
      if (!filled) {
         vset = this.getHollowed(vset);
      }

      return this.changes = this.changes + this.setBlocks(vset, pattern);
   }

   public int drawSpline(
      Pattern pattern, List<BlockVector3> nodevectors, double tension, double bias, double continuity, double quality, double radius, boolean filled
   ) throws MaxChangedBlocksException {
      BlockVector3Set vset = LocalBlockVectorSet.wrapped();
      List<Node> nodes = new ArrayList<>(nodevectors.size());
      Interpolation interpol = new KochanekBartelsInterpolation();

      for (BlockVector3 nodevector : nodevectors) {
         Node n = new Node(nodevector.toVector3());
         n.setTension(tension);
         n.setBias(bias);
         n.setContinuity(continuity);
         nodes.add(n);
      }

      interpol.setNodes(nodes);
      double splinelength = interpol.arcLength(0.0, 1.0);
      double loop = 0.0;

      while (loop <= 1.0) {
         BlockVector3 tipv = interpol.getPosition(loop).toBlockPoint();
         if (radius == 0.0) {
            pattern.apply(this, tipv, tipv);
            this.changes++;
         } else {
            vset.add(tipv);
         }

         loop += 1.0 / splinelength / quality;
      }

      if (radius != 0.0) {
         Set<BlockVector3> newVset = getBallooned(vset, radius);
         if (!filled) {
            newVset = this.getHollowed(newVset);
         }

         return this.changes = this.changes + this.setBlocks(newVset, pattern);
      } else {
         return this.changes;
      }
   }

   private static Set<BlockVector3> getBallooned(Set<BlockVector3> vset, double radius) {
      if (radius < 1.0) {
         return vset;
      } else {
         BlockVector3Set returnset = LocalBlockVectorSet.wrapped();
         int ceilrad = (int)Math.ceil(radius);

         for (BlockVector3 v : vset) {
            int tipx = v.x();
            int tipy = v.y();
            int tipz = v.z();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++) {
               for (int loopy = tipy - ceilrad; loopy <= tipy + ceilrad; loopy++) {
                  for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++) {
                     if (MathMan.hypot(loopx - tipx, loopy - tipy, loopz - tipz) <= radius) {
                        returnset.add(loopx, loopy, loopz);
                     }
                  }
               }
            }
         }

         return returnset;
      }
   }

   public static Set<BlockVector3> getStretched(Set<BlockVector3> vset, double radius) {
      if (radius < 1.0) {
         return vset;
      } else {
         BlockVector3Set returnset = LocalBlockVectorSet.wrapped();
         int ceilrad = (int)Math.ceil(radius);

         for (BlockVector3 v : vset) {
            int tipx = v.x();
            int tipy = v.y();
            int tipz = v.z();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++) {
               for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++) {
                  if (MathMan.hypot(loopx - tipx, 0.0, loopz - tipz) <= radius) {
                     returnset.add(loopx, v.y(), loopz);
                  }
               }
            }
         }

         return returnset;
      }
   }

   public Set<BlockVector3> getOutline(Set<BlockVector3> vset) {
      BlockVector3Set returnset = LocalBlockVectorSet.wrapped();
      BlockVector3Set newset = LocalBlockVectorSet.wrapped();
      newset.addAll(vset);

      for (BlockVector3 v : newset) {
         int x = v.x();
         int y = v.y();
         int z = v.z();
         if (!newset.contains(x + 1, y, z) || !newset.contains(x - 1, y, z) || !newset.contains(x, y, z + 1) || !newset.contains(x, y, z - 1)) {
            returnset.add(v);
         }
      }

      return returnset;
   }

   public Set<BlockVector3> getHollowed(Set<BlockVector3> vset) {
      BlockVector3Set returnset = LocalBlockVectorSet.wrapped();
      BlockVector3Set newset = LocalBlockVectorSet.wrapped();
      newset.addAll(vset);

      for (BlockVector3 v : newset) {
         int x = v.x();
         int y = v.y();
         int z = v.z();
         if (!newset.contains(x + 1, y, z)
            || !newset.contains(x - 1, y, z)
            || !newset.contains(x, y + 1, z)
            || !newset.contains(x, y - 1, z)
            || !newset.contains(x, y, z + 1)
            || !newset.contains(x, y, z - 1)) {
            returnset.add(v);
         }
      }

      return returnset;
   }

   private void recurseHollow(Region region, BlockVector3 origin, Set<BlockVector3> outside, Mask mask) {
      BlockVector3Set queue = BlockVector3Set.getAppropriateVectorSet(region);
      queue.add(origin);
      MutableBlockVector3 mutable = new MutableBlockVector3();

      while (!queue.isEmpty()) {
         Iterator<BlockVector3> iter = queue.iterator();

         while (iter.hasNext()) {
            BlockVector3 current = iter.next();
            iter.remove();
            if (!mask.test(current) && outside.add(current) && region.contains(current)) {
               for (BlockVector3 recurseDirection : recurseDirections) {
                  queue.add(mutable.setComponents(current).add(recurseDirection));
               }
            }
         }
      }
   }

   public int makeBiomeShape(
      final Region region, final Vector3 zero, final Vector3 unit, final BiomeType biomeType, final String expressionString, final boolean hollow
   ) throws ExpressionException {
      return this.makeBiomeShape(region, zero, unit, biomeType, expressionString, hollow, WorldEdit.getInstance().getConfiguration().calculationTimeout);
   }

   public int makeBiomeShape(
      final Region region,
      final Vector3 zero,
      final Vector3 unit,
      final BiomeType biomeType,
      final String expressionString,
      final boolean hollow,
      final int timeout
   ) throws ExpressionException {
      final Expression expression = Expression.compile(expressionString, "x", "y", "z");
      expression.optimize();
      final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
      expression.setEnvironment(environment);
      final AtomicInteger timedOut = new AtomicInteger();
      ArbitraryBiomeShape shape = new ArbitraryBiomeShape(region) {
         @Override
         protected BiomeType getBiome(int x, int y, int z, BiomeType defaultBiomeType) {
            environment.setCurrentBlock(x, y, z);
            double scaledX = (x - zero.x()) / unit.x();
            double scaledY = (y - zero.y()) / unit.y();
            double scaledZ = (z - zero.z()) / unit.z();

            try {
               return expression.evaluate(new double[]{scaledX, scaledY, scaledZ}, timeout) <= 0.0 ? null : defaultBiomeType;
            } catch (ExpressionTimeoutException var12) {
               timedOut.getAndIncrement();
               return null;
            } catch (Exception var13) {
               EditSession.LOGGER.warn("Failed to create shape", var13);
               return null;
            }
         }
      };
      int changed = shape.generate(this, biomeType, hollow);
      if (timedOut.get() > 0) {
         throw new ExpressionTimeoutException(
            String.format("%d biomes changed. %d biomes took too long to evaluate (increase time with //timeout)", changed, timedOut.get())
         );
      } else {
         return changed;
      }
   }

   public int morph(BlockVector3 position, double brushSize, int minErodeFaces, int numErodeIterations, int minDilateFaces, int numDilateIterations) throws MaxChangedBlocksException {
      int ceilBrushSize = (int)Math.ceil(brushSize);
      int bufferSize = ceilBrushSize * 2 + 3;
      BlockState[][][] currentBuffer = new BlockState[bufferSize][bufferSize][bufferSize];
      BlockState[][][] nextBuffer = new BlockState[bufferSize][bufferSize][bufferSize];

      for (int x = 0; x < bufferSize; x++) {
         for (int y = 0; y < bufferSize; y++) {
            for (int z = 0; z < bufferSize; z++) {
               BlockState blockState = this.getBlock(position.add(x - ceilBrushSize - 1, y - ceilBrushSize - 1, z - ceilBrushSize - 1));
               currentBuffer[x][y][z] = blockState;
               nextBuffer[x][y][z] = blockState;
            }
         }
      }

      double brushSizeSq = brushSize * brushSize;
      Map<BlockState, Integer> blockStateFrequency = new HashMap<>();

      for (int i = 0; i < numErodeIterations; i++) {
         for (int x = 0; x <= ceilBrushSize * 2; x++) {
            int realX = x - ceilBrushSize;
            int xsqr = realX * realX;

            for (int y = 0; y <= ceilBrushSize * 2; y++) {
               int realY = y - ceilBrushSize;
               int ysqr = realY * realY;

               for (int z = 0; z <= ceilBrushSize * 2; z++) {
                  int realZ = z - ceilBrushSize;
                  int zsqr = realZ * realZ;
                  if (!(xsqr + ysqr + zsqr > brushSizeSq)) {
                     nextBuffer[x + 1][y + 1][z + 1] = currentBuffer[x + 1][y + 1][z + 1];
                     BlockState blockState = currentBuffer[x + 1][y + 1][z + 1];
                     if (!blockState.getBlockType().getMaterial().isLiquid() && !blockState.getBlockType().getMaterial().isAir()) {
                        blockStateFrequency.clear();
                        int totalFaces = 0;
                        int highestFreq = 0;
                        BlockState highestState = blockState;

                        for (BlockVector3 vec3 : recurseDirections) {
                           BlockState adj = currentBuffer[x + 1 + vec3.x()][y + 1 + vec3.y()][z + 1 + vec3.z()];
                           if (adj.getBlockType().getMaterial().isLiquid() || adj.getBlockType().getMaterial().isAir()) {
                              totalFaces++;
                              int newFreq = blockStateFrequency.getOrDefault(adj, 0) + 1;
                              blockStateFrequency.put(adj, newFreq);
                              if (newFreq > highestFreq) {
                                 highestFreq = newFreq;
                                 highestState = adj;
                              }
                           }
                        }

                        if (totalFaces >= minErodeFaces) {
                           nextBuffer[x + 1][y + 1][z + 1] = highestState;
                        }
                     }
                  }
               }
            }
         }

         BlockState[][][] tmp = currentBuffer;
         currentBuffer = nextBuffer;
         nextBuffer = tmp;
      }

      for (int i = 0; i < numDilateIterations; i++) {
         for (int x = 0; x <= ceilBrushSize * 2; x++) {
            int realX = x - ceilBrushSize;
            int xsqr = realX * realX;

            for (int y = 0; y <= ceilBrushSize * 2; y++) {
               int realY = y - ceilBrushSize;
               int ysqr = realY * realY;

               for (int zx = 0; zx <= ceilBrushSize * 2; zx++) {
                  int realZ = zx - ceilBrushSize;
                  int zsqr = realZ * realZ;
                  if (!(xsqr + ysqr + zsqr > brushSizeSq)) {
                     nextBuffer[x + 1][y + 1][zx + 1] = currentBuffer[x + 1][y + 1][zx + 1];
                     BlockState blockState = currentBuffer[x + 1][y + 1][zx + 1];
                     if (blockState.getBlockType().getMaterial().isLiquid() || blockState.getBlockType().getMaterial().isAir()) {
                        blockStateFrequency.clear();
                        int totalFaces = 0;
                        int highestFreq = 0;
                        BlockState highestState = blockState;

                        for (BlockVector3 vec3x : recurseDirections) {
                           BlockState adj = currentBuffer[x + 1 + vec3x.x()][y + 1 + vec3x.y()][zx + 1 + vec3x.z()];
                           if (!adj.getBlockType().getMaterial().isLiquid() && !adj.getBlockType().getMaterial().isAir()) {
                              totalFaces++;
                              int newFreq = blockStateFrequency.getOrDefault(adj, 0) + 1;
                              blockStateFrequency.put(adj, newFreq);
                              if (newFreq > highestFreq) {
                                 highestFreq = newFreq;
                                 highestState = adj;
                              }
                           }
                        }

                        if (totalFaces >= minDilateFaces) {
                           nextBuffer[x + 1][y + 1][zx + 1] = highestState;
                        }
                     }
                  }
               }
            }
         }

         BlockState[][][] tmp = currentBuffer;
         currentBuffer = nextBuffer;
         nextBuffer = tmp;
      }

      int changed = 0;

      for (int x = 0; x < bufferSize; x++) {
         for (int y = 0; y < bufferSize; y++) {
            for (int zxx = 0; zxx < bufferSize; zxx++) {
               if (this.setBlock(position.add(x - ceilBrushSize - 1, y - ceilBrushSize - 1, zxx - ceilBrushSize - 1), currentBuffer[x][y][zxx])) {
                  changed++;
               }
            }
         }
      }

      return changed;
   }

   public boolean regenerate(Region region) {
      return this.regenerate(region, this);
   }

   public boolean regenerate(Region region, EditSession session) {
      return session.regenerate(region, null, null);
   }

   private void setExistingBlocks(BlockVector3 pos1, BlockVector3 pos2) {
      for (int x = pos1.x(); x <= pos2.x(); x++) {
         for (int z = pos1.z(); z <= pos2.z(); z++) {
            for (int y = pos1.y(); y <= pos2.y(); y++) {
               this.setBlock(x, y, z, this.getFullBlock(x, y, z));
            }
         }
      }
   }

   public boolean regenerate(Region region, BiomeType biome, Long seed) {
      AbstractChangeSet fcs = (AbstractChangeSet)this.getChangeSet();
      this.setChangeSet(null);
      FaweRegionExtent fe = this.getRegionExtent();
      boolean cuboid = region instanceof CuboidRegion;
      if (fe != null && cuboid) {
         BlockVector3 max = region.getMaximumPoint();
         BlockVector3 min = region.getMinimumPoint();
         if (!fe.contains(max.x(), max.y(), max.z()) && !fe.contains(min.x(), min.y(), min.z())) {
            throw FaweCache.OUTSIDE_REGION;
         }
      }

      Set<BlockVector2> chunks = region.getChunks();
      MutableBlockVector3 mutable = new MutableBlockVector3();
      MutableBlockVector3 mutable2 = new MutableBlockVector3();
      MutableBlockVector2 mutable2D = new MutableBlockVector2();

      for (BlockVector2 chunk : chunks) {
         final int cx = chunk.x();
         final int cz = chunk.z();
         int bx = cx << 4;
         int bz = cz << 4;
         BlockVector3 cmin = BlockVector3.at(bx, 0, bz);
         BlockVector3 cmax = cmin.add(15, this.maxY, 15);
         boolean containsBot1 = fe == null || fe.contains(cmin.x(), cmin.y(), cmin.z());
         boolean containsBot2 = region.contains(cmin);
         boolean containsTop1 = fe == null || fe.contains(cmax.x(), cmax.y(), cmax.z());
         boolean containsTop2 = region.contains(cmax);
         if (!containsBot2 || !containsTop2 || containsBot1 || containsTop1) {
            boolean conNextX = chunks.contains(mutable2D.setComponents(cx + 1, cz));
            boolean conNextZ = chunks.contains(mutable2D.setComponents(cx, cz + 1));
            boolean containsAny = false;
            if (cuboid && containsBot1 && containsBot2 && containsTop1 && containsTop2 && conNextX && conNextZ) {
               containsAny = true;
               if (fcs != null) {
                  for (int x = 0; x < 16; x++) {
                     int xx = x + bx;

                     for (int z = 0; z < 16; z++) {
                        int zz = z + bz;

                        for (int y = this.minY; y < this.maxY + 1; y++) {
                           BaseBlock block = this.getFullBlock(mutable.setComponents(xx, y, zz));
                           fcs.add(mutable, block, BlockTypes.AIR.getDefaultState().toBaseBlock());
                        }
                     }
                  }
               }
            } else {
               if (!conNextX) {
                  this.setExistingBlocks(mutable.setComponents(bx + 16, 0, bz), mutable2.setComponents(bx + 31, this.maxY, bz + 15));
               }

               if (!conNextZ) {
                  this.setExistingBlocks(mutable.setComponents(bx, 0, bz + 16), mutable2.setComponents(bx + 15, this.maxY, bz + 31));
               }

               if (!chunks.contains(mutable2D.setComponents(cx + 1, cz + 1)) && !conNextX && !conNextZ) {
                  this.setExistingBlocks(mutable.setComponents(bx + 16, 0, bz + 16), mutable2.setComponents(bx + 31, this.maxY, bz + 31));
               }

               for (int x = 0; x < 16; x++) {
                  int xx = x + bx;
                  mutable.mutX(xx);

                  for (int z = 0; z < 16; z++) {
                     int zz = z + bz;
                     mutable.mutZ(zz);

                     for (int y = this.minY; y < this.maxY + 1; y++) {
                        mutable.mutY(y);
                        boolean contains = (fe == null || fe.contains(xx, y, zz)) && region.contains(mutable);
                        if (contains) {
                           containsAny = true;
                           if (fcs != null) {
                              BaseBlock block = this.getFullBlock(mutable);
                              fcs.add(mutable, block, BlockTypes.AIR.getDefaultState().toBaseBlock());
                           }
                        } else {
                           BaseBlock block = this.getFullBlock(mutable);

                           try {
                              this.setBlock(mutable, block);
                           } catch (MaxChangedBlocksException var34) {
                              throw new RuntimeException(var34);
                           }
                        }
                     }
                  }
               }
            }

            if (containsAny) {
               this.changes++;
               TaskManager.taskManager().sync(new RunnableVal<Object>() {
                  @Override
                  public void run(Object value) {
                     EditSession.this.regenerateChunk(cx, cz, biome, seed);
                  }
               });
            }
         }
      }

      if (this.changes != 0) {
         this.flushQueue();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.world.getEntities();
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.world.getEntities(region);
   }

   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      try {
         return this.getExtent().createEntity(location, entity);
      } catch (WorldEditException var4) {
         throw new RuntimeException("Unexpected exception", var4);
      }
   }

   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      try {
         return this.getExtent().createEntity(location, entity, uuid);
      } catch (WorldEditException var5) {
         throw new RuntimeException("Unexpected exception", var5);
      }
   }

   @Override
   public void removeEntity(int x, int y, int z, UUID uuid) {
      try {
         this.getExtent().removeEntity(x, y, z, uuid);
      } catch (WorldEditException var6) {
         throw new RuntimeException("Unexpected exception", var6);
      }
   }

   @Override
   public void generate(Region region, GenBase gen) throws WorldEditException {
      for (BlockVector2 chunkPos : region.getChunks()) {
         gen.generate(chunkPos, new SingleRegionExtent(this, null, region));
      }
   }

   @Override
   public void addSchems(Region region, Mask mask, List<ClipboardHolder> clipboards, int rarity, boolean rotate) throws WorldEditException {
      this.spawnResource(region, new SchemGen(mask, this, clipboards, rotate, region), rarity, 1);
   }

   @Override
   public void addOre(Region region, Mask mask, Pattern material, int size, int frequency, int rarity, int minY, int maxY) throws WorldEditException {
      this.spawnResource(region, new OreGen(this, mask, material, size, minY, maxY), rarity, frequency);
   }

   @Override
   public Clipboard lazyCopy(Region region) {
      WorldCopyClipboard faweClipboard = WorldCopyClipboard.of(this, region);
      faweClipboard.setOrigin(region.getMinimumPoint());
      return faweClipboard;
   }

   public int makeBlob(BlockVector3 position, Pattern pattern, double size, double frequency, double amplitude, Vector3 radius, double sphericity) {
      double seedX = ThreadLocalRandom.current().nextDouble();
      double seedY = ThreadLocalRandom.current().nextDouble();
      double seedZ = ThreadLocalRandom.current().nextDouble();
      int px = position.x();
      int py = position.y();
      int pz = position.z();
      double distort = frequency / size;
      double modX = 1.0 / radius.x();
      double modY = 1.0 / radius.y();
      double modZ = 1.0 / radius.z();
      int r = (int)size;
      int radiusSqr = (int)(size * size);
      int sizeInt = (int)size * 2;
      if (sphericity == 1.0) {
         for (int x = -sizeInt; x <= sizeInt; x++) {
            double nx = seedX + x * distort;
            double d1 = x * x * modX;
            int xx = px + x;

            for (int y = -sizeInt; y <= sizeInt; y++) {
               double d2 = d1 + y * y * modY;
               double ny = seedY + y * distort;
               int yy = py + y;

               for (int z = -sizeInt; z <= sizeInt; z++) {
                  double nz = seedZ + z * distort;
                  double distance = d2 + z * z * modZ;
                  int zz = pz + z;
                  double noise = amplitude * SimplexNoise.noise(nx, ny, nz);
                  if (distance + distance * noise < radiusSqr) {
                     this.setBlock(xx, yy, zz, pattern);
                  }
               }
            }
         }
      } else {
         AffineTransform transform = new AffineTransform()
            .rotateX(ThreadLocalRandom.current().nextInt(360))
            .rotateY(ThreadLocalRandom.current().nextInt(360))
            .rotateZ(ThreadLocalRandom.current().nextInt(360));
         double manScaleX = 1.25 + seedX * 0.5;
         double manScaleY = 1.25 + seedY * 0.5;
         double manScaleZ = 1.25 + seedZ * 0.5;
         MutableVector3 mutable = new MutableVector3();
         double roughness = 1.0 - sphericity;

         for (int xr = -sizeInt; xr <= sizeInt; xr++) {
            int xx = px + xr;

            for (int yr = -sizeInt; yr <= sizeInt; yr++) {
               int yy = py + yr;

               for (int zr = -sizeInt; zr <= sizeInt; zr++) {
                  int zz = pz + zr;
                  mutable.setComponents(xr, yr, zr);
                  Vector3 pt = transform.apply(mutable);
                  int x = MathMan.roundInt(pt.x());
                  int y = MathMan.roundInt(pt.y());
                  int zx = MathMan.roundInt(pt.z());
                  double xScaled = Math.abs(x) * modX;
                  double yScaled = Math.abs(y) * modY;
                  double zScaled = Math.abs(zx) * modZ;
                  double manDist = xScaled + yScaled + zScaled;
                  double distSqr = x * x * modX + zx * zx * modZ + y * y * modY;
                  double distance = Math.sqrt(distSqr) * sphericity
                     + MathMan.max(manDist, xScaled * manScaleX, yScaled * manScaleY, zScaled * manScaleZ) * roughness;
                  double noise = amplitude * SimplexNoise.noise(seedX + x * distort, seedZ + zx * distort, seedZ + zx * distort);
                  if (distance + distance * noise < r) {
                     this.setBlock(xx, yy, zz, pattern);
                  }
               }
            }
         }
      }

      return this.changes;
   }

   public int generateFeature(ConfiguredFeatureType feature, BlockVector3 position) {
      feature.place(this, position);
      return this.changes;
   }

   public int generateStructure(StructureType structure, BlockVector3 position) {
      structure.place(this, position);
      return this.changes;
   }

   @Deprecated
   public static enum ReorderMode {
      MULTI_STAGE("multi"),
      FAST("fast"),
      NONE("none");

      private final String displayName;

      private ReorderMode(String displayName) {
         this.displayName = displayName;
      }

      public String getDisplayName() {
         return this.displayName;
      }
   }

   public static enum Stage {
      BEFORE_HISTORY,
      BEFORE_REORDER,
      BEFORE_CHANGE;
   }
}
