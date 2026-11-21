package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class FixedLengthArrayType<T> extends ArrayType<T> {
   private final int length;

   public FixedLengthArrayType(Type<T> type, int length) {
      super(type);
      this.length = length;
   }

   @Override
   public T[] read(ByteBuf buffer) {
      return this.readArray(buffer, this.length);
   }

   @Override
   public void write(ByteBuf buffer, T[] object) {
      for (T o : object) {
         this.elementType.write(buffer, o);
      }
   }

   public final class OptionalFixedLengthArrayType extends OptionalType<T[]> {
      public OptionalFixedLengthArrayType() {
         super(FixedLengthArrayType.this);
      }
   }
}
