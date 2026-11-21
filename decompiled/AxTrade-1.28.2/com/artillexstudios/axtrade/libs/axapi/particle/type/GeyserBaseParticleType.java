package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.GeyserBaseParticleOption;

public final class GeyserBaseParticleType implements ParticleType<GeyserBaseParticleOption> {
   public void write(GeyserBaseParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.waterBlocks());
      buf.writeFloat(data.burstImpulseBase());
   }

   public GeyserBaseParticleOption read(FriendlyByteBuf buf) {
      return new GeyserBaseParticleOption(buf.readInt(), buf.readFloat());
   }
}
