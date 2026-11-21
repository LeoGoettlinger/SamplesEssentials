package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;

public final class SimpleParticleType implements ParticleType<ParticleOption> {
   @Override
   public void write(ParticleOption data, FriendlyByteBuf buf) {
   }

   @Override
   public ParticleOption read(FriendlyByteBuf buf) {
      return null;
   }
}
