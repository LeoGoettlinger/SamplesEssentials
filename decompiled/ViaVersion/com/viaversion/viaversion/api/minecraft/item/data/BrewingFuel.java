package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.ResolvableFloat;
import com.viaversion.viaversion.api.minecraft.ResolvableInt;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record BrewingFuel(ResolvableInt uses, ResolvableFloat speedMultiplier) {
   public static final Type<BrewingFuel> TYPE = new Type<BrewingFuel>(BrewingFuel.class) {
      public BrewingFuel read(ByteBuf buffer) {
         ResolvableInt uses = Types.RESOLVABLE_INT.read(buffer);
         ResolvableFloat speedMultiplier = Types.RESOLVABLE_FLOAT.read(buffer);
         return new BrewingFuel(uses, speedMultiplier);
      }

      public void write(ByteBuf buffer, BrewingFuel value) {
         Types.RESOLVABLE_INT.write(buffer, value.uses);
         Types.RESOLVABLE_FLOAT.write(buffer, value.speedMultiplier);
      }

      public void write(Ops ops, BrewingFuel value) {
         ops.writeMap(map -> map.write("uses", Types.RESOLVABLE_INT, value.uses).write("speed_multiplier", Types.RESOLVABLE_FLOAT, value.speedMultiplier));
      }
   };
}
