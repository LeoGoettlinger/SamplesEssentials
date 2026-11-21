package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record SuspiciousStewEffect(int mobEffect, int duration) {
   public static final Type<SuspiciousStewEffect> TYPE = new Type<SuspiciousStewEffect>(SuspiciousStewEffect.class) {
      public SuspiciousStewEffect read(ByteBuf buffer) {
         int effect = Types.VAR_INT.readPrimitive(buffer);
         int duration = Types.VAR_INT.readPrimitive(buffer);
         return new SuspiciousStewEffect(effect, duration);
      }

      public void write(ByteBuf buffer, SuspiciousStewEffect value) {
         Types.VAR_INT.writePrimitive(buffer, value.mobEffect);
         Types.VAR_INT.writePrimitive(buffer, value.duration);
      }

      public void write(Ops ops, SuspiciousStewEffect value) {
         ops.writeMap(map -> map.write("id", EnumTypes.MOB_EFFECT, value.mobEffect).writeOptional("duration", Types.INT, value.duration, 160));
      }
   };
   public static final Type<SuspiciousStewEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);
}
