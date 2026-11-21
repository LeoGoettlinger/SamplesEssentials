package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class StringTagImpl extends ValueTagImpl<String> implements StringTag {
   public StringTagImpl(String value) {
      super(value);
   }

   @Override
   public boolean isString() {
      return true;
   }

   @Override
   public String getAsString() {
      return this.value;
   }

   @Override
   public byte getTypeId() {
      return 8;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      byte[] bytes = this.value.getBytes(outputStream.getCharset());
      outputStream.writeShort(bytes.length);
      outputStream.write(bytes);
   }

   @Override
   public String toString() {
      return this.shouldQuote(this.value) ? "\"" + this.value + "\"" : this.value;
   }

   private boolean shouldQuote(String str) {
      return !str.chars().allMatch(c -> c >= 48 && c <= 57 || c >= 65 && c <= 90 || c >= 97 && c <= 122 || c == 95);
   }
}
