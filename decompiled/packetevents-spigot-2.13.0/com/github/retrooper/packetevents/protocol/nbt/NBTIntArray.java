package com.github.retrooper.packetevents.protocol.nbt;

import java.util.Arrays;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTIntArray extends NBT {
   protected final int[] array;

   public NBTIntArray(int[] array) {
      this.array = array;
   }

   @Override
   public NBTType<NBTIntArray> getType() {
      return NBTType.INT_ARRAY;
   }

   public int[] getValue() {
      return this.array;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NBTIntArray other = (NBTIntArray)obj;
         return Arrays.equals(this.array, other.array);
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.array);
   }

   public NBTIntArray copy() {
      int[] aint = new int[this.array.length];
      System.arraycopy(this.array, 0, aint, 0, this.array.length);
      return new NBTIntArray(aint);
   }

   @Override
   public String toString() {
      return "IntArray(" + Arrays.toString(this.array) + ")";
   }
}
