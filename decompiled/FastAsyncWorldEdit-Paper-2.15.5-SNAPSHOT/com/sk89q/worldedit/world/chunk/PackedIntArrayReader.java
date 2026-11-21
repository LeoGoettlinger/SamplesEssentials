package com.sk89q.worldedit.world.chunk;

import com.google.common.base.Preconditions;

public class PackedIntArrayReader {
   private static final int[] FACTORS = new int[64];
   private final long[] data;
   private final int elementBits;
   private final long maxValue;
   private final int elementsPerLong;
   private final int factor;
   private final int storedSize;

   public PackedIntArrayReader(long[] data) {
      this.data = data;
      this.storedSize = 4096;
      this.elementBits = data.length * 64 / 4096;
      this.maxValue = (1L << this.elementBits) - 1L;
      this.elementsPerLong = 64 / this.elementBits;
      this.factor = FACTORS[this.elementsPerLong - 1];
      int j = (this.storedSize + this.elementsPerLong - 1) / this.elementsPerLong;
      if (j != data.length) {
         throw new IllegalStateException("Invalid packed-int array provided, should be of length " + j);
      }
   }

   public PackedIntArrayReader(long[] data, int storedSize) {
      this.data = data;
      this.storedSize = storedSize;
      this.elementBits = data.length * 64 / storedSize;
      this.maxValue = (1L << this.elementBits) - 1L;
      this.elementsPerLong = 64 / this.elementBits;
      this.factor = FACTORS[this.elementsPerLong - 1];
      int j = (storedSize + this.elementsPerLong - 1) / this.elementsPerLong;
      if (j != data.length) {
         throw new IllegalStateException("Invalid packed-int array provided, should be of length " + j);
      }
   }

   public int get(int index) {
      Preconditions.checkElementIndex(index, this.storedSize);
      int i = this.adjustIndex(index);
      long l = this.data[i];
      int j = (index - i * this.elementsPerLong) * this.elementBits;
      return (int)(l >> j & this.maxValue);
   }

   private int adjustIndex(int i) {
      return (int)((long)i * this.factor + this.factor >> 32);
   }

   static {
      FACTORS[0] = -1;

      for (int i = 2; i <= 64; i++) {
         FACTORS[i - 1] = (int)(Integer.toUnsignedLong(-1) / i);
      }
   }
}
