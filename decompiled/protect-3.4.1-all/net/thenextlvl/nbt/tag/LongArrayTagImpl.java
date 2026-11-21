package net.thenextlvl.nbt.tag;

import java.io.IOException;
import java.util.Arrays;
import net.thenextlvl.nbt.NBTOutputStream;

final class LongArrayTagImpl extends ValueTagImpl<long[]> implements LongArrayTag {
   public LongArrayTagImpl(long... value) {
      super(value);
   }

   @Override
   public long[] getAsLongArray() {
      return this.getValue();
   }

   @Override
   public byte getTypeId() {
      return 12;
   }

   @Override
   public int size() {
      return this.value.length;
   }

   public Long get(int index) {
      return this.value[index];
   }

   public long[] getValue() {
      return (long[])this.value.clone();
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeLong(this.value.length);

      for (long l : this.value) {
         outputStream.writeLong(l);
      }
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof LongArrayTagImpl valueTag ? Arrays.equals(this.value, valueTag.value) : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.value);
   }

   @Override
   public String toString() {
      if (this.value.length == 0) {
         return "[]";
      } else {
         StringBuilder builder = new StringBuilder("[");

         for (int i = 0; i < ((long[])this.value).length; i++) {
            if (i > 0) {
               builder.append(",");
            }

            builder.append(this.value[i]).append("l");
         }

         return builder.append(']').toString();
      }
   }
}
