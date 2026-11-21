package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.valueproviders.floats;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TrapezoidFloat implements FloatProvider {
   public static final NbtMapCodec<TrapezoidFloat> MAP_CODEC = new NbtMapCodec<TrapezoidFloat>() {
      public TrapezoidFloat decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         float min = tag.getNumberTagValueOrThrow("min").floatValue();
         float max = tag.getNumberTagValueOrThrow("max").floatValue();
         float plateau = tag.getNumberTagValueOrThrow("plateau").floatValue();
         return new TrapezoidFloat(min, max, plateau);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, TrapezoidFloat value) throws NbtCodecException {
         tag.setTag("min", new NBTFloat(value.min));
         tag.setTag("max", new NBTFloat(value.max));
         tag.setTag("plateau", new NBTFloat(value.plateau));
      }
   };
   private final float min;
   private final float max;
   private final float plateau;

   public TrapezoidFloat(float min, float max, float plateau) {
      this.min = min;
      this.max = max;
      this.plateau = plateau;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderTypes.TRAPEZOID;
   }

   @Override
   public float getSample(Random random) {
      float range = this.max - this.min;
      float plateauStart = (range - this.plateau) / 2.0F;
      float plateauEnd = range - plateauStart;
      return this.min + random.nextFloat() * plateauEnd + random.nextFloat() * plateauStart;
   }

   @Override
   public float getMin() {
      return this.min;
   }

   @Override
   public float getMax() {
      return this.max;
   }

   public float getPlateau() {
      return this.plateau;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         TrapezoidFloat that = (TrapezoidFloat)obj;
         if (Float.compare(that.min, this.min) != 0) {
            return false;
         } else {
            return Float.compare(that.max, this.max) != 0 ? false : Float.compare(that.plateau, this.plateau) == 0;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max, this.plateau);
   }
}
