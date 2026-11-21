package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class ByteType extends Type<Byte> implements TypeConverter<Byte> {
   public ByteType() {
      super(Byte.class);
   }

   public byte readPrimitive(ByteBuf buffer) {
      return buffer.readByte();
   }

   public void writePrimitive(ByteBuf buffer, byte object) {
      buffer.writeByte(object);
   }

   @Deprecated
   public Byte read(ByteBuf buffer) {
      return buffer.readByte();
   }

   @Deprecated
   public void write(ByteBuf buffer, Byte object) {
      buffer.writeByte(object);
   }

   public void write(Ops ops, Byte value) {
      ops.writeByte(value);
   }

   public Byte from(Object o) {
      if (o instanceof Number number) {
         return number.byteValue();
      } else if (o instanceof Boolean boo) {
         return Byte.valueOf((byte)(boo ? 1 : 0));
      } else {
         throw new UnsupportedOperationException();
      }
   }
}
