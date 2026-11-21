package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.ResolvableFloat;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public final class ResolvableFloatType extends Type<ResolvableFloat> {
   public ResolvableFloatType() {
      super(ResolvableFloat.class);
   }

   public ResolvableFloat read(ByteBuf buffer) {
      return buffer.readBoolean() ? ResolvableFloat.of(Types.FLOAT.readPrimitive(buffer)) : ResolvableFloat.of(Types.STRING.read(buffer));
   }

   public void write(ByteBuf buffer, ResolvableFloat value) {
      if (value.isLeft()) {
         buffer.writeBoolean(true);
         Types.FLOAT.writePrimitive(buffer, value.left());
      } else {
         buffer.writeBoolean(false);
         Types.IDENTIFIER.write(buffer, Key.of(value.right()));
      }
   }

   public void write(Ops ops, ResolvableFloat value) {
      if (value.isLeft()) {
         Types.FLOAT.write(ops, value.left());
      } else {
         Types.IDENTIFIER.write(ops, Key.of(value.right()));
      }
   }
}
