package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class DoubleTagImpl extends NumberTagImpl<Double> implements DoubleTag {
   public DoubleTagImpl(Double value) {
      super(value);
   }

   @Override
   public double getAsDouble() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 6;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeDouble(this.value);
   }
}
