package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class FloatTagImpl extends NumberTagImpl<Float> implements FloatTag {
   public FloatTagImpl(Float value) {
      super(value);
   }

   @Override
   public float getAsFloat() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 5;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeFloat(this.value);
   }

   @Override
   public String toString() {
      return this.value + "f";
   }
}
