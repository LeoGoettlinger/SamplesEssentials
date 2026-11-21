package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.IntegerParticleOption;

public final class IntegerParticleType implements ParticleType<IntegerParticleOption> {
   public void write(IntegerParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.value());
   }

   public IntegerParticleOption read(FriendlyByteBuf buf) {
      return new IntegerParticleOption(buf.readInt());
   }
}
