package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record SwingAnimation(int type, int duration) {
   public static final Type<SwingAnimation> TYPE = new SwingAnimation.SwingAnimationType(EnumTypes.SWING_ANIMATION1_21_11.idFromName("whack"));

   private static class SwingAnimationType extends Type<SwingAnimation> {
      private final int defaultType;

      public SwingAnimationType(int defaultType) {
         super(SwingAnimation.class);
         this.defaultType = defaultType;
      }

      public SwingAnimation read(ByteBuf buffer) {
         int type = Types.VAR_INT.readPrimitive(buffer);
         int duration = Types.VAR_INT.readPrimitive(buffer);
         return new SwingAnimation(type, duration);
      }

      public void write(ByteBuf buffer, SwingAnimation value) {
         Types.VAR_INT.writePrimitive(buffer, value.type);
         Types.VAR_INT.writePrimitive(buffer, value.duration);
      }

      public void write(Ops ops, SwingAnimation value) {
         ops.writeMap(
            map -> map.writeOptional("type", EnumTypes.SWING_ANIMATION1_21_11, value.type, this.defaultType)
               .writeOptional("duration", Types.INT, value.duration, 6)
         );
      }
   }
}
