package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.TrailParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.Colors;

public final class TrailParticleType implements ParticleType<TrailParticleOption> {
   public void write(TrailParticleOption data, FriendlyByteBuf buf) {
      buf.writeDouble(data.x());
      buf.writeDouble(data.y());
      buf.writeDouble(data.z());
      buf.writeInt(Colors.fromVector(data.color()));
      buf.writeVarInt(data.duration());
   }

   public TrailParticleOption read(FriendlyByteBuf buf) {
      return new TrailParticleOption(buf.readDouble(), buf.readDouble(), buf.readDouble(), Colors.toVector(buf.readInt()), buf.readVarInt());
   }
}
