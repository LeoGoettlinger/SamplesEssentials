package com.sk89q.worldedit.function.visitor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class BreadthFirstSearch implements Operation {
   public static final BlockVector3[] DEFAULT_DIRECTIONS = new BlockVector3[6];
   public static final BlockVector3[] DIAGONAL_DIRECTIONS;
   private final RegionFunction function;
   private final SingleThreadQueueExtent singleQueue;
   private BlockVectorSet queue = new BlockVectorSet();
   private BlockVectorSet visited = new BlockVectorSet();
   private BlockVector3[] directions;
   protected final int minY;
   protected final int maxY;
   private int affected = 0;
   private int currentDepth = 0;
   private final int maxDepth;
   private int maxBranch = Integer.MAX_VALUE;

   public BreadthFirstSearch(RegionFunction function) {
      this(
         function,
         Integer.MAX_VALUE,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY(),
         null
      );
   }

   public BreadthFirstSearch(RegionFunction function, int depth, int minY, int maxY) {
      this(function, depth, minY, maxY, null);
   }

   public BreadthFirstSearch(RegionFunction function, int maxDepth, int minY, int maxY, Extent extent) {
      Preconditions.checkNotNull(function);
      this.function = function;
      this.directions = DEFAULT_DIRECTIONS;
      this.maxDepth = maxDepth;
      this.minY = minY;
      this.maxY = maxY;
      if (extent != null) {
         ExtentTraverser<ParallelQueueExtent> queueTraverser = new ExtentTraverser<>(extent).find(ParallelQueueExtent.class);
         this.singleQueue = queueTraverser != null ? (SingleThreadQueueExtent)queueTraverser.get().getExtent() : null;
      } else {
         this.singleQueue = null;
      }
   }

   public void setDirections(BlockVector3... directions) {
      this.directions = directions;
   }

   public void setDirections(Collection<BlockVector3> directions) {
      this.setDirections(directions.toArray(new BlockVector3[0]));
   }

   public Collection<BlockVector3> getDirections() {
      return Arrays.asList(this.directions);
   }

   public void addAxes() {
      HashSet<BlockVector3> set = Sets.newHashSet(this.directions);
      set.add(BlockVector3.UNIT_MINUS_Y);
      set.add(BlockVector3.UNIT_Y);
      set.add(BlockVector3.UNIT_MINUS_X);
      set.add(BlockVector3.UNIT_X);
      set.add(BlockVector3.UNIT_MINUS_Z);
      set.add(BlockVector3.UNIT_Z);
      this.setDirections(set);
   }

   public void addDiagonal() {
      HashSet<BlockVector3> set = Sets.newHashSet(this.directions);
      set.add(Direction.NORTHEAST.toBlockVector());
      set.add(Direction.SOUTHEAST.toBlockVector());
      set.add(Direction.SOUTHWEST.toBlockVector());
      set.add(Direction.NORTHWEST.toBlockVector());
      this.setDirections(set);
   }

   public void visit(BlockVector3 position) {
      if (!this.visited.contains(position)) {
         this.isVisitable(position, position);
         this.queue.add(position);
         this.visited.add(position);
      }
   }

   private void visit(BlockVector3 from, BlockVector3 to) {
      if (!this.visited.contains(to)) {
         this.visited.add(to);
         if (this.isVisitable(from, to)) {
            this.queue.add(to);
         }
      }
   }

   public void setVisited(BlockVectorSet set) {
      this.visited = set;
   }

   public BlockVectorSet getVisited() {
      return this.visited;
   }

   public boolean isVisited(BlockVector3 pos) {
      return this.visited.contains(pos);
   }

   public void setMaxBranch(int maxBranch) {
      this.maxBranch = maxBranch;
   }

   protected abstract boolean isVisitable(BlockVector3 from, BlockVector3 to);

   public int getAffected() {
      return this.affected;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      MutableBlockVector3 mutable = new MutableBlockVector3();
      BlockVector3[] dirs = this.directions;
      BlockVectorSet tempQueue = new BlockVectorSet();
      BlockVectorSet chunkLoadSet = new BlockVectorSet();

      for (this.currentDepth = 0; !this.queue.isEmpty() && this.currentDepth <= this.maxDepth; this.currentDepth++) {
         int loadCount = 0;
         if (this.singleQueue != null && Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT > 1) {
            int cx = Integer.MIN_VALUE;
            int cz = Integer.MIN_VALUE;

            label94:
            for (BlockVector3 from : this.queue) {
               for (BlockVector3 direction : dirs) {
                  if (loadCount > Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT) {
                     break label94;
                  }

                  int x = from.x() + direction.x();
                  int z = from.z() + direction.x();
                  if (cx != (cx = x >> 4) || cz != (cz = z >> 4)) {
                     int y = from.y() + direction.y();
                     if (y >= this.singleQueue.getMinY() && y <= this.singleQueue.getMaxY() && !this.visited.contains(x, y, z)) {
                        loadCount++;
                        chunkLoadSet.add(cx, 0, cz);
                     }
                  }
               }
            }

            for (BlockVector3 chunk : chunkLoadSet) {
               this.singleQueue.addChunkLoad(chunk.x(), chunk.z());
            }
         }

         for (BlockVector3 from : this.queue) {
            if (this.function.apply(from)) {
               this.affected++;
            }

            int i = 0;

            for (int j = 0; i < dirs.length && j < this.maxBranch; i++) {
               BlockVector3 direction = dirs[i];
               int y = from.y() + direction.y();
               if (y >= this.minY && y <= this.maxY) {
                  int x = from.x() + direction.x();
                  int z = from.z() + direction.z();
                  if (!this.visited.contains(x, y, z) && this.isVisitable(from, mutable.setComponents(x, y, z))) {
                     j++;
                     this.visited.add(x, y, z);
                     tempQueue.add(x, y, z);
                  }
               }
            }
         }

         if (this.currentDepth == this.maxDepth) {
            break;
         }

         BlockVectorSet tmp = this.queue;
         this.queue = tempQueue;
         tmp.clear();
         tempQueue = tmp;
      }

      return null;
   }

   public int getDepth() {
      return this.currentDepth;
   }

   @Override
   public void cancel() {
      this.queue.clear();
      this.visited.clear();
      this.affected = 0;
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return ImmutableList.of(Caption.of("worldedit.operation.affected.block", TextComponent.of(this.getAffected())));
   }

   static {
      DEFAULT_DIRECTIONS[0] = BlockVector3.UNIT_MINUS_Y;
      DEFAULT_DIRECTIONS[1] = BlockVector3.UNIT_Y;
      DEFAULT_DIRECTIONS[2] = BlockVector3.UNIT_MINUS_X;
      DEFAULT_DIRECTIONS[3] = BlockVector3.UNIT_X;
      DEFAULT_DIRECTIONS[4] = BlockVector3.UNIT_MINUS_Z;
      DEFAULT_DIRECTIONS[5] = BlockVector3.UNIT_Z;
      List<BlockVector3> list = new ArrayList<>();

      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
               if (x != 0 || y != 0 || z != 0) {
                  BlockVector3 pos = BlockVector3.at(x, y, z);
                  if (!list.contains(pos)) {
                     list.add(pos);
                  }
               }
            }
         }
      }

      list.sort((o1, o2) -> (int)Math.signum((float)(o1.lengthSq() - o2.lengthSq())));
      DIAGONAL_DIRECTIONS = list.toArray(new BlockVector3[0]);
   }
}
