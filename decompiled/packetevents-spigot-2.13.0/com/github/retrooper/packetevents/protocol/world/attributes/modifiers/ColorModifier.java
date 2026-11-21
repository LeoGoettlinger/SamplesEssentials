package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ColorModifier<A> extends AttributeModifier<Color, A> {
   ColorModifier<AlphaColor> ALPHA_BLEND = Color::blendWith;
   ColorModifier<Color> ADD = Color::plus;
   ColorModifier<Color> SUBTRACT = Color::minus;
   ColorModifier<Color> MULTIPLY = Color::times;
   ColorModifier<BlendToGrayArgument> BLEND_TO_GRAY = new ColorModifier<BlendToGrayArgument>() {
      public Color apply(Color value, BlendToGrayArgument arg) {
         return arg.blend(value);
      }

      @Override
      public NbtCodec<BlendToGrayArgument> argumentCodec(EnvironmentAttribute<Color> attribute) {
         return BlendToGrayArgument.CODEC;
      }
   };

   @FunctionalInterface
   public interface ArgbModifier extends ColorModifier<AlphaColor> {
      @Override
      default NbtCodec<AlphaColor> argumentCodec(EnvironmentAttribute<Color> attribute) {
         return NbtCodecs.ARGB_COLOR;
      }
   }

   @FunctionalInterface
   public interface RgbModifier extends ColorModifier<Color> {
      @Override
      default NbtCodec<Color> argumentCodec(EnvironmentAttribute<Color> attribute) {
         return NbtCodecs.RGB_COLOR;
      }
   }
}
