package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class IntTagImpl extends NumberTagImpl<Integer> implements IntTag {
   public IntTagImpl(Integer value) {
      super(value);
   }

   @Override
   public int getAsInt() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 3;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeInt(this.value);
   }
}
