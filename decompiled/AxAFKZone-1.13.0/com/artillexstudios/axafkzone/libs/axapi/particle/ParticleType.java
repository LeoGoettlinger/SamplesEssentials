package com.artillexstudios.axafkzone.libs.axapi.particle;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;

public interface ParticleType<T extends ParticleOption> {
   void write(T data, FriendlyByteBuf buf);

   T read(FriendlyByteBuf buf);
}
