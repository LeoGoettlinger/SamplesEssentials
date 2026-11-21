package com.artillexstudios.axafkzone.libs.axapi.particle.type;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleType;
import com.artillexstudios.axafkzone.libs.axapi.particle.option.ItemStackParticleOption;

public final class ItemStackParticleType implements ParticleType<ItemStackParticleOption> {
   public void write(ItemStackParticleOption data, FriendlyByteBuf buf) {
      buf.writeItemStack(data.stack());
   }

   public ItemStackParticleOption read(FriendlyByteBuf buf) {
      return new ItemStackParticleOption(buf.readItemStack());
   }
}
