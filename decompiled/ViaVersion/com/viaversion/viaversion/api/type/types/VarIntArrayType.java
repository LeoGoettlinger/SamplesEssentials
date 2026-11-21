package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class VarIntArrayType extends Type<int[]> {
   private final int length;

   public VarIntArrayType(int length) {
      super(int[].class);
      this.length = length;
   }

   public VarIntArrayType() {
      this(-1);
   }

   public int[] read(ByteBuf buffer) {
      int length = this.length == -1 ? Types.VAR_INT.readPrimitive(buffer) : this.length;
      Preconditions.checkArgument(buffer.isReadable(length));
      int[] array = new int[length];

      for (int i = 0; i < array.length; i++) {
         array[i] = Types.VAR_INT.readPrimitive(buffer);
      }

      return array;
   }

   public void write(ByteBuf buffer, int[] object) {
      if (this.length != -1) {
         Preconditions.checkArgument(this.length == object.length, "Length does not match expected length");
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.length);
      }

      for (int i : object) {
         Types.VAR_INT.writePrimitive(buffer, i);
      }
   }
}
