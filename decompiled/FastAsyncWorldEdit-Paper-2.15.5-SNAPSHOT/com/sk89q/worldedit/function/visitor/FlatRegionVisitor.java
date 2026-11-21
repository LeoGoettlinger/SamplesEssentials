package com.sk89q.worldedit.function.visitor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

public class FlatRegionVisitor implements Operation {
   private final SingleThreadQueueExtent singleQueue;
   private final FlatRegion flatRegion;
   private final FlatRegionFunction function;
   private int affected = 0;

   public FlatRegionVisitor(FlatRegion flatRegion, FlatRegionFunction function) {
      this(flatRegion, function, null);
   }

   public FlatRegionVisitor(FlatRegion flatRegion, FlatRegionFunction function, Extent extent) {
      Preconditions.checkNotNull(flatRegion);
      Preconditions.checkNotNull(function);
      this.function = function;
      this.flatRegion = flatRegion;
      if (extent != null) {
         ExtentTraverser<ParallelQueueExtent> queueTraverser = new ExtentTraverser<>(extent).find(ParallelQueueExtent.class);
         this.singleQueue = queueTraverser != null ? (SingleThreadQueueExtent)queueTraverser.get().getExtent() : null;
      } else {
         this.singleQueue = null;
      }
   }

   public int getAffected() {
      return this.affected;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      if (this.singleQueue != null) {
         this.singleQueue.preload(this.flatRegion);
      }

      for (BlockVector2 pt : this.flatRegion.asFlatRegion()) {
         if (this.function.apply(pt)) {
            this.affected++;
         }
      }

      return null;
   }

   @Override
   public void cancel() {
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return ImmutableList.of(Caption.of("worldedit.operation.affected.column", TextComponent.of(this.getAffected())));
   }
}
