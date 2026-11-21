package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import com.github.retrooper.packetevents.util.AlphaFloat;
import com.github.retrooper.packetevents.util.MathUtil;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FloatModifier<A> extends AttributeModifier<Float, A> {
   FloatModifier<AlphaFloat> ALPHA_BLEND = new FloatModifier<AlphaFloat>() {
      public Float apply(Float value, AlphaFloat arg) {
         return MathUtil.lerp(arg.getAlpha(), value, arg.getValue());
      }

      @Override
      public NbtCodec<AlphaFloat> argumentCodec(EnvironmentAttribute<Float> attribute) {
         return AlphaFloat.CODEC;
      }
   };
   FloatModifier<Float> ADD = Float::sum;
   FloatModifier<Float> SUBTRACT = (FloatModifier.Simple)(a, b) -> a - b;
   FloatModifier<Float> MULTIPLY = (FloatModifier.Simple)(a, b) -> a * b;
   FloatModifier<Float> MINIMUM = Math::min;
   FloatModifier<Float> MAXIMUM = Math::max;

   @FunctionalInterface
   public interface Simple extends FloatModifier<Float> {
      @Override
      default NbtCodec<Float> argumentCodec(EnvironmentAttribute<Float> attribute) {
         return NbtCodecs.FLOAT;
      }
   }
}
