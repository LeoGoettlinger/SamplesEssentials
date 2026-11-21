package com.fastasyncworldedit.core.math;

import com.fastasyncworldedit.core.util.MathMan;

public final class BitArrayUnstretched {
   private final long[] data;
   private final int bitsPerEntry;
   private final int maxSeqLocIndex;
   private final int emptyBitCount;
   private final long mask;
   private final int longLen;

   public BitArrayUnstretched(int bitsPerEntry, int arraySize, long[] buffer) {
      this.bitsPerEntry = bitsPerEntry;
      this.mask = (1L << bitsPerEntry) - 1L;
      this.emptyBitCount = 64 % bitsPerEntry;
      this.maxSeqLocIndex = 64 - (bitsPerEntry + this.emptyBitCount);
      int blocksPerLong = MathMan.floorZero(64.0 / bitsPerEntry);
      this.longLen = MathMan.ceilZero((float)arraySize / blocksPerLong);
      if (buffer.length < this.longLen) {
         this.data = new long[this.longLen];
      } else {
         this.data = buffer;
      }
   }

   public BitArrayUnstretched(int bitsPerEntry, int arraySize) {
      this.bitsPerEntry = bitsPerEntry;
      this.mask = (1L << bitsPerEntry) - 1L;
      this.emptyBitCount = 64 % bitsPerEntry;
      this.maxSeqLocIndex = 64 - bitsPerEntry;
      int blocksPerLong = MathMan.floorZero(64.0 / bitsPerEntry);
      this.longLen = MathMan.ceilZero((float)arraySize / blocksPerLong);
      this.data = new long[this.longLen];
   }

   public long[] getData() {
      return this.data;
   }

   public void set(int index, int value) {
      if (this.longLen != 0) {
         int bitIndexStart = index * this.bitsPerEntry + MathMan.floorZero((double)index / this.longLen) * this.emptyBitCount;
         int longIndexStart = bitIndexStart >> 6;
         int localBitIndexStart = bitIndexStart & 63;
         this.data[longIndexStart] = this.data[longIndexStart] & ~(this.mask << localBitIndexStart) | (long)value << localBitIndexStart;
      }
   }

   public int get(int index) {
      if (this.longLen == 0) {
         return 0;
      } else {
         int bitIndexStart = index * this.bitsPerEntry + MathMan.floorZero((double)index / this.longLen) * this.emptyBitCount;
         int longIndexStart = bitIndexStart >> 6;
         int localBitIndexStart = bitIndexStart & 63;
         return (int)(this.data[longIndexStart] >>> localBitIndexStart & this.mask);
      }
   }

   public int getLength() {
      return this.longLen;
   }

   public void fromRaw(int[] arr) {
      long[] data = this.data;
      int bitsPerEntry = this.bitsPerEntry;
      int maxSeqLocIndex = this.maxSeqLocIndex;
      int localStart = 0;
      int arrI = 0;
      long l = 0L;

      for (int i = 0; i < this.longLen; i++) {
         while (localStart <= maxSeqLocIndex && arrI < arr.length) {
            int lastVal = arr[arrI++];
            l |= (long)lastVal << localStart;
            localStart += bitsPerEntry;
         }

         localStart = 0;
         data[i] = l;
         l = 0L;
      }
   }

   public int[] toRaw() {
      return this.toRaw(new int[4096]);
   }

   public int[] toRaw(int[] buffer) {
      long[] data = this.data;
      int bitsPerEntry = this.bitsPerEntry;
      int maxSeqLocIndex = this.maxSeqLocIndex;
      int localStart = 0;
      int arrI = 0;

      for (int i = 0; i < this.longLen; i++) {
         for (long l = data[i]; localStart <= maxSeqLocIndex && arrI < buffer.length; localStart += bitsPerEntry) {
            char lastVal = (char)(l >>> localStart & this.mask);
            buffer[arrI++] = lastVal;
         }

         localStart = 0;
      }

      return buffer;
   }

   public char[] toRaw(char[] buffer) {
      long[] data = this.data;
      int bitsPerEntry = this.bitsPerEntry;
      int maxSeqLocIndex = this.maxSeqLocIndex;
      int localStart = 0;
      int arrI = 0;

      for (int i = 0; i < this.longLen; i++) {
         for (long l = data[i]; localStart <= maxSeqLocIndex && arrI < buffer.length; localStart += bitsPerEntry) {
            char lastVal = (char)(l >>> localStart & this.mask);
            buffer[arrI++] = lastVal;
         }

         localStart = 0;
      }

      return buffer;
   }
}
