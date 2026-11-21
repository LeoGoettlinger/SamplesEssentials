package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.IntegerParticleOption;

public final class VarIntParticleType implements ParticleType<IntegerParticleOption> {
   public void write(IntegerParticleOption data, FriendlyByteBuf buf) {
      buf.writeVarInt(data.value());
   }

   public IntegerParticleOption read(FriendlyByteBuf buf) {
      return new IntegerParticleOption(buf.readVarInt());
   }
}
