package com.artillexstudios.axtrade.libs.axapi.particle.type;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleType;
import com.artillexstudios.axtrade.libs.axapi.particle.option.IntegerParticleOption;

public final class IntegerParticleType implements ParticleType<IntegerParticleOption> {
   public void write(IntegerParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.value());
   }

   public IntegerParticleOption read(FriendlyByteBuf buf) {
      return new IntegerParticleOption(buf.readInt());
   }
}
