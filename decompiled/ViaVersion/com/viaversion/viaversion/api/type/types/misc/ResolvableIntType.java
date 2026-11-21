package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.ResolvableInt;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public final class ResolvableIntType extends Type<ResolvableInt> {
   public ResolvableIntType() {
      super(ResolvableInt.class);
   }

   public ResolvableInt read(ByteBuf buffer) {
      return buffer.readBoolean() ? ResolvableInt.of(Types.INT.readPrimitive(buffer)) : ResolvableInt.of(Types.STRING.read(buffer));
   }

   public void write(ByteBuf buffer, ResolvableInt value) {
      if (value.isLeft()) {
         buffer.writeBoolean(true);
         Types.INT.writePrimitive(buffer, value.left());
      } else {
         buffer.writeBoolean(false);
         Types.IDENTIFIER.write(buffer, Key.of(value.right()));
      }
   }

   public void write(Ops ops, ResolvableInt value) {
      if (value.isLeft()) {
         Types.INT.write(ops, value.left());
      } else {
         Types.IDENTIFIER.write(ops, Key.of(value.right()));
      }
   }
}
