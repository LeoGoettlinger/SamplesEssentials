package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.FloatParticleOption;

public final class FloatParticleType implements ParticleType<FloatParticleOption> {
   public void write(FloatParticleOption data, FriendlyByteBuf buf) {
      buf.writeFloat(data.value());
   }

   public FloatParticleOption read(FriendlyByteBuf buf) {
      return new FloatParticleOption(buf.readFloat());
   }
}
