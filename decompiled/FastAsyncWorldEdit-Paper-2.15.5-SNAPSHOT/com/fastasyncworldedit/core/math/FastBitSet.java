package com.fastasyncworldedit.core.math;

import java.util.Arrays;

public class FastBitSet {
   private final int size;
   private final long[] bits;

   public FastBitSet(int size) {
      this.size = size;
      this.bits = new long[size + 64 >> 6];
   }

   public FastBitSet(long[] bits, int size) {
      this.bits = bits;
      this.size = size;
   }

   public static long[] create(int size) {
      return new long[size + 64 >> 6];
   }

   public static boolean get(long[] bits, final int i) {
      return (bits[i >> 6] & 1L << (i & 63)) != 0L;
   }

   public static void set(long[] bits, final int i) {
      bits[i >> 6] = bits[i >> 6] | 1L << (i & 63);
   }

   public static void clear(long[] bits, final int i) {
      bits[i >> 6] = bits[i >> 6] & ~(1L << (i & 63));
   }

   public static void set(long[] bits, final int i, final boolean v) {
      if (v) {
         set(bits, i);
      } else {
         clear(bits, i);
      }
   }

   public static void setRange(long[] bits, final int b, final int e) {
      int bt = b >> 6;
      int et = e >> 6;
      if (bt != et) {
         fill(bits, bt + 1, et, -1L);
         bits[bt] |= -1L << (b & 63);
         bits[et] |= ~(-1L << (e & 63));
      } else {
         bits[bt] |= -1L << (b & 63) & ~(-1L << (e & 63));
      }
   }

   public static void clearRange(long[] bits, final int b, final int e) {
      int bt = b >> 6;
      int et = e >> 6;
      if (bt != et) {
         fill(bits, bt + 1, et, 0L);
         bits[bt] &= ~(-1L << (b & 63));
         bits[et] &= -1L << (e & 63);
      } else {
         bits[bt] &= ~(-1L << (b & 63)) | -1L << (e & 63);
      }
   }

   public static void setAll(long[] bits) {
      Arrays.fill(bits, -1L);
   }

   public static void unsetAll(long[] bits) {
      Arrays.fill(bits, 0L);
   }

   public static void and(long[] bits, final long[] other) {
      int end = Math.min(other.length, bits.length);

      for (int i = 0; i < end; i++) {
         bits[i] &= other[i];
      }
   }

   public static void or(long[] bits, final long[] other) {
      int end = Math.min(other.length, bits.length);

      for (int i = 0; i < end; i++) {
         bits[i] |= other[i];
      }
   }

   private static void fill(final long[] a, final int b, final int e, final long l) {
      for (int i = b; i < e; i++) {
         a[i] = l;
      }
   }

   public boolean get(final int i) {
      return (this.bits[i >> 6] & 1L << (i & 63)) != 0L;
   }

   public void set(final int i) {
      this.bits[i >> 6] = this.bits[i >> 6] | 1L << (i & 63);
   }

   public void clear(final int i) {
      this.bits[i >> 6] = this.bits[i >> 6] & ~(1L << (i & 63));
   }

   public void set(final int i, final boolean v) {
      if (v) {
         this.set(i);
      } else {
         this.clear(i);
      }
   }

   public void setRange(final int b, final int e) {
      int bt = b >> 6;
      int et = e >> 6;
      if (bt != et) {
         fill(this.bits, bt + 1, et, -1L);
         this.bits[bt] = this.bits[bt] | -1L << (b & 63);
         this.bits[et] = this.bits[et] | ~(-1L << (e & 63));
      } else {
         this.bits[bt] = this.bits[bt] | -1L << (b & 63) & ~(-1L << (e & 63));
      }
   }

   public void clearRange(final int b, final int e) {
      int bt = b >> 6;
      int et = e >> 6;
      if (bt != et) {
         fill(this.bits, bt + 1, et, 0L);
         this.bits[bt] = this.bits[bt] & ~(-1L << (b & 63));
         this.bits[et] = this.bits[et] & -1L << (e & 63);
      } else {
         this.bits[bt] = this.bits[bt] & (~(-1L << (b & 63)) | -1L << (e & 63));
      }
   }

   public void and(final FastBitSet other) {
      int end = Math.min(other.bits.length, this.bits.length);

      for (int i = 0; i < end; i++) {
         this.bits[i] = this.bits[i] & other.bits[i];
      }
   }

   public void or(final FastBitSet other) {
      int end = Math.min(other.bits.length, this.bits.length);

      for (int i = 0; i < end; i++) {
         this.bits[i] = this.bits[i] | other.bits[i];
      }
   }

   public int cardinality() {
      if (this.size == 0) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < this.bits.length - 1; i++) {
            count += Long.bitCount(this.bits[i]);
         }

         return count + Long.bitCount(this.bits[this.bits.length - 1] & ~(-1L << (this.size & 63)));
      }
   }

   public int size() {
      return this.size;
   }

   public FastBitSet.IntIterator iterator() {
      return new FastBitSet.IntIterator();
   }

   public class IntIterator {
      int index = 0;
      long bitBuffer = 0L;
      int value = 0;

      public boolean next() {
         while (this.bitBuffer == 0L) {
            if (this.index >= FastBitSet.this.bits.length) {
               return false;
            }

            this.bitBuffer = FastBitSet.this.bits[this.index];
            this.index++;
         }

         long lowBit = Long.lowestOneBit(this.bitBuffer);
         int bitIndex = Long.bitCount(lowBit - 1L);
         this.value = (this.index - 1 << 6) + bitIndex;
         this.bitBuffer ^= lowBit;
         return true;
      }

      public int getValue() {
         return this.value;
      }
   }
}
