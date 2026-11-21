package com.sk89q.worldedit.function.visitor;

import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.FlatRegion;

public class LayerVisitor implements Operation {
   private final FlatRegion flatRegion;
   private final LayerFunction function;
   private final SingleThreadQueueExtent singleQueue;
   private Mask2D mask = Masks.alwaysTrue2D();
   private final int minY;
   private final int maxY;

   public LayerVisitor(FlatRegion flatRegion, int minY, int maxY, LayerFunction function) {
      this(flatRegion, minY, maxY, function, null);
   }

   public LayerVisitor(FlatRegion flatRegion, int minY, int maxY, LayerFunction function, Extent extent) {
      Preconditions.checkNotNull(flatRegion);
      Preconditions.checkArgument(minY <= maxY, "minY <= maxY required");
      Preconditions.checkNotNull(function);
      this.flatRegion = flatRegion;
      this.minY = minY;
      this.maxY = maxY;
      this.function = function;
      if (extent != null) {
         ExtentTraverser<ParallelQueueExtent> queueTraverser = new ExtentTraverser<>(extent).find(ParallelQueueExtent.class);
         this.singleQueue = queueTraverser != null ? (SingleThreadQueueExtent)queueTraverser.get().getExtent() : null;
      } else {
         this.singleQueue = null;
      }
   }

   public Mask2D getMask() {
      return this.mask;
   }

   public void setMask(Mask2D mask) {
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      if (this.singleQueue != null) {
         this.singleQueue.preload(this.flatRegion);
      }

      for (BlockVector2 column : this.flatRegion.asFlatRegion()) {
         if (this.mask.test(column) && !this.function.isGround(column.toBlockVector3(this.maxY + 1))) {
            boolean found = false;
            int groundY = 0;

            for (int y = this.maxY; y >= this.minY; y--) {
               BlockVector3 test = column.toBlockVector3(y);
               if (!found && this.function.isGround(test)) {
                  found = true;
                  groundY = y;
               }

               if (found && !this.function.apply(test, groundY - y)) {
                  break;
               }
            }
         }
      }

      return null;
   }

   @Override
   public void cancel() {
   }
}
