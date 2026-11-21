package com.github.retrooper.packetevents.protocol.nbt;

import java.util.Arrays;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTByteArray extends NBT {
   protected final byte[] array;

   public NBTByteArray(byte[] array) {
      this.array = array;
   }

   @Override
   public NBTType<NBTByteArray> getType() {
      return NBTType.BYTE_ARRAY;
   }

   public byte[] getValue() {
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
         NBTByteArray other = (NBTByteArray)obj;
         return Arrays.equals(this.array, other.array);
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.array);
   }

   public NBTByteArray copy() {
      byte[] abyte = new byte[this.array.length];
      System.arraycopy(this.array, 0, abyte, 0, this.array.length);
      return new NBTByteArray(abyte);
   }

   @Override
   public String toString() {
      return "ByteArray(" + Arrays.toString(this.array) + ")";
   }
}
