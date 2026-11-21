package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.GeyserParticleOption;

public final class GeyserParticleType implements ParticleType<GeyserParticleOption> {
   public void write(GeyserParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.waterBlocks());
   }

   public GeyserParticleOption read(FriendlyByteBuf buf) {
      return new GeyserParticleOption(buf.readInt());
   }
}
