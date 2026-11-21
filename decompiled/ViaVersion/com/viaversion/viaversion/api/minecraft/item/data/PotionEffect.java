package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record PotionEffect(int effect, PotionEffectData effectData) {
   public static final Type<PotionEffect> TYPE = new Type<PotionEffect>(PotionEffect.class) {
      public PotionEffect read(ByteBuf buffer) {
         int effect = Types.VAR_INT.readPrimitive(buffer);
         PotionEffectData effectData = PotionEffectData.TYPE.read(buffer);
         return new PotionEffect(effect, effectData);
      }

      public void write(ByteBuf buffer, PotionEffect value) {
         Types.VAR_INT.writePrimitive(buffer, value.effect);
         PotionEffectData.TYPE.write(buffer, value.effectData);
      }

      public void write(Ops ops, PotionEffect value) {
         ops.writeMap(map -> map.write("id", EnumTypes.MOB_EFFECT, value.effect).writeInlinedMap(PotionEffectData.TYPE, value.effectData));
      }
   };
   public static final Type<PotionEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);
}
