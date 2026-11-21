package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class BooleanArrayType extends Type<boolean[]> {
   private final int length;

   public BooleanArrayType(int length) {
      super(boolean[].class);
      this.length = length;
   }

   public BooleanArrayType() {
      super(boolean[].class);
      this.length = -1;
   }

   public void write(ByteBuf buffer, boolean[] object) {
      if (this.length != -1) {
         Preconditions.checkArgument(this.length == object.length, "Length does not match expected length");
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.length);
      }

      for (boolean b : object) {
         buffer.writeBoolean(b);
      }
   }

   public boolean[] read(ByteBuf buffer) {
      int length = this.length == -1 ? Types.VAR_INT.readPrimitive(buffer) : this.length;
      Preconditions.checkArgument(buffer.isReadable(length), "Length is larger than readable bytes");
      boolean[] array = new boolean[length];

      for (int i = 0; i < length; i++) {
         array[i] = buffer.readBoolean();
      }

      return array;
   }
}
