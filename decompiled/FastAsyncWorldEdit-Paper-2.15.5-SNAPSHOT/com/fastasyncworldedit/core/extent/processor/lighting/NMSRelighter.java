package com.fastasyncworldedit.core.extent.processor.lighting;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkHolder;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;

public class NMSRelighter implements Relighter {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final int DISPATCH_SIZE = 64;
   private static final DirectionalProperty stairDirection = (DirectionalProperty)BlockTypes.SANDSTONE_STAIRS.<Direction>getProperty("facing");
   private static final EnumProperty stairHalf = (EnumProperty)BlockTypes.SANDSTONE_STAIRS.<String>getProperty("half");
   private static final EnumProperty stairShape = (EnumProperty)BlockTypes.SANDSTONE_STAIRS.<String>getProperty("shape");
   private static final EnumProperty slabHalf = (EnumProperty)BlockTypes.SANDSTONE_SLAB.<String>getProperty("type");
   private final IQueueExtent<?> queue;
   private final Map<Long, NMSRelighter.RelightSkyEntry> skyToRelight;
   private final Object present = new Object();
   private final Map<Long, Integer> chunksToSend;
   private final ConcurrentLinkedQueue<NMSRelighter.RelightSkyEntry> extendSkyToRelight = new ConcurrentLinkedQueue<>();
   private final Map<Long, long[][][]> lightQueue;
   private final AtomicBoolean lightLock = new AtomicBoolean(false);
   private final ConcurrentHashMap<Long, long[][][]> concurrentLightQueue;
   private final RelightMode relightMode;
   private final int maxY;
   private final int minY;
   private final int yLongPerCol;
   private final ReentrantLock lightingLock;
   private final AtomicBoolean finished = new AtomicBoolean(false);
   private boolean removeFirst;

   public NMSRelighter(IQueueExtent<?> queue) {
      this(queue, null);
   }

   public NMSRelighter(IQueueExtent<?> queue, RelightMode relightMode) {
      this.queue = queue;
      this.skyToRelight = new Long2ObjectOpenHashMap(12);
      this.lightQueue = new Long2ObjectOpenHashMap(12);
      this.chunksToSend = new Long2ObjectOpenHashMap(12);
      this.concurrentLightQueue = new ConcurrentHashMap<>(12);
      this.maxY = queue.getMaxY();
      this.minY = queue.getMinY();
      this.yLongPerCol = this.maxY - this.minY + 1 >> 6;
      this.relightMode = relightMode != null ? relightMode : RelightMode.valueOf(Settings.settings().LIGHTING.MODE);
      this.lightingLock = new ReentrantLock();
   }

   @Override
   public boolean isEmpty() {
      return this.skyToRelight.isEmpty() && this.lightQueue.isEmpty() && this.extendSkyToRelight.isEmpty() && this.concurrentLightQueue.isEmpty();
   }

   @Override
   public ReentrantLock getLock() {
      return this.lightingLock;
   }

   @Override
   public boolean isFinished() {
      return this.finished.get();
   }

   @Override
   public synchronized void removeAndRelight(boolean sky) {
      this.removeFirst = true;
      this.fixLightingSafe(sky);
      this.removeFirst = false;
   }

   private void set(int x, int y, int z, long[][][] map) {
      long[][] m1 = map[z];
      if (m1 == null) {
         m1 = map[z] = new long[16][];
      }

      long[] m2 = m1[x];
      if (m2 == null) {
         m2 = m1[x] = new long[this.yLongPerCol];
      }

      y -= this.minY;
      m2[y >> 6] = m2[y >> 6] | 1L << (y & 63);
   }

   @Override
   public void addLightUpdate(int x, int y, int z) {
      long index = MathMan.pairInt(x >> 4, z >> 4);
      if (this.lightLock.compareAndSet(false, true)) {
         synchronized (this.lightQueue) {
            try {
               long[][][] currentMap = this.lightQueue.get(index);
               if (currentMap == null) {
                  currentMap = new long[16][][];
                  this.lightQueue.put(index, currentMap);
               }

               this.set(x & 15, y, z & 15, currentMap);
               this.lightQueue.putAll(this.concurrentLightQueue);
               this.concurrentLightQueue.clear();
            } finally {
               this.lightLock.set(false);
            }
         }
      } else {
         long[][][] currentMap = this.concurrentLightQueue.get(index);
         if (currentMap == null) {
            currentMap = new long[16][][];
            this.concurrentLightQueue.put(index, currentMap);
         }

         this.set(x & 15, y, z & 15, currentMap);
      }
   }

   @Override
   public synchronized void clear() {
      this.extendSkyToRelight.clear();
      this.skyToRelight.clear();
      this.chunksToSend.clear();
      this.lightQueue.clear();
   }

   @Override
   public boolean addChunk(int cx, int cz, byte[] fix, int bitmask) {
      NMSRelighter.RelightSkyEntry toPut = new NMSRelighter.RelightSkyEntry(cx, cz, fix, bitmask, this.minY, this.maxY);
      this.extendSkyToRelight.add(toPut);
      return true;
   }

   private synchronized Map<Long, NMSRelighter.RelightSkyEntry> getSkyMap() {
      NMSRelighter.RelightSkyEntry entry;
      while ((entry = this.extendSkyToRelight.poll()) != null) {
         long pair = MathMan.pairInt(entry.x, entry.z);
         NMSRelighter.RelightSkyEntry existing = this.skyToRelight.put(pair, entry);
         if (existing != null) {
            entry.bitmask = entry.bitmask | existing.bitmask;
            if (entry.fix != null) {
               for (int i = 0; i < entry.fix.length; i++) {
                  entry.fix[i] = (byte)(entry.fix[i] & existing.fix[i]);
               }
            }
         }
      }

      return this.skyToRelight;
   }

   @Override
   public synchronized void removeLighting() {
      Iterator<Entry<Long, NMSRelighter.RelightSkyEntry>> iter = this.getSkyMap().entrySet().iterator();

      while (iter.hasNext()) {
         Entry<Long, NMSRelighter.RelightSkyEntry> entry = iter.next();
         NMSRelighter.RelightSkyEntry chunk = entry.getValue();
         long pair = entry.getKey();
         this.chunksToSend.compute(pair, (k, existing) -> chunk.bitmask | (existing != null ? existing : 0));
         ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x, chunk.z);
         if (!iChunk.isInit()) {
            iChunk.init(this.queue, chunk.x, chunk.z);
         }

         for (int i = this.minY >> 4; i <= this.maxY >> 4; i++) {
            iChunk.removeSectionLighting(i, true);
         }

         iter.remove();
      }
   }

   public void updateBlockLight(Map<Long, long[][][]> map) {
      int size = map.size();
      if (size != 0) {
         Queue<BlockVector3> lightPropagationQueue = new LinkedList<>();
         Queue<NMSRelighter.LightRemovalNode> lightRemovalQueue = new LinkedList<>();
         Map<BlockVector3, Object> visited = new HashMap<>(32);
         Map<BlockVector3, Object> removalVisited = new HashMap<>(32);
         BlockTypes.STONE.getMaterial();
         Iterator<Entry<Long, long[][][]>> iter = map.entrySet().iterator();

         while (iter.hasNext() && size-- > 0) {
            Entry<Long, long[][][]> entry = iter.next();
            long index = entry.getKey();
            long[][][] blocks = entry.getValue();
            int chunkX = MathMan.unpairIntX(index);
            int chunkZ = MathMan.unpairIntY(index);
            int bx = chunkX << 4;
            int bz = chunkZ << 4;
            ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunkX, chunkZ);
            if (!iChunk.isInit()) {
               iChunk.init(this.queue, chunkX, chunkZ);
            }

            for (int lz = 0; lz < blocks.length; lz++) {
               long[][] m1 = blocks[lz];
               if (m1 != null) {
                  for (int lx = 0; lx < m1.length; lx++) {
                     long[] m2 = m1[lx];
                     if (m2 != null) {
                        for (int i = 0; i < m2.length; i++) {
                           int yStart = (i << 6) + this.minY;
                           long value = m2[i];
                           if (value != 0L) {
                              for (int j = 0; j < 64; j++) {
                                 if ((value >> j & 1L) == 1L) {
                                    int x = lx + bx;
                                    int y = yStart + j;
                                    int z = lz + bz;
                                    int oldLevel = iChunk.getEmittedLight(lx, y, lz);
                                    int newLevel = iChunk.getBrightness(lx, y, lz);
                                    if (oldLevel != newLevel) {
                                       iChunk.setBlockLight(lx, y, lz, newLevel);
                                       BlockVector3 node = BlockVector3.at(x, y, z);
                                       if (newLevel < oldLevel) {
                                          removalVisited.put(node, this.present);
                                          lightRemovalQueue.add(new NMSRelighter.LightRemovalNode(node, oldLevel));
                                       } else {
                                          visited.put(node, this.present);
                                          lightPropagationQueue.add(node);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            iter.remove();
         }

         while (!lightRemovalQueue.isEmpty()) {
            NMSRelighter.LightRemovalNode val = lightRemovalQueue.poll();
            BlockVector3 node = val.pos();
            int lightLevel = val.previous();
            this.computeRemoveBlockLight(node.x() - 1, node.y(), node.z(), lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
            this.computeRemoveBlockLight(node.x() + 1, node.y(), node.z(), lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
            if (node.y() > this.minY) {
               this.computeRemoveBlockLight(node.x(), node.y() - 1, node.z(), lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
            }

            if (node.y() < this.maxY) {
               this.computeRemoveBlockLight(node.x(), node.y() + 1, node.z(), lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
            }

            this.computeRemoveBlockLight(node.x(), node.y(), node.z() - 1, lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
            this.computeRemoveBlockLight(node.x(), node.y(), node.z() + 1, lightLevel, lightRemovalQueue, lightPropagationQueue, removalVisited, visited);
         }

         while (!lightPropagationQueue.isEmpty()) {
            BlockVector3 nodex = lightPropagationQueue.poll();
            ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(nodex.x() >> 4, nodex.z() >> 4);
            if (!iChunk.isInit()) {
               iChunk.init(this.queue, nodex.x() >> 4, nodex.z() >> 4);
            }

            int lightLevelx = iChunk.getEmittedLight(nodex.x() & 15, nodex.y(), nodex.z() & 15);
            BlockState state = this.queue.getBlock(nodex.x(), nodex.y(), nodex.z());
            String id = state.getBlockType().id().toLowerCase(Locale.ROOT);
            if (lightLevelx > 1) {
               if (id.contains("slab")) {
                  boolean top = state.getState(slabHalf).equalsIgnoreCase("top");
                  this.computeSlab(nodex.x(), nodex.y(), nodex.z(), lightLevelx, lightPropagationQueue, visited, top);
               } else if (id.contains("stair")) {
                  boolean top = state.getState(stairHalf).equalsIgnoreCase("top");
                  Direction direction = this.getStairDir(state);
                  String shape = this.getStairShape(state);
                  this.computeStair(nodex.x(), nodex.y(), nodex.z(), lightLevelx, lightPropagationQueue, visited, top, direction, shape);
               } else {
                  this.computeNormal(nodex.x(), nodex.y(), nodex.z(), lightLevelx, lightPropagationQueue, visited);
               }
            }
         }
      }
   }

   private void computeStair(
      int x, int y, int z, int currentLight, Queue<BlockVector3> queue, Map<BlockVector3, Object> visited, boolean top, Direction direction, String shape
   ) {
      if (direction == Direction.WEST
         || (direction != Direction.NORTH || !shape.equals("inner_right"))
            && (direction != Direction.SOUTH || !shape.equals("inner_left"))
            && (direction != Direction.EAST || shape.startsWith("outer"))) {
         BlockState state = this.queue.getBlock(x + 1, y, z);
         if (this.checkStairEast(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
            if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
               this.computeSpreadBlockLight(x + 1, y, z, currentLight, queue, visited);
            } else {
               label515: {
                  Direction otherDir = this.getStairDir(state);
                  String otherShape = this.getStairShape(state);
                  boolean b1 = otherDir == Direction.NORTH && !otherShape.equals("outer_right")
                     || otherDir == Direction.EAST && otherShape.equals("inner_left");
                  boolean b2 = otherDir == Direction.SOUTH && !otherShape.equals("outer_left")
                     || otherDir == Direction.EAST && otherShape.equals("inner_right");
                  switch (direction) {
                     case EAST:
                        if (shape.equals("outer_right") && b1 || shape.equals("outer_left") && b2) {
                           break label515;
                        }
                        break;
                     case WEST:
                        if (!shape.equals("straight") && !shape.startsWith("outer") && (shape.equals("inner_left") && b1 || shape.equals("inner_right") && b2)) {
                           break label515;
                        }
                        break;
                     case SOUTH:
                        if (b1 || otherDir == Direction.SOUTH && otherShape.equals("inner_right")) {
                           break label515;
                        }
                        break;
                     case NORTH:
                        if (b2 || otherDir == Direction.NORTH && otherShape.equals("inner_left")) {
                           break label515;
                        }
                  }

                  this.computeSpreadBlockLight(x + 1, y, z, currentLight, queue, visited);
               }
            }
         }
      }

      if (direction == Direction.EAST
         || (direction != Direction.SOUTH || !shape.equals("inner_right"))
            && (direction != Direction.NORTH || !shape.equals("inner_left"))
            && (direction != Direction.WEST || shape.startsWith("outer"))) {
         BlockState state = this.queue.getBlock(x - 1, y, z);
         if (this.checkStairWest(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
            if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
               this.computeSpreadBlockLight(x - 1, y, z, currentLight, queue, visited);
            } else {
               label516: {
                  Direction otherDir = this.getStairDir(state);
                  String otherShape = this.getStairShape(state);
                  boolean b1 = otherDir == Direction.SOUTH && !otherShape.equals("outer_right")
                     || otherDir == Direction.WEST && otherShape.equals("inner_left");
                  boolean b2 = otherDir == Direction.NORTH && !otherShape.equals("outer_left")
                     || otherDir == Direction.WEST && otherShape.equals("inner_right");
                  switch (direction) {
                     case EAST:
                        if (!shape.equals("straight") && !shape.startsWith("outer") && (shape.equals("inner_left") && b1 || shape.equals("inner_right") && b2)) {
                           break label516;
                        }
                        break;
                     case WEST:
                        if (shape.equals("outer_right") && b1 || shape.equals("outer_left") && b2) {
                           break label516;
                        }
                        break;
                     case SOUTH:
                        if (b2 || otherDir == Direction.SOUTH && otherShape.equals("inner_left")) {
                           break label516;
                        }
                        break;
                     case NORTH:
                        if (b1 || otherDir == Direction.NORTH && otherShape.equals("inner_right")) {
                           break label516;
                        }
                  }

                  this.computeSpreadBlockLight(x - 1, y, z, currentLight, queue, visited);
               }
            }
         }
      }

      if (direction == Direction.NORTH
         || (direction != Direction.WEST || !shape.equals("inner_right"))
            && (direction != Direction.EAST || !shape.equals("inner_left"))
            && (direction != Direction.SOUTH || shape.startsWith("outer"))) {
         BlockState state = this.queue.getBlock(x, y, z + 1);
         if (this.checkStairSouth(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
            if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
               this.computeSpreadBlockLight(x, y, z + 1, currentLight, queue, visited);
            } else {
               label517: {
                  Direction otherDir = this.getStairDir(state);
                  String otherShape = this.getStairShape(state);
                  boolean b1 = otherDir == Direction.EAST && !otherShape.equals("outer_right")
                     || otherDir == Direction.SOUTH && otherShape.equals("inner_left");
                  boolean b2 = otherDir == Direction.WEST && !otherShape.equals("outer_left")
                     || otherDir == Direction.SOUTH && otherShape.equals("inner_right");
                  switch (direction) {
                     case EAST:
                        if (b2 || otherDir == Direction.EAST && otherShape.equals("inner_left")) {
                           break label517;
                        }
                        break;
                     case WEST:
                        if (b1 || otherDir == Direction.WEST && otherShape.equals("inner_right")) {
                           break label517;
                        }
                        break;
                     case SOUTH:
                        if (shape.equals("outer_right") && b1 || shape.equals("outer_left") && b2) {
                           break label517;
                        }
                        break;
                     case NORTH:
                        if (!shape.equals("straight") && !shape.startsWith("outer") && (shape.equals("inner_left") && b1 || shape.equals("inner_right") && b2)) {
                           break label517;
                        }
                  }

                  this.computeSpreadBlockLight(x, y, z + 1, currentLight, queue, visited);
               }
            }
         }
      }

      if (direction == Direction.SOUTH
         || (direction != Direction.EAST || !shape.equals("inner_right"))
            && (direction != Direction.WEST || !shape.equals("inner_left"))
            && (direction != Direction.NORTH || shape.startsWith("outer"))) {
         BlockState state = this.queue.getBlock(x, y, z - 1);
         if (this.checkStairNorth(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
            if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
               this.computeSpreadBlockLight(x, y, z - 1, currentLight, queue, visited);
            } else {
               label518: {
                  Direction otherDir = this.getStairDir(state);
                  String otherShape = this.getStairShape(state);
                  boolean b1 = otherDir == Direction.WEST && !otherShape.equals("outer_right")
                     || otherDir == Direction.NORTH && otherShape.equals("inner_left");
                  boolean b2 = otherDir == Direction.EAST && !otherShape.equals("outer_left")
                     || otherDir == Direction.NORTH && otherShape.equals("inner_right");
                  switch (direction) {
                     case EAST:
                        if (b1 || otherDir == Direction.EAST && otherShape.equals("inner_right")) {
                           break label518;
                        }
                        break;
                     case WEST:
                        if (b2 || otherDir == Direction.WEST && otherShape.equals("inner_left")) {
                           break label518;
                        }
                        break;
                     case SOUTH:
                        if (!shape.equals("straight") && !shape.startsWith("outer") && (shape.equals("inner_left") && b1 || shape.equals("inner_right") && b2)) {
                           break label518;
                        }
                        break;
                     case NORTH:
                        if (shape.equals("outer_right") && b1 || shape.equals("outer_left") && b2) {
                           break label518;
                        }
                  }

                  this.computeSpreadBlockLight(x, y, z - 1, currentLight, queue, visited);
               }
            }
         }
      }

      this.computeUpDown(x, y, z, currentLight, queue, visited, top);
   }

   private void computeSlab(int x, int y, int z, int currentLight, Queue<BlockVector3> queue, Map<BlockVector3, Object> visited, boolean top) {
      BlockState state = this.queue.getBlock(x + 1, y, z);
      if (this.checkStairEast(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
         this.computeSpreadBlockLight(x + 1, y, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x - 1, y, z);
      if (this.checkStairWest(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
         this.computeSpreadBlockLight(x - 1, y, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y, z + 1);
      if (this.checkStairSouth(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
         this.computeSpreadBlockLight(x, y, z + 1, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y, z - 1);
      if (this.checkStairNorth(state) && this.isStairOrTrueTop(state, top) && this.isSlabOrTrueValue(state, top ? "top" : "bottom")) {
         this.computeSpreadBlockLight(x, y, z - 1, currentLight, queue, visited);
      }

      this.computeUpDown(x, y, z, currentLight, queue, visited, top);
   }

   private void computeUpDown(int x, int y, int z, int currentLight, Queue<BlockVector3> queue, Map<BlockVector3, Object> visited, boolean top) {
      BlockState state = this.queue.getBlock(x, y - 1, z);
      if (y > 0 && top && this.isSlabOrTrueValue(state, "bottom") && this.isStairOrTrueTop(state, false)) {
         this.computeSpreadBlockLight(x, y - 1, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y + 1, z);
      if (y < this.maxY && !top && this.isSlabOrTrueValue(state, "top") && this.isStairOrTrueTop(state, true)) {
         this.computeSpreadBlockLight(x, y + 1, z, currentLight, queue, visited);
      }
   }

   private void computeNormal(int x, int y, int z, int currentLight, Queue<BlockVector3> queue, Map<BlockVector3, Object> visited) {
      BlockState state = this.queue.getBlock(x + 1, y, z);
      if (this.checkStairEast(state) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "bottom"))) {
         this.computeSpreadBlockLight(x + 1, y, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x - 1, y, z);
      if (this.checkStairWest(state) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "bottom"))) {
         this.computeSpreadBlockLight(x - 1, y, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y, z + 1);
      if (this.checkStairSouth(state) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "bottom"))) {
         this.computeSpreadBlockLight(x, y, z + 1, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y, z - 1);
      if (this.checkStairNorth(state) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "bottom"))) {
         this.computeSpreadBlockLight(x, y, z - 1, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y - 1, z);
      if (y > 0 && this.isSlabOrTrueValue(state, "bottom") && this.isStairOrTrueTop(state, false)) {
         this.computeSpreadBlockLight(x, y - 1, z, currentLight, queue, visited);
      }

      state = this.queue.getBlock(x, y + 1, z);
      if (y < this.maxY && this.isSlabOrTrueValue(state, "top") && this.isStairOrTrueTop(state, false)) {
         this.computeSpreadBlockLight(x, y + 1, z, currentLight, queue, visited);
      }
   }

   private boolean checkStairNorth(BlockState state) {
      if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
         return true;
      } else {
         Direction direction = this.getStairDir(state);
         String shape = this.getStairShape(state);
         if (shape.startsWith("outer") || direction == Direction.NORTH) {
            return true;
         } else if (direction == Direction.SOUTH) {
            return false;
         } else {
            return direction == Direction.WEST ? !shape.equals("inner_left") : direction != Direction.EAST || !shape.equals("inner_right");
         }
      }
   }

   private boolean checkStairSouth(BlockState state) {
      if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
         return true;
      } else {
         Direction direction = this.getStairDir(state);
         String shape = this.getStairShape(state);
         if (shape.startsWith("outer") || direction == Direction.SOUTH) {
            return true;
         } else if (direction == Direction.NORTH) {
            return false;
         } else {
            return direction == Direction.EAST ? !shape.equals("inner_left") : direction != Direction.WEST || !shape.equals("inner_right");
         }
      }
   }

   private boolean checkStairEast(BlockState state) {
      if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
         return true;
      } else {
         Direction direction = this.getStairDir(state);
         String shape = this.getStairShape(state);
         if (shape.startsWith("outer") || direction == Direction.EAST) {
            return true;
         } else if (direction == Direction.WEST) {
            return false;
         } else {
            return direction == Direction.NORTH ? !shape.equals("inner_left") : direction != Direction.SOUTH || !shape.equals("inner_right");
         }
      }
   }

   private boolean checkStairWest(BlockState state) {
      if (!state.getBlockType().id().toLowerCase(Locale.ROOT).contains("stair")) {
         return true;
      } else {
         Direction direction = this.getStairDir(state);
         String shape = this.getStairShape(state);
         if (shape.startsWith("outer") || direction == Direction.WEST) {
            return true;
         } else if (direction == Direction.EAST) {
            return false;
         } else {
            return direction == Direction.SOUTH ? !shape.equals("inner_left") : direction != Direction.NORTH || !shape.equals("inner_right");
         }
      }
   }

   private Direction getStairDir(BlockState state) {
      return state.getState(stairDirection);
   }

   private String getStairShape(BlockState state) {
      return state.getState(stairShape).toLowerCase(Locale.ROOT);
   }

   private boolean isStairOrTrueTop(BlockState state, boolean top) {
      return !state.getBlockType().id().contains("stair") || state.getState(stairHalf).equals("top") == top;
   }

   private boolean isSlabOrTrueValue(BlockState state, String value) {
      return !state.getBlockType().id().contains("slab") || state.getState(slabHalf).equals(value);
   }

   private void computeRemoveBlockLight(
      int x,
      int y,
      int z,
      int currentLight,
      Queue<NMSRelighter.LightRemovalNode> queue,
      Queue<BlockVector3> spreadQueue,
      Map<BlockVector3, Object> visited,
      Map<BlockVector3, Object> spreadVisited
   ) {
      ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(x >> 4, z >> 4);
      if (!iChunk.isInit()) {
         iChunk.init(this.queue, x >> 4, z >> 4);
      }

      int current = iChunk.getEmittedLight(x & 15, y, z & 15);
      if (current != 0 && current < currentLight) {
         iChunk.setBlockLight(x, y, z, 0);
         if (current > 1) {
            visited.computeIfAbsent(BlockVector3.at(x, y, z), k -> {
               queue.add(new NMSRelighter.LightRemovalNode(k, current));
               return this.present;
            });
         }
      } else if (current >= currentLight) {
         spreadVisited.computeIfAbsent(BlockVector3.at(x, y, z), k -> {
            spreadQueue.add(k);
            return this.present;
         });
      }
   }

   private void computeSpreadBlockLight(int x, int y, int z, int currentLight, Queue<BlockVector3> queue, Map<BlockVector3, Object> visited) {
      BlockMaterial material = this.queue.getBlock(x, y, z).getMaterial();
      boolean solidNeedsLight = (!material.isSolid() || !material.isFullCube()) && material.getLightOpacity() > 0 && material.getLightValue() == 0;
      int correctedLight = !solidNeedsLight ? currentLight - Math.max(1, material.getLightOpacity()) : currentLight - 1;
      if (currentLight > 0) {
         ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(x >> 4, z >> 4);
         if (!iChunk.isInit()) {
            iChunk.init(this.queue, x >> 4, z >> 4);
         }

         int current = iChunk.getEmittedLight(x & 15, y, z & 15);
         if (currentLight > current) {
            iChunk.setBlockLight(x & 15, y, z & 15, currentLight);
            visited.computeIfAbsent(BlockVector3.at(x, y, z), k -> {
               if (correctedLight > 1) {
                  queue.add(k);
               }

               return this.present;
            });
         }
      }
   }

   @Override
   public void fixLightingSafe(boolean sky) {
      if (!this.isEmpty()) {
         if (sky) {
            this.fixSkyLighting();
         } else {
            synchronized (this) {
               Map<Long, NMSRelighter.RelightSkyEntry> map = this.getSkyMap();
               Iterator<Entry<Long, NMSRelighter.RelightSkyEntry>> iter = map.entrySet().iterator();

               while (iter.hasNext()) {
                  Entry<Long, NMSRelighter.RelightSkyEntry> entry = iter.next();
                  this.chunksToSend.put(entry.getKey(), entry.getValue().bitmask);
                  iter.remove();
               }
            }
         }

         this.fixBlockLighting();
         this.sendChunks();
      }
   }

   @Override
   public void fixBlockLighting() {
      synchronized (this.lightQueue) {
         while (!this.lightLock.compareAndSet(false, true)) {
            try {
               this.lightLock.wait(50L);
            } catch (InterruptedException var9) {
               LOGGER.error(var9);
            }
         }

         try {
            this.updateBlockLight(this.lightQueue);
         } finally {
            this.lightLock.set(false);
         }
      }
   }

   @Override
   public synchronized void close() {
      Iterator<Entry<Long, Integer>> iter = this.chunksToSend.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<Long, Integer> entry = iter.next();
         long pair = entry.getKey();
         int bitMask = entry.getValue();
         int x = MathMan.unpairIntX(pair);
         int z = MathMan.unpairIntY(pair);
         ChunkHolder<?> chunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(x, z);
         chunk.setBitMask(bitMask);
         iter.remove();
      }

      if (Settings.settings().LIGHTING.ASYNC) {
         this.queue.flush();
         this.finished.set(true);
      } else {
         TaskManager.taskManager().sync(new RunnableVal<Object>() {
            @Override
            public void run(Object value) {
               NMSRelighter.this.queue.flush();
               NMSRelighter.this.finished.set(true);
            }
         });
      }
   }

   public void flush() {
      this.close();
   }

   public synchronized void sendChunks() {
      RunnableVal<Object> runnable = new RunnableVal<Object>() {
         @Override
         public void run(Object value) {
            Iterator<Entry<Long, Integer>> iter = NMSRelighter.this.chunksToSend.entrySet().iterator();

            while (iter.hasNext()) {
               Entry<Long, Integer> entry = iter.next();
               long pair = entry.getKey();
               int bitMask = entry.getValue();
               int x = MathMan.unpairIntX(pair);
               int z = MathMan.unpairIntY(pair);
               ChunkHolder<?> chunk = (ChunkHolder<?>)NMSRelighter.this.queue.getOrCreateChunk(x, z);
               chunk.setBitMask(bitMask);
               chunk.flushLightToGet();
               Fawe.<IFawe>platform().getPlatformAdapter().sendChunk(chunk.getOrCreateGet(), bitMask, true);
               iter.remove();
            }

            NMSRelighter.this.finished.set(true);
         }
      };
      if (Settings.settings().LIGHTING.ASYNC) {
         runnable.run();
      } else {
         TaskManager.taskManager().sync(runnable);
      }
   }

   @Override
   public synchronized void fixSkyLighting() {
      Map<Long, NMSRelighter.RelightSkyEntry> map = this.getSkyMap();
      ArrayList<NMSRelighter.RelightSkyEntry> chunksList = new ArrayList<>(map.size());
      Iterator<Entry<Long, NMSRelighter.RelightSkyEntry>> iter = map.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<Long, NMSRelighter.RelightSkyEntry> entry = iter.next();
         this.chunksToSend.put(entry.getKey(), entry.getValue().bitmask);
         chunksList.add(entry.getValue());
         iter.remove();
      }

      Collections.sort(chunksList);
      int size = chunksList.size();
      if (size > 64) {
         int amount = (size + 64 - 1) / 64;

         for (int i = 0; i < amount; i++) {
            int start = i * 64;
            int end = Math.min(size, start + 64);
            List<NMSRelighter.RelightSkyEntry> sub = chunksList.subList(start, end);
            this.fixSkyLighting(sub);
         }
      } else {
         this.fixSkyLighting(chunksList);
      }
   }

   public void fill(byte[] mask, ChunkHolder<?> iChunk, int y, byte reason) {
      if (y >= 16) {
         Arrays.fill(mask, (byte)15);
      } else {
         switch (reason) {
            case 1:
               int index = 0;

               for (int z = 0; z < 16; z++) {
                  for (int x = 0; x < 16; x++) {
                     mask[index++] = (byte)iChunk.getSkyLight(x, y, z);
                  }
               }
               break;
            case 2:
               Arrays.fill(mask, (byte)0);
         }
      }
   }

   private void fixSkyLighting(List<NMSRelighter.RelightSkyEntry> sorted) {
      NMSRelighter.RelightSkyEntry[] chunks = sorted.toArray(new NMSRelighter.RelightSkyEntry[0]);
      boolean remove = this.removeFirst;
      BlockVectorSet chunkSet = null;
      if (remove) {
         BlockVectorSet tmpSet = new BlockVectorSet();
         chunkSet = new BlockVectorSet();

         for (NMSRelighter.RelightSkyEntry chunk : chunks) {
            tmpSet.add(chunk.x, 0, chunk.z);
         }

         for (NMSRelighter.RelightSkyEntry chunk : chunks) {
            int x = chunk.x;
            int z = chunk.z;
            if (tmpSet.contains(x + 1, 0, z) && tmpSet.contains(x - 1, 0, z) && tmpSet.contains(x, 0, z + 1) && tmpSet.contains(x, 0, z - 1)) {
               chunkSet.add(x, 0, z);
            }
         }
      }

      for (int y = this.maxY; y > this.minY; y--) {
         for (NMSRelighter.RelightSkyEntry chunkx : chunks) {
            int layer = y - this.minY >> 4;
            byte[] mask = chunkx.mask;
            int bx = chunkx.x << 4;
            int bz = chunkx.z << 4;
            ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunkx.x, chunkx.z);
            if (chunkx.fix[layer] != 0) {
               if ((y & 15) == 0 && layer != 0 && chunkx.fix[layer - 1] == 0) {
                  this.fill(mask, iChunk, y, chunkx.fix[layer]);
               }
            } else {
               if (!iChunk.isInit()) {
                  iChunk.init(this.queue, chunkx.x, chunkx.z);
               }

               chunkx.smooth = false;
               if (remove && (y & 15) == 15 && chunkSet.contains(chunkx.x, 0, chunkx.z)) {
                  iChunk.removeSectionLighting(y >> 4, true);
               }

               for (int j = 0; j < 256; j++) {
                  int x = j & 15;
                  int z = j >> 4;
                  byte value = mask[j];
                  BlockState state = iChunk.getBlock(x, y, z);
                  BlockMaterial material = state.getMaterial();
                  int opacity = material.getLightOpacity();
                  int brightness = material.getLightValue();
                  if (brightness > 0 && brightness != iChunk.getEmittedLight(x, y, z)) {
                     this.addLightUpdate(bx + x, y, bz + z);
                  }

                  switch (value) {
                     case 0:
                        if (opacity > 1) {
                           iChunk.setSkyLight(x, y, z, 0);
                           continue;
                        }
                        break;
                     case 1:
                     case 2:
                     case 3:
                     case 4:
                     case 5:
                     case 6:
                     case 7:
                     case 8:
                     case 9:
                     case 10:
                     case 11:
                     case 12:
                     case 13:
                     case 14:
                        if (opacity >= value) {
                           mask[j] = 0;
                           if (this.isStairOrTrueTop(state, true) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "double"))) {
                              iChunk.setSkyLight(x, y, z, 0);
                           } else {
                              iChunk.setSkyLight(x, y, z, value);
                           }
                           continue;
                        }

                        if (opacity <= 1) {
                           mask[j] = --value;
                        } else {
                           mask[j] = value = (byte)Math.max(0, value - opacity);
                        }
                        break;
                     case 15:
                        if (opacity > 0) {
                           value -= (byte)opacity;
                           mask[j] = value;
                        }

                        if (this.isStairOrTrueTop(state, true) && (this.isSlabOrTrueValue(state, "top") || this.isSlabOrTrueValue(state, "double"))) {
                           iChunk.setSkyLight(x, y, z, value);
                        } else {
                           iChunk.setSkyLight(x, y, z, value + opacity);
                        }
                        continue;
                  }

                  chunkx.smooth = true;
                  iChunk.setSkyLight(x, y, z, value);
               }
            }
         }

         for (NMSRelighter.RelightSkyEntry chunkxx : chunks) {
            if (chunkxx.smooth) {
               this.smoothSkyLight(chunkxx, y, true);
            }
         }

         for (int i = chunks.length - 1; i >= 0; i--) {
            NMSRelighter.RelightSkyEntry chunkxxx = chunks[i];
            if (chunkxxx.smooth) {
               this.smoothSkyLight(chunkxxx, y, false);
            }
         }
      }
   }

   private void smoothSkyLight(NMSRelighter.RelightSkyEntry chunk, int y, boolean direction) {
      byte[] mask = chunk.mask;
      ChunkHolder<?> iChunk = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x, chunk.z);
      if (!iChunk.isInit()) {
         iChunk.init(this.queue, chunk.x, chunk.z);
      }

      if (direction) {
         ChunkHolder<?> iChunkx = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x - 1, chunk.z);
         ChunkHolder<?> iChunkz = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x, chunk.z - 1);
         if (!iChunkx.isInit()) {
            iChunkx.init(this.queue, chunk.x - 1, chunk.z);
         }

         if (!iChunkz.isInit()) {
            iChunkz.init(this.queue, chunk.x, chunk.z - 1);
         }

         for (int j = 0; j < 256; j++) {
            int x = j & 15;
            int z = j >> 4;
            if (mask[j] < 14 && (mask[j] != 0 || iChunk.getOpacity(x, y, z) <= 1)) {
               byte value = mask[j];
               if (x != 0 && z != 0) {
                  if ((value = (byte)Math.max(iChunk.getSkyLight(x - 1, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunk.getSkyLight(x, y, z - 1) - 1, value);
                  }

                  if (value > mask[j]) {
                     iChunk.setSkyLight(x, y, z, mask[j] = value);
                  }
               } else if (x == 0 && z == 0) {
                  if ((value = (byte)Math.max(iChunkx.getSkyLight(15, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunkz.getSkyLight(x, y, 15) - 1, value);
                  }

                  if (value > mask[j]) {
                     iChunk.setSkyLight(x, y, z, mask[j] = value);
                  }
               } else if (x == 0) {
                  if ((value = (byte)Math.max(iChunkx.getSkyLight(15, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunk.getSkyLight(x, y, z - 1) - 1, value);
                  }

                  if (value > mask[j]) {
                     iChunk.setSkyLight(x, y, z, mask[j] = value);
                  }
               } else {
                  if ((value = (byte)Math.max(iChunk.getSkyLight(x - 1, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunkz.getSkyLight(x, y, 15) - 1, value);
                  }

                  if (value > mask[j]) {
                     iChunk.setSkyLight(x, y, z, mask[j] = value);
                  }
               }
            }
         }
      } else {
         ChunkHolder<?> iChunkxx = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x + 1, chunk.z);
         ChunkHolder<?> iChunkzx = (ChunkHolder<?>)this.queue.getOrCreateChunk(chunk.x, chunk.z + 1);
         if (!iChunkxx.isInit()) {
            iChunkxx.init(this.queue, chunk.x - 1, chunk.z);
         }

         if (!iChunkzx.isInit()) {
            iChunkzx.init(this.queue, chunk.x, chunk.z - 1);
         }

         for (int jx = 255; jx >= 0; jx--) {
            int x = jx & 15;
            int z = jx >> 4;
            if (mask[jx] < 14 && (mask[jx] != 0 || iChunk.getOpacity(x, y, z) <= 1)) {
               byte value = mask[jx];
               if (x != 15 && z != 15) {
                  if ((value = (byte)Math.max(iChunk.getSkyLight(x + 1, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunk.getSkyLight(x, y, z + 1) - 1, value);
                  }

                  if (value > mask[jx]) {
                     iChunk.setSkyLight(x, y, z, mask[jx] = value);
                  }
               } else if (x == 15 && z == 15) {
                  if ((value = (byte)Math.max(iChunkxx.getSkyLight(0, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunkzx.getSkyLight(x, y, 0) - 1, value);
                  }

                  if (value > mask[jx]) {
                     iChunk.setSkyLight(x, y, z, mask[jx] = value);
                  }
               } else if (x == 15) {
                  if ((value = (byte)Math.max(iChunkxx.getSkyLight(0, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunk.getSkyLight(x, y, z + 1) - 1, value);
                  }

                  if (value > mask[jx]) {
                     iChunk.setSkyLight(x, y, z, mask[jx] = value);
                  }
               } else {
                  if ((value = (byte)Math.max(iChunk.getSkyLight(x + 1, y, z) - 1, value)) < 14) {
                     value = (byte)Math.max(iChunkzx.getSkyLight(x, y, 0) - 1, value);
                  }

                  if (value > mask[jx]) {
                     iChunk.setSkyLight(x, y, z, mask[jx] = value);
                  }
               }
            }
         }
      }
   }

   private record LightRemovalNode(BlockVector3 pos, int previous) {
   }

   private static class RelightSkyEntry implements Comparable<NMSRelighter.RelightSkyEntry> {
      private static final Comparator<NMSRelighter.RelightSkyEntry> COMPARATOR = Comparator.comparingInt(NMSRelighter.RelightSkyEntry::x)
         .thenComparingInt(NMSRelighter.RelightSkyEntry::z);
      public final int x;
      public final int z;
      public final byte[] mask;
      public final byte[] fix;
      public int bitmask;
      public boolean smooth;

      private RelightSkyEntry(int x, int z, byte[] fix, int bitmask, int minY, int maxY) {
         this.x = x;
         this.z = z;
         byte[] array = new byte[256];
         Arrays.fill(array, (byte)15);
         this.mask = array;
         this.bitmask = bitmask;
         if (fix == null) {
            this.fix = new byte[maxY - minY + 1 >> 4];
            Arrays.fill(this.fix, (byte)0);
         } else {
            this.fix = fix;
         }
      }

      private int x() {
         return this.x;
      }

      private int z() {
         return this.z;
      }

      @Override
      public String toString() {
         return this.x + "," + this.z;
      }

      public int compareTo(@Nonnull NMSRelighter.RelightSkyEntry o) {
         return COMPARATOR.compare(this, o);
      }
   }
}
