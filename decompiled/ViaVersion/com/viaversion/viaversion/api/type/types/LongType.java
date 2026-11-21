package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class LongType extends Type<Long> implements TypeConverter<Long> {
   public LongType() {
      super(Long.class);
   }

   @Deprecated
   public Long read(ByteBuf buffer) {
      return buffer.readLong();
   }

   @Deprecated
   public void write(ByteBuf buffer, Long object) {
      buffer.writeLong(object);
   }

   public void write(Ops ops, Long value) {
      ops.writeLong(value);
   }

   public Long from(Object o) {
      if (o instanceof Number number) {
         return number.longValue();
      } else if (o instanceof Boolean boo) {
         return boo ? 1L : 0L;
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public long readPrimitive(ByteBuf buffer) {
      return buffer.readLong();
   }

   public void writePrimitive(ByteBuf buffer, long object) {
      buffer.writeLong(object);
   }

   public static final class OptionalLongType extends OptionalType<Long> {
      public OptionalLongType() {
         super(Types.LONG);
      }
   }
}
