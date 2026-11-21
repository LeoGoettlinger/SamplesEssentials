package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class UnsignedByteType extends Type<Short> implements TypeConverter<Short> {
   public static final int MAX_VALUE = 255;

   public UnsignedByteType() {
      super("Unsigned Byte", Short.class);
   }

   public Short read(ByteBuf buffer) {
      return buffer.readUnsignedByte();
   }

   public void write(ByteBuf buffer, Short object) {
      buffer.writeByte(object);
   }

   public void write(Ops ops, Short value) {
      Types.BYTE.write(ops, (byte)(value & 255));
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
