package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class IntType extends Type<Integer> implements TypeConverter<Integer> {
   public IntType() {
      super(Integer.class);
   }

   public Integer read(ByteBuf buffer) {
      return buffer.readInt();
   }

   public int readPrimitive(ByteBuf buffer) {
      return buffer.readInt();
   }

   public void write(ByteBuf buffer, Integer object) {
      buffer.writeInt(object);
   }

   public void writePrimitive(ByteBuf buffer, int object) {
      buffer.writeInt(object);
   }

   public void write(Ops ops, Integer value) {
      ops.writeInt(value);
   }

   public Integer from(Object o) {
      if (o instanceof Number number) {
         return number.intValue();
      } else if (o instanceof Boolean boo) {
         return boo ? 1 : 0;
      } else {
         throw new UnsupportedOperationException();
      }
   }
}
