package com.artillexstudios.axrankmenu.libs.axapi.particle.type;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.ItemStackParticleOption;

public final class ItemStackParticleType implements ParticleType<ItemStackParticleOption> {
   public void write(ItemStackParticleOption data, FriendlyByteBuf buf) {
      buf.writeItemStack(data.stack());
   }

   public ItemStackParticleOption read(FriendlyByteBuf buf) {
      return new ItemStackParticleOption(buf.readItemStack());
   }
}
