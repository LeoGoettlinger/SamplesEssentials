package net.thenextlvl.nbt.tag;

import java.io.IOException;
import java.util.Arrays;
import net.thenextlvl.nbt.NBTOutputStream;
import org.jetbrains.annotations.Contract;

final class ByteArrayTagImpl extends ValueTagImpl<byte[]> implements ByteArrayTag {
   public ByteArrayTagImpl(byte... array) {
      super(array);
   }

   @Override
   public byte[] getAsByteArray() {
      return this.getValue();
   }

   @Override
   public byte getTypeId() {
      return 7;
   }

   @Contract(
      pure = true
   )
   @Override
   public int size() {
      return this.value.length;
   }

   @Contract(
      pure = true
   )
   public Byte get(int index) {
      return this.value[index];
   }

   public byte[] getValue() {
      return (byte[])this.value.clone();
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeInt(this.value.length);
      outputStream.write(this.value);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ByteArrayTagImpl valueTag ? Arrays.equals(this.value, valueTag.value) : false;
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

         for (int i = 0; i < ((byte[])this.value).length; i++) {
            if (i > 0) {
               builder.append(",");
            }

            builder.append(this.value[i]).append("b");
         }

         return builder.append(']').toString();
      }
   }
}
