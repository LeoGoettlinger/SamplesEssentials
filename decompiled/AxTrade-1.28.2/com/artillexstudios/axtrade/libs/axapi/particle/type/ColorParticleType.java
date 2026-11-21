package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.ColorParticleOption;
import com.artillexstudios.axtrade.libs.axapi.utils.Colors;

public final class ColorParticleType implements ParticleType<ColorParticleOption> {
   public void write(ColorParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(Colors.fromVector(data.color()));
   }

   public ColorParticleOption read(FriendlyByteBuf buf) {
      return new ColorParticleOption(Colors.toVector(buf.readInt()));
   }
}
