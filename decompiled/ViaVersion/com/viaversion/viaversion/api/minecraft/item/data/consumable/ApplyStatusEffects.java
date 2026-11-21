package com.viaversion.viaversion.api.minecraft.item.data.consumable;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record ApplyStatusEffects(PotionEffect[] effects, float probability) {
   public static final Type<ApplyStatusEffects> TYPE = new Type<ApplyStatusEffects>(ApplyStatusEffects.class) {
      public ApplyStatusEffects read(ByteBuf buffer) {
         PotionEffect[] effects = PotionEffect.ARRAY_TYPE.read(buffer);
         float probability = buffer.readFloat();
         return new ApplyStatusEffects(effects, probability);
      }

      public void write(ByteBuf buffer, ApplyStatusEffects value) {
         PotionEffect.ARRAY_TYPE.write(buffer, value.effects);
         buffer.writeFloat(value.probability);
      }

      public void write(Ops ops, ApplyStatusEffects value) {
         ops.writeMap(map -> map.write("effects", PotionEffect.ARRAY_TYPE, value.effects).writeOptional("probability", Types.FLOAT, value.probability, 1.0F));
      }
   };
}
