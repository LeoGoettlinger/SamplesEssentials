package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class LongTagImpl extends NumberTagImpl<Long> implements LongTag {
   public LongTagImpl(Long value) {
      super(value);
   }

   @Override
   public long getAsLong() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 4;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeLong(this.value);
   }

   @Override
   public String toString() {
      return this.value + "l";
   }
}
