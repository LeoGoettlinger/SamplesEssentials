package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Unit;
import io.netty.buffer.ByteBuf;

public final class EmptyType extends Type<Unit> {
   public EmptyType() {
      super(Unit.class);
   }

   public Unit read(ByteBuf buffer) {
      return Unit.INSTANCE;
   }

   public void write(ByteBuf buffer, Unit value) {
   }

   public void write(Ops ops, Unit value) {
      ops.writeMap(map -> {});
   }
}
