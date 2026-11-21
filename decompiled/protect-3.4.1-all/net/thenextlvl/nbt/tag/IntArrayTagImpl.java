package net.thenextlvl.nbt.tag;

import java.io.IOException;
import java.util.Arrays;
import net.thenextlvl.nbt.NBTOutputStream;

final class IntArrayTagImpl extends ValueTagImpl<int[]> implements IntArrayTag {
   public IntArrayTagImpl(int... value) {
      super(value);
   }

   @Override
   public int[] getAsIntArray() {
      return this.getValue();
   }

   @Override
   public byte getTypeId() {
      return 11;
   }

   @Override
   public int size() {
      return this.value.length;
   }

   public Integer get(int index) {
      return this.value[index];
   }

   public int[] getValue() {
      return (int[])this.value.clone();
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeInt(this.value.length);

      for (int i : this.value) {
         outputStream.writeInt(i);
      }
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof IntArrayTagImpl valueTag ? Arrays.equals(this.value, valueTag.value) : false;
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

         for (int i = 0; i < ((int[])this.value).length; i++) {
            if (i > 0) {
               builder.append(",");
            }

            builder.append(this.value[i]);
         }

         return builder.append(']').toString();
      }
   }
}
