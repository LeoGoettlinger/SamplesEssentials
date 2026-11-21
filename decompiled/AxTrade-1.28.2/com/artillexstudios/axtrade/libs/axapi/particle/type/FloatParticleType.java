package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.FloatParticleOption;

public final class FloatParticleType implements ParticleType<FloatParticleOption> {
   public void write(FloatParticleOption data, FriendlyByteBuf buf) {
      buf.writeFloat(data.value());
   }

   public FloatParticleOption read(FriendlyByteBuf buf) {
      return new FloatParticleOption(buf.readFloat());
   }
}
