package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class GlobalBlockPositionType extends Type<GlobalBlockPosition> {
   public GlobalBlockPositionType() {
      super(GlobalBlockPosition.class);
   }

   public GlobalBlockPosition read(ByteBuf buffer) {
      String dimension = Types.STRING.read(buffer);
      return Types.BLOCK_POSITION1_14.read(buffer).withDimension(dimension);
   }

   public void write(ByteBuf buffer, GlobalBlockPosition object) {
      Types.STRING.write(buffer, object.dimension());
      Types.BLOCK_POSITION1_14.write(buffer, object);
   }

   public void write(Ops ops, GlobalBlockPosition value) {
      ops.writeMap(map -> map.write("dimension", Types.IDENTIFIER, Key.of(value.dimension())).write("pos", Types.BLOCK_POSITION1_14, value));
   }

   public static final class OptionalGlobalPositionType extends OptionalType<GlobalBlockPosition> {
      public OptionalGlobalPositionType() {
         super(Types.GLOBAL_POSITION);
      }
   }
}
