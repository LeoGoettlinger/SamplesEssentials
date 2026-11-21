package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.DustColorTransitionParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Colors;

public final class DustColorTransitionParticleType implements ParticleType<DustColorTransitionParticleOption> {
   public void write(DustColorTransitionParticleOption data, FriendlyByteBuf buf) {
      buf.writeInt(Colors.fromVector(data.color1()));
      buf.writeInt(Colors.fromVector(data.color2()));
      buf.writeFloat(data.scale());
   }

   public DustColorTransitionParticleOption read(FriendlyByteBuf buf) {
      return new DustColorTransitionParticleOption(Colors.toVector(buf.readInt()), Colors.toVector(buf.readInt()), buf.readFloat());
   }
}
