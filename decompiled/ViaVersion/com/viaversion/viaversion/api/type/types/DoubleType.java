package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class DoubleType extends Type<Double> implements TypeConverter<Double> {
   public DoubleType() {
      super(Double.class);
   }

   @Deprecated
   public Double read(ByteBuf buffer) {
      return buffer.readDouble();
   }

   public double readPrimitive(ByteBuf buffer) {
      return buffer.readDouble();
   }

   @Deprecated
   public void write(ByteBuf buffer, Double object) {
      buffer.writeDouble(object);
   }

   public void writePrimitive(ByteBuf buffer, double object) {
      buffer.writeDouble(object);
   }

   public void write(Ops ops, Double value) {
      ops.writeDouble(value);
   }

   public Double from(Object o) {
      if (o instanceof Number number) {
         return number.doubleValue();
      } else if (o instanceof Boolean boo) {
         return boo ? 1.0 : 0.0;
      } else {
         throw new UnsupportedOperationException();
      }
   }
}
