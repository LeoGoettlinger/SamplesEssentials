package com.sk89q.worldedit.function.operation;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.BlockTranslateExtent;
import com.fastasyncworldedit.core.extent.OncePerChunkExtent;
import com.fastasyncworldedit.core.extent.PositionTransformExtent;
import com.fastasyncworldedit.core.extent.clipboard.WorldCopyClipboard;
import com.fastasyncworldedit.core.extent.processor.ExtentBatchProcessorHolder;
import com.fastasyncworldedit.core.function.RegionMaskTestFunction;
import com.fastasyncworldedit.core.function.block.BiomeCopy;
import com.fastasyncworldedit.core.function.block.CombinedBlockCopy;
import com.fastasyncworldedit.core.function.block.SimpleBlockCopy;
import com.fastasyncworldedit.core.function.visitor.IntersectRegionFunction;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.ProcessorTraverser;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.CombinedRegionFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.entity.ExtentEntityCopy;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;

public class ForwardExtentCopy implements Operation {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Extent source;
   private final Extent destination;
   private final Region region;
   private final BlockVector3 from;
   private final BlockVector3 to;
   private int repetitions = 1;
   private Mask sourceMask = Masks.alwaysTrue();
   private boolean removingEntities;
   private boolean copyingEntities = true;
   private boolean copyingBiomes;
   private RegionFunction sourceFunction = null;
   private Transform transform = new Identity();
   private Transform currentTransform = null;
   private RegionFunction filterFunction;
   private RegionVisitor lastBiomeVisitor;
   private EntityVisitor lastEntityVisitor;
   private int affectedBlocks;
   private int affectedBiomeCols;
   private int affectedEntities;

   public ForwardExtentCopy(Extent source, Region region, Extent destination, BlockVector3 to) {
      this(source, region, region.getMinimumPoint(), destination, to);
   }

   public ForwardExtentCopy(Extent source, Region region, BlockVector3 from, Extent destination, BlockVector3 to) {
      Preconditions.checkNotNull(source);
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(destination);
      Preconditions.checkNotNull(to);
      this.source = source;
      this.destination = destination;
      this.region = region;
      this.from = from;
      this.to = to;
   }

   public Transform getTransform() {
      return this.transform;
   }

   public void setTransform(Transform transform) {
      Preconditions.checkNotNull(transform);
      this.transform = transform;
   }

   public Mask getSourceMask() {
      return this.sourceMask;
   }

   public void setSourceMask(Mask sourceMask) {
      Preconditions.checkNotNull(sourceMask);
      this.sourceMask = sourceMask;
   }

   public void setFilterFunction(RegionFunction filterFunction) {
      this.filterFunction = filterFunction;
   }

   public RegionFunction getSourceFunction() {
      return this.sourceFunction;
   }

   public void setSourceFunction(RegionFunction function) {
      this.sourceFunction = function;
   }

   public int getRepetitions() {
      return this.repetitions;
   }

   public void setRepetitions(int repetitions) {
      Preconditions.checkArgument(repetitions >= 0, "number of repetitions must be non-negative");
      this.repetitions = repetitions;
   }

   public boolean isCopyingEntities() {
      return this.copyingEntities;
   }

   public void setCopyingEntities(boolean copyingEntities) {
      this.copyingEntities = copyingEntities;
   }

   public boolean isRemovingEntities() {
      return this.removingEntities;
   }

   public void setRemovingEntities(boolean removingEntities) {
      this.removingEntities = removingEntities;
   }

   public boolean isCopyingBiomes() {
      return this.copyingBiomes;
   }

   public void setCopyingBiomes(boolean copyingBiomes) {
      if (copyingBiomes && !(this.region instanceof FlatRegion)) {
         throw new UnsupportedOperationException("Can't copy biomes from region that doesn't implement FlatRegion");
      } else {
         this.copyingBiomes = copyingBiomes;
      }
   }

   public int getAffected() {
      return this.affectedBlocks + this.affectedBiomeCols + this.affectedEntities;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      if (this.currentTransform == null) {
         this.currentTransform = this.transform;
      }

      if (this.lastBiomeVisitor != null) {
         this.affectedBiomeCols = this.affectedBiomeCols + this.lastBiomeVisitor.getAffected();
         this.lastBiomeVisitor = null;
      }

      if (this.lastEntityVisitor != null) {
         this.affectedEntities = this.affectedEntities + this.lastEntityVisitor.getAffected();
         this.lastEntityVisitor = null;
      }

      Extent finalDest = this.destination;
      BlockVector3 translation = this.to.subtract(this.from);
      if (!translation.equals(BlockVector3.ZERO)) {
         finalDest = new BlockTranslateExtent(finalDest, translation.x(), translation.y(), translation.z());
      }

      RegionVisitor blockCopy = null;
      PositionTransformExtent transExt = null;
      if (!this.currentTransform.isIdentity()) {
         if (this.currentTransform instanceof AffineTransform && !((AffineTransform)this.currentTransform).isOffAxis()) {
            transExt = new PositionTransformExtent(finalDest, this.currentTransform);
            transExt.setOrigin(this.from);
            finalDest = transExt;
         } else {
            transExt = new PositionTransformExtent(this.source, this.currentTransform.inverse());
            transExt.setOrigin(this.from);
            RegionFunction copy = new SimpleBlockCopy(transExt, finalDest);
            if (this.filterFunction != null) {
               copy = new IntersectRegionFunction(this.filterFunction, copy);
            }

            if (this.sourceFunction != null) {
               copy = CombinedRegionFunction.combine(copy, this.sourceFunction);
            }

            if (this.sourceMask != Masks.alwaysTrue()) {
               new MaskTraverser(this.sourceMask).reset(transExt);
               copy = new RegionMaskingFilter(this.sourceMask, copy);
            }

            if (this.copyingBiomes && (this.source.isWorld() || this.region instanceof FlatRegion)) {
               copy = CombinedRegionFunction.combine(copy, new BiomeCopy(this.source, finalDest));
            }

            blockCopy = new BackwardsExtentBlockCopy(this.region, this.from, this.transform, copy);
         }
      }

      if (blockCopy == null) {
         RegionFunction maskFunc = null;
         RegionFunction copyx;
         if (this.sourceFunction == null) {
            copyx = new SimpleBlockCopy(this.source, finalDest);
         } else {
            BlockVector3 disAbs = translation.abs();
            BlockVector3 size = this.region.getMaximumPoint().subtract(this.region.getMinimumPoint()).add(1, 1, 1);
            boolean overlap = disAbs.x() < size.x() && disAbs.y() < size.y() && disAbs.z() < size.z();
            RegionFunction copySrcFunc = this.sourceFunction;
            if (overlap && translation.length() != 0.0) {
               int x = translation.x();
               int y = translation.y();
               int z = translation.z();
               maskFunc = position -> {
                  BlockVector3 bv = BlockVector3.at(position.x() + x, position.y() + y, position.z() + z);
                  return this.region.contains(bv) ? this.sourceFunction.apply(bv) : false;
               };
               copySrcFunc = position -> {
                  BlockVector3 bv = BlockVector3.at(position.x() - x, position.y() - y, position.z() - z);
                  return !this.region.contains(bv) ? this.sourceFunction.apply(position) : false;
               };
            }

            copyx = new CombinedBlockCopy(this.source, finalDest, copySrcFunc);
         }

         if (this.filterFunction != null) {
            copyx = new IntersectRegionFunction(this.filterFunction, copyx);
         }

         if (this.sourceMask != Masks.alwaysTrue()) {
            if (maskFunc != null) {
               copyx = new RegionMaskTestFunction(this.sourceMask, copyx, maskFunc);
            } else {
               copyx = new RegionMaskingFilter(this.sourceMask, copyx);
            }
         }

         if (this.copyingBiomes && (this.source.isWorld() || this.region instanceof FlatRegion)) {
            copyx = CombinedRegionFunction.combine(copyx, new BiomeCopy(this.source, finalDest));
         }

         ExtentTraverser<ParallelQueueExtent> queueTraverser = new ExtentTraverser<>(finalDest).find(ParallelQueueExtent.class);
         Extent preloader = (Extent)(queueTraverser != null ? queueTraverser.get() : this.source);
         blockCopy = new RegionVisitor(this.region, copyx, preloader);
      }

      Collection<Entity> entities = (Collection<Entity>)(this.copyingEntities ? getEntities(this.source, this.region) : Collections.emptySet());

      for (int i = 0; i < this.repetitions; i++) {
         Operations.completeBlindly(blockCopy);
         if (!entities.isEmpty()) {
            ExtentEntityCopy entityCopy = new ExtentEntityCopy(this.source, this.from.toVector3(), this.destination, this.to.toVector3(), this.currentTransform);
            entityCopy.setRemoving(this.removingEntities);
            List<? extends Entity> entities2 = Lists.newArrayList(this.source.getEntities(this.region));
            entities2.removeIf(entity -> {
               EntityProperties properties = entity.getFacet(EntityProperties.class);
               return properties != null && !properties.isPasteable();
            });
            EntityVisitor entityVisitor = new EntityVisitor(entities.iterator(), entityCopy);
            Operations.completeBlindly(entityVisitor);
            this.affectedEntities = this.affectedEntities + entityVisitor.getAffected();
         }

         if (transExt != null) {
            this.currentTransform = this.currentTransform.combine(this.transform);
            transExt.setTransform(this.currentTransform);
         }
      }

      this.affectedBlocks = this.affectedBlocks + blockCopy.getAffected();
      if (this.copyingBiomes) {
         this.affectedBiomeCols = this.affectedBiomeCols
            + (this.source.fullySupports3DBiomes() ? this.getAffected() >> 2 : this.region.getWidth() * this.region.getLength());
      }

      return null;
   }

   public static Collection<Entity> getEntities(Extent source, Region region) {
      Extent extent = source;
      if (source instanceof WorldCopyClipboard clip) {
         extent = clip.getExtent();
      }

      IQueueExtent<IQueueChunk> queue = null;
      if (Settings.settings().EXPERIMENTAL.IMPROVED_ENTITY_EDITS && new ExtentTraverser<>(source).findAndGet(Clipboard.class) == null) {
         ParallelQueueExtent parallel = new ExtentTraverser<>(extent).findAndGet(ParallelQueueExtent.class);
         if (parallel != null) {
            queue = parallel.getExtent();
         } else {
            queue = new ExtentTraverser<>(extent).findAndGet(SingleThreadQueueExtent.class);
         }

         if (queue == null) {
            LOGGER.warn(
               "Could not find IQueueExtent from `" + source.getClass().getName() + "` instance for entity retrieval, OncePerChunkExtent will not work."
            );
            if (Settings.settings().ENABLED_COMPONENTS.DEBUG) {
               ExtentTraverser.printNestedExtents(source);
            }
         }
      }

      if (queue == null) {
         Set<Entity> entities = new HashSet<>(region != null ? source.getEntities(region) : source.getEntities());
         entities.removeIf(entity -> {
            EntityProperties properties = entity.getFacet(EntityProperties.class);
            return properties != null && !properties.isPasteable();
         });
         return entities;
      } else {
         LinkedBlockingQueue<Entity> entities = new LinkedBlockingQueue<>();
         Consumer<IChunkGet> task = get -> {
            if (region != null && !(region instanceof CuboidRegion cuboid && cuboid.chunkContainedBy(get.getX(), get.getZ(), get.getMinY(), get.getMaxY()))) {
               get.getFullEntities().forEach(e -> {
                  if (region.contains(e.getLocation().toBlockPoint())) {
                     entities.add(e);
                  }
               });
            } else {
               entities.addAll(get.getFullEntities());
            }
         };
         Extent ext = extent instanceof AbstractDelegateExtent ex ? ex.getExtent() : extent;
         ExtentBatchProcessorHolder batchExtent = new ExtentTraverser<>(extent).findAndGet(ExtentBatchProcessorHolder.class);
         OncePerChunkExtent oncePer = new ExtentTraverser<>(extent).findAndGet(OncePerChunkExtent.class);
         if (batchExtent != null && oncePer == null) {
            oncePer = new ProcessorTraverser<>(batchExtent).find(OncePerChunkExtent.class);
         }

         if (oncePer != null) {
            oncePer.reset();
            oncePer.setTask(task);
         } else {
            oncePer = new OncePerChunkExtent(ext, queue, task);
            new ExtentTraverser<>(extent).setNext(oncePer);
         }

         return entities;
      }
   }

   @Override
   public void cancel() {
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return ImmutableList.of(
         Caption.of("worldedit.operation.affected.block", TextComponent.of(this.affectedBlocks)),
         Caption.of("worldedit.operation.affected.biome", TextComponent.of(this.affectedBiomeCols)),
         Caption.of("worldedit.operation.affected.entity", TextComponent.of(this.affectedEntities))
      );
   }
}
