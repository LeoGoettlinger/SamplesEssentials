package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.ResolvableFloat;
import com.viaversion.viaversion.api.minecraft.ResolvableInt;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record CookingFuel(ResolvableInt burnTime, ResolvableFloat speedMultiplier) {
   public static final Type<CookingFuel> TYPE = new Type<CookingFuel>(CookingFuel.class) {
      public CookingFuel read(ByteBuf buffer) {
         ResolvableInt burnTime = Types.RESOLVABLE_INT.read(buffer);
         ResolvableFloat speedMultiplier = Types.RESOLVABLE_FLOAT.read(buffer);
         return new CookingFuel(burnTime, speedMultiplier);
      }

      public void write(ByteBuf buffer, CookingFuel value) {
         Types.RESOLVABLE_INT.write(buffer, value.burnTime);
         Types.RESOLVABLE_FLOAT.write(buffer, value.speedMultiplier);
      }

      public void write(Ops ops, CookingFuel value) {
         ops.writeMap(
            map -> map.write("burn_time", Types.RESOLVABLE_INT, value.burnTime).write("speed_multiplier", Types.RESOLVABLE_FLOAT, value.speedMultiplier)
         );
      }
   };
}
