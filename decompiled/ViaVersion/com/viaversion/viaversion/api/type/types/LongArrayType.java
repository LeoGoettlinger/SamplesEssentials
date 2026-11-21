package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class LongArrayType extends Type<long[]> {
   private final int length;

   public LongArrayType(int length) {
      super(long[].class);
      this.length = length;
   }

   public LongArrayType() {
      this(-1);
   }

   public long[] read(ByteBuf buffer) {
      int length = ArrayType.checkLength(this.length == -1 ? Types.VAR_INT.readPrimitive(buffer) : this.length, 8, buffer.readableBytes());
      return readFixedLength(buffer, length);
   }

   public static long[] readFixedLength(ByteBuf buffer, int expectedLength) {
      long[] value = new long[expectedLength];

      for (int i = 0; i < expectedLength; i++) {
         value[i] = buffer.readLong();
      }

      return value;
   }

   public void write(ByteBuf buffer, long[] object) {
      if (this.length != -1) {
         Preconditions.checkArgument(this.length == object.length, "Length does not match expected length");
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.length);
      }

      writeFixedLength(buffer, object);
   }

   public static void writeFixedLength(ByteBuf buffer, long[] object) {
      for (long l : object) {
         buffer.writeLong(l);
      }
   }

   public void write(Ops ops, long[] value) {
      ops.writeLongs(value);
   }
}
