package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BlendToGrayArgument {
   public static final NbtCodec<BlendToGrayArgument> CODEC = (new NbtMapCodec<BlendToGrayArgument>() {
      public BlendToGrayArgument decode(NBTCompound compound, PacketWrapper<?> wrapper) throws NbtCodecException {
         float brightness = compound.getNumberTagValueOrThrow("brightness").floatValue();
         float factor = compound.getNumberTagValueOrThrow("factor").floatValue();
         return new BlendToGrayArgument(brightness, factor);
      }

      public void encode(NBTCompound compound, PacketWrapper<?> wrapper, BlendToGrayArgument value) throws NbtCodecException {
         compound.setTag("brightness", new NBTFloat(value.brightness));
         compound.setTag("factor", new NBTFloat(value.factor));
      }
   }).codec();
   private final float brightness;
   private final float factor;

   public BlendToGrayArgument(float brightness, float factor) {
      this.brightness = brightness;
      this.factor = factor;
   }

   public AlphaColor blend(AlphaColor color) {
      AlphaColor scaledGrayscale = color.asGrayscale().scale(this.brightness);
      return color.lerpSrgb(scaledGrayscale, this.factor);
   }

   public Color blend(Color color) {
      Color scaledGrayscale = color.asGrayscale().scale(this.brightness);
      return color.lerpSrgb(scaledGrayscale, this.factor);
   }

   public float getBrightness() {
      return this.brightness;
   }

   public float getFactor() {
      return this.factor;
   }
}
