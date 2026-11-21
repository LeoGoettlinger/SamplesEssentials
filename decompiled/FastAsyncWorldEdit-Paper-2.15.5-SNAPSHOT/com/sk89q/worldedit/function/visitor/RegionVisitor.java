package com.sk89q.worldedit.function.visitor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Iterator;

@Deprecated
public class RegionVisitor implements Operation {
   public final Iterable<? extends BlockVector3> iterable;
   private final SingleThreadQueueExtent singleQueue;
   private final Region region;
   private final RegionFunction function;
   private int affected = 0;

   @Deprecated
   public RegionVisitor(Region region, RegionFunction function) {
      this(region, function, null);
   }

   public RegionVisitor(Iterable<BlockVector3> iterable, RegionFunction function, Extent extent) {
      this.region = iterable instanceof Region ? (Region)iterable : null;
      this.iterable = iterable;
      this.function = function;
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
      if (this.singleQueue != null && Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT > 1) {
         Iterator<? extends BlockVector3> trailIter = this.iterable.iterator();
         Iterator<? extends BlockVector3> leadIter = this.iterable.iterator();
         int lastTrailChunkX = Integer.MIN_VALUE;
         int lastTrailChunkZ = Integer.MIN_VALUE;
         int lastLeadChunkX = Integer.MIN_VALUE;
         int lastLeadChunkZ = Integer.MIN_VALUE;
         int loadingTarget = Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT;

         while (trailIter.hasNext()) {
            BlockVector3 pt = trailIter.next();
            this.apply(pt);
            int cx = pt.x() >> 4;
            int cz = pt.z() >> 4;
            if (cx != lastTrailChunkX || cz != lastTrailChunkZ) {
               lastTrailChunkX = cx;
               lastTrailChunkZ = cz;
               int amount;
               if (lastLeadChunkX == Integer.MIN_VALUE) {
                  lastLeadChunkX = cx;
                  lastLeadChunkZ = cz;
                  amount = loadingTarget;
               } else {
                  amount = 1;
               }

               try {
                  int count = 0;

                  label67:
                  while (count < amount) {
                     BlockVector3 v = leadIter.next();
                     int vcx = v.x() >> 4;
                     int vcz = v.z() >> 4;
                     if (vcx != lastLeadChunkX || vcz != lastLeadChunkZ) {
                        lastLeadChunkX = vcx;
                        lastLeadChunkZ = vcz;
                        this.singleQueue.addChunkLoad(vcx, vcz);
                        count++;
                     }

                     for (int i = 0; i < 16; i++) {
                        if (!leadIter.hasNext()) {
                           break label67;
                        }

                        leadIter.next();
                     }
                  }
               } catch (FaweException var18) {
                  throw new RuntimeException(var18);
               } catch (Throwable var19) {
               }
            }

            for (int i = 0; i < 16; i++) {
               if (!trailIter.hasNext()) {
                  return null;
               }

               this.apply(trailIter.next());
            }
         }
      } else {
         for (BlockVector3 pt : this.region) {
            this.apply(pt);
         }
      }

      return null;
   }

   private void apply(BlockVector3 pt) throws WorldEditException {
      if (this.function.apply(pt)) {
         this.affected++;
      }
   }

   @Override
   public void cancel() {
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return ImmutableList.of(Caption.of("worldedit.operation.affected.block", TextComponent.of(this.getAffected())));
   }
}
