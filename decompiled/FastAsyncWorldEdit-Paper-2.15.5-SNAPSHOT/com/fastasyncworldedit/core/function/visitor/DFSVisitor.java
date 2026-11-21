package com.fastasyncworldedit.core.function.visitor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.math.IntTriple;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DFSVisitor implements Operation {
   private final RegionFunction function;
   private final List<BlockVector3> directions = new ArrayList<>();
   private final Map<DFSVisitor.Node, AtomicInteger> visited;
   private final ArrayDeque<DFSVisitor.NodePair> queue;
   private final HashSet<DFSVisitor.Node> hashQueue;
   private final int maxDepth;
   private final int maxBranch;
   private int affected = 0;

   public DFSVisitor(final RegionFunction function) {
      this(function, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public DFSVisitor(final RegionFunction function, int maxDepth, int maxBranching) {
      this.queue = new ArrayDeque<>();
      this.hashQueue = new LinkedHashSet<>();
      this.visited = new LinkedHashMap<>();
      this.function = function;
      this.directions.add(BlockVector3.UNIT_MINUS_Y);
      this.directions.add(BlockVector3.UNIT_Y);
      this.directions.add(BlockVector3.UNIT_MINUS_X);
      this.directions.add(BlockVector3.UNIT_X);
      this.directions.add(BlockVector3.UNIT_MINUS_Z);
      this.directions.add(BlockVector3.UNIT_Z);
      this.maxDepth = maxDepth;
      this.maxBranch = maxBranching;
   }

   public abstract boolean isVisitable(BlockVector3 from, BlockVector3 to);

   public List<BlockVector3> getDirections() {
      return this.directions;
   }

   private IntTriple[] getIntDirections() {
      IntTriple[] array = new IntTriple[this.directions.size()];

      for (int i = 0; i < array.length; i++) {
         BlockVector3 dir = this.directions.get(i);
         array[i] = new IntTriple(dir.x(), dir.y(), dir.z());
      }

      return array;
   }

   public void visit(final BlockVector3 pos) {
      DFSVisitor.Node node = new DFSVisitor.Node(pos.x(), pos.y(), pos.z());
      if (!this.hashQueue.contains(node)) {
         this.isVisitable(pos, pos);
         this.queue.addFirst(new DFSVisitor.NodePair(null, node, 0));
         this.hashQueue.add(node);
      }
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      MutableBlockVector3 mutable = new MutableBlockVector3();
      MutableBlockVector3 mutable2 = new MutableBlockVector3();
      IntTriple[] dirs = this.getIntDirections();

      while (!this.queue.isEmpty()) {
         DFSVisitor.NodePair current = this.queue.poll();
         DFSVisitor.Node from = current.to;
         this.hashQueue.remove(from);
         if (!this.visited.containsKey(from)) {
            mutable.mutX(from.getX());
            mutable.mutY(from.getY());
            mutable.mutZ(from.getZ());
            this.function.apply(mutable);
            int countAdd = 0;
            int countAttempt = 0;

            for (IntTriple direction : dirs) {
               mutable2.mutX(from.getX() + direction.x());
               mutable2.mutY(from.getY() + direction.y());
               mutable2.mutZ(from.getZ() + direction.z());
               if (this.isVisitable(mutable, mutable2)) {
                  DFSVisitor.Node adjacent = new DFSVisitor.Node(mutable2.x(), mutable2.y(), mutable2.z());
                  if (!adjacent.equals(current.from)) {
                     AtomicInteger adjacentCount = this.visited.get(adjacent);
                     if (adjacentCount == null) {
                        if (countAdd++ < this.maxBranch) {
                           if (!this.hashQueue.contains(adjacent)) {
                              if (current.depth == this.maxDepth) {
                                 countAttempt++;
                              } else {
                                 this.hashQueue.add(adjacent);
                                 this.queue.addFirst(new DFSVisitor.NodePair(from, adjacent, current.depth + 1));
                              }
                           } else {
                              countAttempt++;
                           }
                        } else {
                           countAttempt++;
                        }
                     } else if (adjacentCount.decrementAndGet() == 0) {
                        this.visited.remove(adjacent);
                     } else if (this.hashQueue.contains(adjacent)) {
                        countAttempt++;
                     }
                  }
               }
            }

            if (countAttempt > 0) {
               this.visited.put(from, new AtomicInteger(countAttempt));
            }

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
      return Lists.newArrayList(new Component[]{Caption.of("fawe.worldedit.visitor.visitor.block", TextComponent.of(this.getAffected()))});
   }

   public int getAffected() {
      return this.affected;
   }

   public static final class Node {
      private int x;
      private int y;
      private int z;

      public Node(int x, int y, int z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      private void set(int x, int y, int z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      private void set(DFSVisitor.Node node) {
         this.x = node.x;
         this.y = node.y;
         this.z = node.z;
      }

      @Override
      public int hashCode() {
         return this.x ^ this.z << 12 ^ this.y << 24;
      }

      private int getX() {
         return this.x;
      }

      private int getY() {
         return this.y;
      }

      private int getZ() {
         return this.z;
      }

      @Override
      public String toString() {
         return this.x + "," + this.y + "," + this.z;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else {
            DFSVisitor.Node other = (DFSVisitor.Node)obj;
            return other.x == this.x && other.z == this.z && other.y == this.y;
         }
      }
   }

   public record NodePair(DFSVisitor.Node from, DFSVisitor.Node to, int depth) {
   }
}
