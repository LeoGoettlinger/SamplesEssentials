package com.fastasyncworldedit.core.math;

public final class BitArray {
   private final long[] data;
   private final int bitsPerEntry;
   private final int maxSeqLocIndex;
   private final long mask;
   private final int longLen;

   public BitArray(int bitsPerEntry, int arraySize, long[] buffer) {
      this.bitsPerEntry = bitsPerEntry;
      this.maxSeqLocIndex = 64 - bitsPerEntry;
      this.mask = (1L << bitsPerEntry) - 1L;
      this.longLen = arraySize * bitsPerEntry >> 6;
      if (buffer.length < this.longLen) {
         this.data = new long[this.longLen];
      } else {
         this.data = buffer;
      }
   }

   public BitArray(int bitsPerEntry, int arraySize) {
      this.bitsPerEntry = bitsPerEntry;
      this.maxSeqLocIndex = 64 - bitsPerEntry;
      this.mask = (1L << bitsPerEntry) - 1L;
      this.longLen = arraySize * bitsPerEntry >> 6;
      this.data = new long[this.longLen];
   }

   public long[] getData() {
      return this.data;
   }

   public void set(int index, int value) {
      if (this.longLen != 0) {
         int bitIndexStart = index * this.bitsPerEntry;
         int longIndexStart = bitIndexStart >> 6;
         int localBitIndexStart = bitIndexStart & 63;
         this.data[longIndexStart] = this.data[longIndexStart] & ~(this.mask << localBitIndexStart) | (long)value << localBitIndexStart;
         if (localBitIndexStart > this.maxSeqLocIndex) {
            int longIndexEnd = longIndexStart + 1;
            int localShiftStart = 64 - localBitIndexStart;
            int localShiftEnd = this.bitsPerEntry - localShiftStart;
            this.data[longIndexEnd] = this.data[longIndexEnd] >>> localShiftEnd << localShiftEnd | (long)value >> localShiftStart;
         }
      }
   }

   public final int get(int index) {
      if (this.longLen == 0) {
         return 0;
      } else {
         int bitIndexStart = index * this.bitsPerEntry;
         int longIndexStart = bitIndexStart >> 6;
         int localBitIndexStart = bitIndexStart & 63;
         if (localBitIndexStart <= this.maxSeqLocIndex) {
            return (int)(this.data[longIndexStart] >>> localBitIndexStart & this.mask);
         } else {
            int localShift = 64 - localBitIndexStart;
            return (int)((this.data[longIndexStart] >>> localBitIndexStart | this.data[longIndexStart + 1] << localShift) & this.mask);
         }
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
         while (localStart <= maxSeqLocIndex) {
            int lastVal = arr[arrI++];
            l |= (long)lastVal << localStart;
            localStart += bitsPerEntry;
         }

         if (localStart < 64) {
            if (i != this.longLen - 1) {
               int lastVal = arr[arrI++];
               int shift = 64 - localStart;
               long nextVal = lastVal >> shift;
               l |= lastVal - (nextVal << shift) << localStart;
               data[i] = l;
               int var17 = i + 1;
               l = nextVal;
               data[var17] = nextVal;
               localStart -= maxSeqLocIndex;
            }
         } else {
            localStart = 0;
            data[i] = l;
            l = 0L;
         }
      }
   }

   public int[] toRaw() {
      return this.toRaw(new int[4096]);
   }

   public int[] toRaw(int[] buffer) {
      long[] data = this.data;
      int dataLength = this.longLen;
      int bitsPerEntry = this.bitsPerEntry;
      long maxEntryValue = this.mask;
      int maxSeqLocIndex = this.maxSeqLocIndex;
      int localStart = 0;
      int arrI = 0;

      for (int i = 0; i < dataLength; i++) {
         long l;
         for (l = data[i]; localStart <= maxSeqLocIndex; localStart += bitsPerEntry) {
            char lastVal = (char)(l >>> localStart & maxEntryValue);
            buffer[arrI++] = lastVal;
         }

         if (localStart < 64) {
            if (i != dataLength - 1) {
               char lastVal = (char)(l >>> localStart);
               localStart -= maxSeqLocIndex;
               l = data[i + 1];
               int localShift = bitsPerEntry - localStart;
               lastVal = (char)(lastVal | l << localShift);
               lastVal = (char)(lastVal & maxEntryValue);
               buffer[arrI++] = lastVal;
            }
         } else {
            localStart = 0;
         }
      }

      return buffer;
   }

   public char[] toRaw(char[] buffer) {
      long[] data = this.data;
      int dataLength = this.longLen;
      int bitsPerEntry = this.bitsPerEntry;
      long maxEntryValue = this.mask;
      int maxSeqLocIndex = this.maxSeqLocIndex;
      int localStart = 0;
      int arrI = 0;

      for (int i = 0; i < dataLength; i++) {
         long l;
         for (l = data[i]; localStart <= maxSeqLocIndex; localStart += bitsPerEntry) {
            char lastVal = (char)(l >>> localStart & maxEntryValue);
            buffer[arrI++] = lastVal;
         }

         if (localStart < 64) {
            if (i != dataLength - 1) {
               char lastVal = (char)(l >>> localStart);
               localStart -= maxSeqLocIndex;
               l = data[i + 1];
               int localShift = bitsPerEntry - localStart;
               lastVal = (char)(lastVal | l << localShift);
               lastVal = (char)(lastVal & maxEntryValue);
               buffer[arrI++] = lastVal;
            }
         } else {
            localStart = 0;
         }
      }

      return buffer;
   }
}
