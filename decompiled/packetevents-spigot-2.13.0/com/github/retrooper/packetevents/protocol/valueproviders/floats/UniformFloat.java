package com.github.retrooper.packetevents.protocol.valueproviders.floats;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class UniformFloat implements FloatProvider {
   public static final NbtMapCodec<UniformFloat> MAP_CODEC = new NbtMapCodec<UniformFloat>() {
      public UniformFloat decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         float min = tag.getNumberTagValueOrThrow("min_inclusive").floatValue();
         float max = tag.getNumberTagValueOrThrow("max_exclusive").floatValue();
         return new UniformFloat(min, max);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, UniformFloat value) throws NbtCodecException {
         tag.setTag("min_inclusive", new NBTFloat(value.min));
         tag.setTag("max_exclusive", new NBTFloat(value.max));
      }
   };
   private final float min;
   private final float max;

   public UniformFloat(float min, float max) {
      this.min = min;
      this.max = max;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderTypes.UNIFORM;
   }

   @Override
   public float getSample(Random random) {
      return random.nextFloat() * (this.max - this.min) + this.min;
   }

   @Override
   public float getMin() {
      return this.min;
   }

   @Override
   public float getMax() {
      return this.max;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         UniformFloat that = (UniformFloat)obj;
         return Float.compare(that.min, this.min) != 0 ? false : Float.compare(that.max, this.max) == 0;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max);
   }
}
