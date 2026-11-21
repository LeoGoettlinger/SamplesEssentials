package com.fastasyncworldedit.core.queue.implementation.blocks;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public abstract class CharBlocks implements IBlocks {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected static final CharBlocks.Section FULL = new CharBlocks.Section() {
      @Override
      public char[] get(CharBlocks blocks, int layer, char[] arr) {
         return arr;
      }

      @Override
      public char[] get(CharBlocks blocks, int layer, char[] arr, boolean aggressive) {
         return arr;
      }
   };
   protected static final CharBlocks.Section EMPTY = new CharBlocks.Section() {
      @Override
      public char[] get(CharBlocks blocks, int layer, char[] arr) {
         return this.get(blocks, layer, arr, true);
      }

      @Override
      public char[] get(CharBlocks blocks, int layer, char[] arr, boolean aggressive) {
         synchronized (blocks.sectionLocks[layer]) {
            return update(blocks, layer, aggressive);
         }
      }
   };
   public char[][] blocks;
   public Object[] sectionLocks;
   protected int minSectionPosition;
   protected int maxSectionPosition;
   protected int sectionCount;
   private int chunkX;
   private int chunkZ;

   public CharBlocks(int minSectionPosition, int maxSectionPosition) {
      this.minSectionPosition = minSectionPosition;
      this.maxSectionPosition = maxSectionPosition;
      this.sectionCount = maxSectionPosition - minSectionPosition + 1;
      this.blocks = new char[this.sectionCount][];
      this.sectionLocks = new Object[this.sectionCount];

      for (int i = 0; i < this.sectionCount; i++) {
         this.sectionLocks[i] = new Object();
      }
   }

   public void init(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   @Override
   public boolean trim(boolean aggressive) {
      for (int i = 0; i < this.sectionCount; i++) {
         synchronized (this.sectionLocks[i]) {
            if (this.blocks[i] != null) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean trim(boolean aggressive, int layer) {
      synchronized (this.sectionLocks[layer]) {
         return this.blocks[layer] == null;
      }
   }

   public IChunkSet reset() {
      for (int i = 0; i < this.sectionCount; i++) {
         synchronized (this.sectionLocks[i]) {
            this.blocks[i] = null;
         }
      }

      return null;
   }

   public void reset(int layer) {
      layer -= this.minSectionPosition;
      synchronized (this.sectionLocks[layer]) {
         this.blocks[layer] = null;
      }
   }

   public char[] update(int layer, char[] data, boolean aggressive) {
      if (data == null) {
         return new char[4096];
      } else {
         Arrays.fill(data, this.defaultOrdinal());
         return data;
      }
   }

   @Override
   public boolean hasSection(int layer) {
      layer -= this.minSectionPosition;
      return layer >= 0 && layer < this.blocks.length && this.blocks[layer] != null;
   }

   @Override
   public char[] load(int layer) {
      layer -= this.minSectionPosition;
      char[] data = this.blocks[layer];
      return (data == null ? EMPTY : FULL).get(this, layer, data);
   }

   @Nullable
   @Override
   public char[] loadIfPresent(int layer) {
      if (layer >= this.minSectionPosition && layer <= this.maxSectionPosition) {
         layer -= this.minSectionPosition;
         return this.blocks[layer];
      } else {
         return null;
      }
   }

   @Override
   public int getSectionCount() {
      return this.sectionCount;
   }

   @Override
   public int getMaxSectionPosition() {
      return this.maxSectionPosition;
   }

   @Override
   public int getMinSectionPosition() {
      return this.minSectionPosition;
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BlockTypesCache.states[this.get(x, y, z)];
   }

   public char get(int x, int y, int z) {
      int layer = y >> 4;
      int index = (y & 15) << 8 | z << 4 | x;
      return layer <= this.maxSectionPosition && layer >= this.minSectionPosition ? this.get(layer, index) : this.defaultOrdinal();
   }

   @Override
   public int getX() {
      return this.chunkX;
   }

   @Override
   public int getZ() {
      return this.chunkZ;
   }

   protected abstract char defaultOrdinal();

   public void set(int x, int y, int z, char value) {
      int layer = y >> 4;
      int index = (y & 15) << 8 | z << 4 | x;

      try {
         this.set(layer, index, value);
      } catch (ArrayIndexOutOfBoundsException var8) {
         LOGGER.error("Tried setting block at coordinates (" + x + "," + y + "," + z + ")");

         assert Fawe.platform() != null;

         LOGGER.error("Layer variable was = {}", layer, var8);
      }
   }

   public final char get(int layer, int index) {
      char[] data = this.blocks[layer - this.minSectionPosition];
      return (data == null ? EMPTY : FULL).get(this, layer, index, data);
   }

   public final void set(int layer, int index, char value) throws ArrayIndexOutOfBoundsException {
      char[] data = this.blocks[layer - this.minSectionPosition];
      (data == null ? EMPTY : FULL).set(this, layer, index, value, data);
   }

   public abstract static class Section {
      static char[] update(CharBlocks blocks, int layer, boolean aggressive) {
         char[] arr = blocks.blocks[layer];
         if (arr == null) {
            arr = blocks.blocks[layer] = blocks.update(layer, null, aggressive);
            if (arr == null) {
               throw new IllegalStateException("Array cannot be null: " + blocks.getClass());
            }
         } else {
            blocks.blocks[layer] = blocks.update(layer, arr, aggressive);
            if (blocks.blocks[layer] == null) {
               throw new IllegalStateException("Array cannot be null (update): " + blocks.getClass());
            }
         }

         return arr;
      }

      abstract char[] get(CharBlocks blocks, int layer, char[] data);

      abstract char[] get(CharBlocks blocks, int layer, char[] data, boolean aggressive);

      public final char get(CharBlocks blocks, int layer, int index, char[] data) {
         int normalized = layer - blocks.minSectionPosition;
         char[] section = this.get(blocks, normalized, data);
         return section[index];
      }

      public final void set(CharBlocks blocks, int layer, int index, char value, char[] data) {
         layer -= blocks.minSectionPosition;
         this.get(blocks, layer, data)[index] = value;
      }
   }
}
