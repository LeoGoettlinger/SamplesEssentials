package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;

public final class ClientboundClearTitlesWrapper extends PacketWrapper {
   private boolean reset;

   public ClientboundClearTitlesWrapper(boolean reset) {
      this.reset = reset;
   }

   public ClientboundClearTitlesWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeBoolean(this.reset);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.reset = buf.readBoolean();
   }

   @Override
   public PacketType packetType() {
      return ClientboundPacketTypes.CLEAR_TITLES;
   }
}
