package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class ByteTagImpl extends NumberTagImpl<Byte> implements ByteTag {
   public ByteTagImpl(Byte value) {
      super(value);
   }

   @Override
   public byte getAsByte() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 1;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.write(this.value);
   }

   @Override
   public String toString() {
      return this.value + "b";
   }
}
