package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.VibrationParticleOption;

public final class VibrationParticleType implements ParticleType<VibrationParticleOption> {
   public void write(VibrationParticleOption data, FriendlyByteBuf buf) {
      buf.writeVarInt(data.source());
      buf.writeBlockPos(data.position());
      buf.writeVarInt(data.entityId());
      buf.writeFloat(data.eyeHeight());
      buf.writeVarInt(data.ticks());
   }

   public VibrationParticleOption read(FriendlyByteBuf buf) {
      return new VibrationParticleOption(buf.readVarInt(), buf.readBlockPosition(), buf.readVarInt(), buf.readFloat(), buf.readVarInt());
   }
}
