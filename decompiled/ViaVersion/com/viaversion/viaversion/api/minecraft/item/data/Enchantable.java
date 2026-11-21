package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record Enchantable(int value) {
   public static final Type<Enchantable> TYPE = new Type<Enchantable>(Enchantable.class) {
      public Enchantable read(ByteBuf buffer) {
         return new Enchantable(Types.VAR_INT.readPrimitive(buffer));
      }

      public void write(ByteBuf buffer, Enchantable value) {
         Types.VAR_INT.writePrimitive(buffer, value.value);
      }

      public void write(Ops ops, Enchantable enchantable) {
         ops.writeMap(map -> map.write("value", Types.INT, enchantable.value));
      }
   };
}
