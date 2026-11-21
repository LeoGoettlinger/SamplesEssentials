package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.DustParticleOption;
import com.artillexstudios.axtrade.libs.axapi.utils.Colors;

public final class DustParticleType implements ParticleType<DustParticleOption> {
   public void write(DustParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(Colors.fromVector(data.color()));
      buf.writeFloat(data.scale());
   }

   public DustParticleOption read(FriendlyByteBuf buf) {
      return new DustParticleOption(Colors.toVector(buf.readInt()), buf.readFloat());
   }
}
