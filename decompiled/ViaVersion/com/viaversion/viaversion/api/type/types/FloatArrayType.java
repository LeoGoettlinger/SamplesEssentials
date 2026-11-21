package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class FloatArrayType extends Type<float[]> {
   private final int length;

   public FloatArrayType(int length) {
      super(float[].class);
      this.length = length;
   }

   public FloatArrayType() {
      super(float[].class);
      this.length = -1;
   }

   public void write(ByteBuf buffer, float[] object) {
      if (this.length != -1) {
         Preconditions.checkArgument(this.length == object.length, "Length does not match expected length");
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.length);
      }

      for (float f : object) {
         buffer.writeFloat(f);
      }
   }

   public float[] read(ByteBuf buffer) {
      int length = ArrayType.checkLength(this.length == -1 ? Types.VAR_INT.readPrimitive(buffer) : this.length, 4, buffer.readableBytes());
      float[] array = new float[length];

      for (int i = 0; i < length; i++) {
         array[i] = buffer.readFloat();
      }

      return array;
   }
}
