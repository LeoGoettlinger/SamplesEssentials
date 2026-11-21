package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class FloatType extends Type<Float> implements TypeConverter<Float> {
   public FloatType() {
      super(Float.class);
   }

   public float readPrimitive(ByteBuf buffer) {
      return buffer.readFloat();
   }

   public void writePrimitive(ByteBuf buffer, float object) {
      buffer.writeFloat(object);
   }

   @Deprecated
   public Float read(ByteBuf buffer) {
      return buffer.readFloat();
   }

   @Deprecated
   public void write(ByteBuf buffer, Float object) {
      buffer.writeFloat(object);
   }

   public Float from(Object o) {
      if (o instanceof Number number) {
         return number.floatValue();
      } else if (o instanceof Boolean boo) {
         return boo ? 1.0F : 0.0F;
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public void write(Ops ops, Float value) {
      ops.writeFloat(value);
   }

   public static final class OptionalFloatType extends OptionalType<Float> {
      public OptionalFloatType() {
         super(Types.FLOAT);
      }
   }
}
