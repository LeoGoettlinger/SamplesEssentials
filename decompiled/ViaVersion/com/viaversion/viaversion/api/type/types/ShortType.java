package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class ShortType extends Type<Short> implements TypeConverter<Short> {
   public ShortType() {
      super(Short.class);
   }

   public short readPrimitive(ByteBuf buffer) {
      return buffer.readShort();
   }

   public void writePrimitive(ByteBuf buffer, short object) {
      buffer.writeShort(object);
   }

   @Deprecated
   public Short read(ByteBuf buffer) {
      return buffer.readShort();
   }

   @Deprecated
   public void write(ByteBuf buffer, Short object) {
      buffer.writeShort(object);
   }

   public void write(Ops ops, Short value) {
      ops.writeShort(value);
   }

   public Short from(Object o) {
      if (o instanceof Number number) {
         return number.shortValue();
      } else if (o instanceof Boolean boo) {
         return Short.valueOf((short)(boo ? 1 : 0));
      } else {
         throw new UnsupportedOperationException();
      }
   }
}
