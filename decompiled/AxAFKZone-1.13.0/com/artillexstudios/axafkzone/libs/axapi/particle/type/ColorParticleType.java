package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.ColorParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.Colors;

public final class ColorParticleType implements ParticleType<ColorParticleOption> {
   public void write(ColorParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(Colors.fromVector(data.color()));
   }

   public ColorParticleOption read(FriendlyByteBuf buf) {
      return new ColorParticleOption(Colors.toVector(buf.readInt()));
   }
}
