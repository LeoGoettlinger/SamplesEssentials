package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record AttackRange(float minRange, float maxRange, float minCreativeRange, float maxCreativeRange, float hitboxMargin, float mobFactor) {
   public static final Type<AttackRange> TYPE = new Type<AttackRange>(AttackRange.class) {
      public AttackRange read(ByteBuf buffer) {
         float minRange = Types.FLOAT.readPrimitive(buffer);
         float maxRange = Types.FLOAT.readPrimitive(buffer);
         float minCreativeRange = Types.FLOAT.readPrimitive(buffer);
         float maxCreativeRange = Types.FLOAT.readPrimitive(buffer);
         float hitboxMargin = Types.FLOAT.readPrimitive(buffer);
         float mobFactor = Types.FLOAT.readPrimitive(buffer);
         return new AttackRange(minRange, maxRange, minCreativeRange, maxCreativeRange, hitboxMargin, mobFactor);
      }

      public void write(ByteBuf buffer, AttackRange value) {
         Types.FLOAT.writePrimitive(buffer, value.minRange);
         Types.FLOAT.writePrimitive(buffer, value.maxRange);
         Types.FLOAT.writePrimitive(buffer, value.minCreativeRange);
         Types.FLOAT.writePrimitive(buffer, value.maxCreativeRange);
         Types.FLOAT.writePrimitive(buffer, value.hitboxMargin);
         Types.FLOAT.writePrimitive(buffer, value.mobFactor);
      }

      public void write(Ops ops, AttackRange AttackRange) {
         ops.writeMap(
            map -> map.writeOptional("min_reach", Types.FLOAT, AttackRange.minRange, 0.0F)
               .writeOptional("max_reach", Types.FLOAT, AttackRange.maxRange, 3.0F)
               .writeOptional("min_creative_reach", Types.FLOAT, AttackRange.minCreativeRange, 0.0F)
               .writeOptional("max_creative_reach", Types.FLOAT, AttackRange.maxCreativeRange, 5.0F)
               .writeOptional("hitbox_margin", Types.FLOAT, AttackRange.hitboxMargin, 0.3F)
               .writeOptional("mob_factor", Types.FLOAT, AttackRange.mobFactor, 1.0F)
         );
      }
   };
}
