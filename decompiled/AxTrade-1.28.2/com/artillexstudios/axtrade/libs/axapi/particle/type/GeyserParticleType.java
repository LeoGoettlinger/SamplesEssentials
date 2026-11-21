package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.GeyserParticleOption;

public final class GeyserParticleType implements ParticleType<GeyserParticleOption> {
   public void write(GeyserParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.waterBlocks());
   }

   public GeyserParticleOption read(FriendlyByteBuf buf) {
      return new GeyserParticleOption(buf.readInt());
   }
}
