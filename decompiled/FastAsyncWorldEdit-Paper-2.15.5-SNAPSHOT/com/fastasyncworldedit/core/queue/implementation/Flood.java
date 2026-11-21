package com.fastasyncworldedit.core.queue.implementation;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Flood {
   private final int maxBranch;
   private final int maxDepth;
   private final Direction[] directions;
   private final Long2ObjectLinkedOpenHashMap<long[][]> chunkVisits;
   private final Long2ObjectLinkedOpenHashMap<int[][]> chunkQueues;
   private int[] queue;
   private long[] visit;
   private final int[][] queues;
   private final long[][] visits;
   private int chunkX;
   private int chunkYLayer;
   private int chunkZ;
   private final ConcurrentLinkedQueue<int[]> queuePool = new ConcurrentLinkedQueue<>();
   private final int minSectionPosition;
   private final int maxSectionPosition;
   private final int sectionCount;

   public Flood(int maxBranch, int maxDepth, Direction[] directions, int minSectionPosition, int maxSectionPosition) {
      this.maxBranch = maxBranch;
      this.maxDepth = maxDepth;
      this.directions = directions;
      this.minSectionPosition = minSectionPosition;
      this.maxSectionPosition = maxSectionPosition;
      this.sectionCount = maxSectionPosition - minSectionPosition + 1;
      this.queues = new int[27][];
      this.visits = new long[27][];
      this.chunkVisits = new Long2ObjectLinkedOpenHashMap();
      this.chunkQueues = new Long2ObjectLinkedOpenHashMap();
   }

   public synchronized void run(World world) {
      QueueHandler queueHandler = Fawe.instance().getQueueHandler();
      IQueueExtent<IQueueChunk> fq = queueHandler.getQueue(world);

      while (!this.chunkQueues.isEmpty()) {
         long firstKey = this.chunkQueues.firstLongKey();
         int x = MathMan.unpairIntX(firstKey);
         int z = MathMan.unpairIntY(firstKey);
         int[][] var8 = (int[][])this.chunkQueues.get(firstKey);
      }
   }

   private void init(int chunkX, int chunkYLayer, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkYLayer = chunkYLayer;
      this.chunkZ = chunkZ;
   }

   public void start(int x, int y, int z) {
      this.push(x, y, z, 0);
   }

   private void push(int x, int y, int z, int depth) {
      int chunkX = x >> 4;
      int chunkZ = z >> 4;
      long pair = MathMan.pairInt(chunkX, chunkZ);
      int layer = (y >> 4) - this.minSectionPosition;
      int[] section = this.getOrCreateQueue(pair, layer);
      int val = (x & 15) + ((z & 15) << 4) + ((y & 15) << 8) + (depth << 12);
      this.push(section, val);
   }

   private int[] getOrCreateQueue(long pair, int layer) {
      int[][] arrs = (int[][])this.chunkQueues.get(pair);
      if (arrs == null) {
         this.chunkQueues.put(pair, arrs = new int[this.sectionCount][]);
      }

      int[] section = arrs[layer];
      if (section == null) {
         arrs[layer] = section = this.newQueue();
      }

      return section;
   }

   private int[] newQueue() {
      int[] arr = this.queuePool.poll();
      if (arr != null) {
         arr[0] = 2;
         arr[1] = 2;
         return arr;
      } else {
         return new int[4096];
      }
   }

   public int poll() {
      int index = this.queue[0];
      if (index == this.queue[1]) {
         return -1;
      } else {
         this.queue[0] = index + 1;
         return this.queue[index];
      }
   }

   private void push(int[] queue, int val) {
      int indexStart = queue[0];
      int indexEnd = queue[1];
      this.push(indexStart, indexEnd, queue, val);
   }

   private void push(int indexStart, int indexEnd, int[] queue, int val) {
      if (indexStart > 2) {
         queue[0] = --indexStart;
         queue[indexStart] = val;
      } else {
         queue[indexEnd] = val;
         queue[0] = ++indexEnd;
      }
   }

   public Direction[] getDirections() {
      return this.directions;
   }

   public int getMaxBranch() {
      return this.maxBranch;
   }

   public int getMaxDepth() {
      return this.maxDepth;
   }

   public void apply(int x, int y, int z, int depth) {
      int i = 0;

      for (int j = 0; i < this.directions.length && j < this.maxBranch; i++) {
         Direction dir = this.directions[i];
         int ty = y + dir.getBlockY();
         int tx = x + dir.getBlockX();
         int tz = z + dir.getBlockZ();
         int or = tx | ty | tz;
         int index;
         long[] visit;
         int[] queue;
         if (or <= 15 && or >= 0) {
            int nextX = tx >> 4;
            int nextY = ty >> 4;
            int nextZ = tz >> 4;
            int sectionIndex = nextX + nextZ * 3 + nextZ * 9 + 13;
            visit = this.visits[sectionIndex];
            queue = this.queues[sectionIndex];
            if (visit == null || queue == null) {
               long pair = MathMan.pairInt(this.chunkX + nextX, this.chunkZ + nextZ);
               int layer = this.chunkYLayer + nextY;
               if (layer < this.minSectionPosition || layer > this.maxSectionPosition) {
                  continue;
               }

               this.queues[sectionIndex] = queue = this.getOrCreateQueue(pair, layer);
            }

            index = (tx & 15) + ((tz & 15) << 4) + ((ty & 15) << 8);
         } else {
            visit = this.visit;
            queue = this.queue;
            index = tx + (tz << 4) + (ty << 8);
         }

         if (!this.getAndSet(visit, index)) {
            j++;
            this.push(queue, index + (depth << 12));
         }
      }
   }

   public void set(long[] bits, int i) {
      bits[i >> 6] = bits[i >> 6] | 1L << (i & 63);
   }

   public final boolean getAndSet(long[] bits, int i) {
      int index = i >> 6;
      long offset = 1L << (i & 63);
      long val = bits[index];
      if ((val & offset) != 0L) {
         return true;
      } else {
         bits[index] |= offset;
         return false;
      }
   }

   public boolean get(long[] bits, int i) {
      return (bits[i >> 6] & 1L << (i & 63)) != 0L;
   }
}
