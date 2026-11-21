package com.github.retrooper.packetevents.protocol.entity.sulfurcube;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SulfurCubeKnockbackModifiers {
   public static final NbtCodec<SulfurCubeKnockbackModifiers> CODEC = (new NbtMapCodec<SulfurCubeKnockbackModifiers>() {
      public SulfurCubeKnockbackModifiers decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         float horizontalPower = tag.getNumberTagValueOrThrow("horizontal_power").floatValue();
         float verticalPower = tag.getNumberTagValueOrThrow("vertical_power").floatValue();
         return new SulfurCubeKnockbackModifiers(horizontalPower, verticalPower);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, SulfurCubeKnockbackModifiers value) throws NbtCodecException {
         tag.setTag("horizontal_power", new NBTFloat(value.horizontalPower));
         tag.setTag("vertical_power", new NBTFloat(value.verticalPower));
      }
   }).codec();
   private final float horizontalPower;
   private final float verticalPower;

   public SulfurCubeKnockbackModifiers(float horizontalPower, float verticalPower) {
      this.horizontalPower = horizontalPower;
      this.verticalPower = verticalPower;
   }

   public float getHorizontalPower() {
      return this.horizontalPower;
   }

   public float getVerticalPower() {
      return this.verticalPower;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         SulfurCubeKnockbackModifiers that = (SulfurCubeKnockbackModifiers)obj;
         return Float.compare(that.horizontalPower, this.horizontalPower) != 0 ? false : Float.compare(that.verticalPower, this.verticalPower) == 0;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.horizontalPower, this.verticalPower);
   }
}
