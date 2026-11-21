package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class UnsignedIntType extends Type<Long> implements TypeConverter<Long> {
   public static final long MAX_UNSIGNED_INT = 4294967295L;

   public UnsignedIntType() {
      super(Long.class);
   }

   public Long read(ByteBuf buffer) {
      return buffer.readUnsignedInt();
   }

   public long readPrimitive(ByteBuf buffer) {
      return buffer.readUnsignedInt();
   }

   public void write(ByteBuf buffer, Long l) {
      buffer.writeInt(l.intValue());
   }

   public void writePrimitive(ByteBuf buffer, long l) {
      buffer.writeInt((int)l);
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
}
