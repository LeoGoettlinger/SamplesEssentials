package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.DustParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.Colors;

public final class DustParticleType implements ParticleType<DustParticleOption> {
   public void write(DustParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(Colors.fromVector(data.color()));
      buf.writeFloat(data.scale());
   }

   public DustParticleOption read(FriendlyByteBuf buf) {
      return new DustParticleOption(Colors.toVector(buf.readInt()), buf.readFloat());
   }
}
