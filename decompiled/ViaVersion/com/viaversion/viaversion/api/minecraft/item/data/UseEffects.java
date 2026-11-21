package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record UseEffects(boolean canSprint, boolean interactVibrations, float speedMultiplier) {
   public static final Type<UseEffects> TYPE = new Type<UseEffects>(UseEffects.class) {
      public UseEffects read(ByteBuf buffer) {
         boolean canSprint = Types.BOOLEAN.read(buffer);
         boolean interactVibrations = Types.BOOLEAN.read(buffer);
         float speedMultiplier = Types.FLOAT.readPrimitive(buffer);
         return new UseEffects(canSprint, interactVibrations, speedMultiplier);
      }

      public void write(ByteBuf buffer, UseEffects value) {
         Types.BOOLEAN.write(buffer, value.canSprint());
         Types.BOOLEAN.write(buffer, value.interactVibrations());
         Types.FLOAT.writePrimitive(buffer, value.speedMultiplier());
      }

      public void write(Ops ops, UseEffects value) {
         ops.writeMap(
            map -> map.writeOptional("can_sprint", Types.BOOLEAN, value.canSprint, false)
               .writeOptional("interact_vibrations", Types.BOOLEAN, value.interactVibrations, true)
               .writeOptional("speed_multiplier", Types.FLOAT, value.speedMultiplier, 0.2F)
         );
      }
   };
}
