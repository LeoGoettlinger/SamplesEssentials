package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AlphaColorModifier<A> extends AttributeModifier<AlphaColor, A> {
   AlphaColorModifier<AlphaColor> ALPHA_BLEND = Color::blendWith;
   AlphaColorModifier<Color> ADD = AlphaColor::plus;
   AlphaColorModifier<Color> SUBTRACT = AlphaColor::minus;
   AlphaColorModifier<AlphaColor> MULTIPLY = AlphaColor::times;
   AlphaColorModifier<BlendToGrayArgument> BLEND_TO_GRAY = new AlphaColorModifier<BlendToGrayArgument>() {
      public AlphaColor apply(AlphaColor value, BlendToGrayArgument arg) {
         return arg.blend(value);
      }

      @Override
      public NbtCodec<BlendToGrayArgument> argumentCodec(EnvironmentAttribute<AlphaColor> attribute) {
         return BlendToGrayArgument.CODEC;
      }
   };

   @FunctionalInterface
   public interface ArgbModifier extends AlphaColorModifier<AlphaColor> {
      @Override
      default NbtCodec<AlphaColor> argumentCodec(EnvironmentAttribute<AlphaColor> attribute) {
         return NbtCodecs.ARGB_COLOR;
      }
   }

   @FunctionalInterface
   public interface RgbModifier extends AlphaColorModifier<Color> {
      @Override
      default NbtCodec<Color> argumentCodec(EnvironmentAttribute<AlphaColor> attribute) {
         return NbtCodecs.RGB_COLOR;
      }
   }
}
