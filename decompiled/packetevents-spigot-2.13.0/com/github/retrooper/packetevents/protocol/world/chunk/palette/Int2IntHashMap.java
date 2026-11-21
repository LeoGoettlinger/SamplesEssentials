package com.github.retrooper.packetevents.protocol.world.chunk.palette;

import java.util.Arrays;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
final class Int2IntHashMap {
   private static final float LOAD_FACTOR = 0.75F;
   public static final int EMPTY_VALUE = -1;
   private long[] data;
   private int size;
   private int threshold;

   public Int2IntHashMap(int expectedSize) {
      int capacity = tableSizeFor(expectedSize);
      this.threshold = (int)(capacity * 0.75F);
      this.data = new long[capacity];
      Arrays.fill(this.data, -1L);
   }

   private static int tableSizeFor(int cap) {
      return cap <= 1 ? 1 : 1 << 32 - Integer.numberOfLeadingZeros(cap - 1);
   }

   public int get(int key) {
      int mask = this.data.length - 1;

      long entry;
      for (int index = this.hash(key) & mask; (entry = this.data[index]) != -1L; index = index + 1 & mask) {
         if ((int)(entry & 4294967295L) == key) {
            return (int)(entry >>> 32);
         }
      }

      return -1;
   }

   public void put(int key, int value) {
      if (this.size >= this.threshold) {
         this.resize(this.data.length << 1);
      }

      long[] data = this.data;
      int mask = data.length - 1;

      int index;
      long entry;
      for (index = this.hash(key) & mask; (entry = data[index]) != -1L; index = index + 1 & mask) {
         if ((int)(entry & 4294967295L) == key) {
            data[index] = entry & 4294967295L | (value & 4294967295L) << 32;
            return;
         }
      }

      data[index] = key & 4294967295L | (value & 4294967295L) << 32;
      this.size++;
   }

   public void putIfAbsent(int key, int value) {
      if (this.size >= this.threshold) {
         this.resize(this.data.length << 1);
      }

      long[] data = this.data;
      int mask = data.length - 1;

      int index;
      long entry;
      for (index = this.hash(key) & mask; (entry = data[index]) != -1L; index = index + 1 & mask) {
         if ((int)(entry & 4294967295L) == key) {
            return;
         }
      }

      data[index] = key & 4294967295L | (value & 4294967295L) << 32;
      this.size++;
   }

   private void resize(int newCapacity) {
      long[] oldData = this.data;
      this.threshold = (int)(newCapacity * 0.75F);
      long[] data = new long[newCapacity];
      Arrays.fill(data, -1L);
      this.data = data;
      int mask = newCapacity - 1;
      int i = 0;

      for (int len = oldData.length; i < len; i++) {
         long entry = oldData[i];
         if (entry != -1L) {
            int key = (int)(entry & 4294967295L);
            int index = this.hash(key) & mask;

            while (data[index] != -1L) {
               index = index + 1 & mask;
            }

            data[index] = entry;
         }
      }
   }

   private int hash(int key) {
      key ^= key >>> 16;
      key *= -2048144789;
      key ^= key >>> 13;
      key *= -1028477387;
      return key ^ key >>> 16;
   }
}
