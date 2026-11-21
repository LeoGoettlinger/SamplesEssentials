package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class ShortTagImpl extends NumberTagImpl<Short> implements ShortTag {
   public ShortTagImpl(Short value) {
      super(value);
   }

   @Override
   public short getAsShort() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 2;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeShort(this.value);
   }

   @Override
   public String toString() {
      return this.value + "s";
   }
}
