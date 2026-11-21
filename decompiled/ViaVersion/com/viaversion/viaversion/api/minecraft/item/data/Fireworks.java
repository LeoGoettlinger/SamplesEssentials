package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record Fireworks(int flightDuration, FireworkExplosion[] explosions) implements Copyable {
   public static final Type<Fireworks> TYPE = new Type<Fireworks>(Fireworks.class) {
      public Fireworks read(ByteBuf buffer) {
         int flightDuration = Types.VAR_INT.readPrimitive(buffer);
         FireworkExplosion[] explosions = FireworkExplosion.ARRAY_TYPE.read(buffer);
         return new Fireworks(flightDuration, explosions);
      }

      public void write(ByteBuf buffer, Fireworks value) {
         Types.VAR_INT.writePrimitive(buffer, value.flightDuration);
         FireworkExplosion.ARRAY_TYPE.write(buffer, value.explosions);
      }

      public void write(Ops ops, Fireworks value) {
         ops.writeMap(
            map -> map.writeOptional("flight_duration", Types.UNSIGNED_BYTE, (short)value.flightDuration, (short)0)
               .writeOptional("explosions", FireworkExplosion.ARRAY_TYPE, value.explosions, new FireworkExplosion[0])
         );
      }
   };

   public Fireworks copy() {
      return new Fireworks(this.flightDuration, Copyable.copy(this.explosions));
   }
}
