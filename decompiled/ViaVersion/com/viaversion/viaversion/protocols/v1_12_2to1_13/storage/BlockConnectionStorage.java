package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.google.common.collect.EvictingQueue;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockConnectionStorage implements StorableObject {
   private final Long2ObjectMap<BlockConnectionStorage.SectionData> blockStorage = new Long2ObjectOpenHashMap<>();
   private final Queue<BlockPosition> modified = EvictingQueue.create(5);
   private long lastIndex = -1L;
   private BlockConnectionStorage.SectionData lastSection;

   public static void init() {
   }

   public void store(int x, int y, int z, int blockState) {
      long index = getChunkSectionIndex(x, y, z);
      BlockConnectionStorage.SectionData section = this.getSection(index);
      if (section == null) {
         if (blockState == 0) {
            return;
         }

         this.blockStorage.put(index, section = new BlockConnectionStorage.SectionData());
         this.lastSection = section;
         this.lastIndex = index;
      }

      section.setBlockAt(x, y, z, blockState);
   }

   public int get(int x, int y, int z) {
      long pair = getChunkSectionIndex(x, y, z);
      BlockConnectionStorage.SectionData section = this.getSection(pair);
      return section == null ? 0 : section.blockAt(x, y, z);
   }

   public void remove(int x, int y, int z) {
      long index = getChunkSectionIndex(x, y, z);
      BlockConnectionStorage.SectionData section = this.getSection(index);
      if (section != null) {
         section.setBlockAt(x, y, z, 0);
         if (section.nonEmptyBlocks() == 0) {
            this.removeSection(index);
         }
      }
   }

   public void markModified(BlockPosition pos) {
      if (!this.modified.contains(pos)) {
         this.modified.add(pos);
      }
   }

   public boolean recentlyModified(BlockPosition pos) {
      for (BlockPosition p : this.modified) {
         if (Math.abs(pos.x() - p.x()) + Math.abs(pos.y() - p.y()) + Math.abs(pos.z() - p.z()) <= 2) {
            return true;
         }
      }

      return false;
   }

   public void clear() {
      this.blockStorage.clear();
      this.lastSection = null;
      this.lastIndex = -1L;
      this.modified.clear();
   }

   public void unloadChunk(int x, int z) {
      for (int y = 0; y < 16; y++) {
         this.unloadSection(x, y, z);
      }
   }

   public void unloadSection(int x, int y, int z) {
      this.removeSection(getChunkSectionIndex(x << 4, y << 4, z << 4));
   }

   @Nullable
   private BlockConnectionStorage.SectionData getSection(long index) {
      if (this.lastIndex == index) {
         return this.lastSection;
      } else {
         this.lastIndex = index;
         return this.lastSection = this.blockStorage.get(index);
      }
   }

   private void removeSection(long index) {
      this.blockStorage.remove(index);
      if (this.lastIndex == index) {
         this.lastIndex = -1L;
         this.lastSection = null;
      }
   }

   private static long getChunkSectionIndex(int x, int y, int z) {
      return (x >> 4 & 67108863L) << 38 | (y >> 4 & 4095L) << 26 | z >> 4 & 67108863L;
   }

   private static final class SectionData {
      private final short[] blockStates = new short[4096];
      private short nonEmptyBlocks;

      public int blockAt(int x, int y, int z) {
         return this.blockStates[encodeBlockPos(x, y, z)];
      }

      public void setBlockAt(int x, int y, int z, int blockState) {
         int index = encodeBlockPos(x, y, z);
         if (blockState != this.blockStates[index]) {
            this.blockStates[index] = (short)blockState;
            if (blockState == 0) {
               this.nonEmptyBlocks--;
            } else {
               this.nonEmptyBlocks++;
            }
         }
      }

      public short nonEmptyBlocks() {
         return this.nonEmptyBlocks;
      }

      private static int encodeBlockPos(int x, int y, int z) {
         return (y & 15) << 8 | (x & 15) << 4 | z & 15;
      }
   }
}
