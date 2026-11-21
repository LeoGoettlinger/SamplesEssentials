package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.SpellParticleOption;

public final class SpellParticleType implements ParticleType<SpellParticleOption> {
   public void write(SpellParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(data.color());
      buf.writeFloat(data.power());
   }

   public SpellParticleOption read(FriendlyByteBuf buf) {
      return new SpellParticleOption(buf.readInt(), buf.readFloat());
   }
}
