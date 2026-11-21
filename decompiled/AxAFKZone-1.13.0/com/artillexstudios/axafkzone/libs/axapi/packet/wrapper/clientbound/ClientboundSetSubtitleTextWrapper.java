package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public final class ClientboundSetSubtitleTextWrapper extends PacketWrapper {
   private Component text;

   public ClientboundSetSubtitleTextWrapper(Component text) {
      this.text = text;
   }

   public ClientboundSetSubtitleTextWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeComponent(this.text);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.text = buf.readComponent();
   }

   @Override
   public PacketType packetType() {
      return ClientboundPacketTypes.SET_SUBTITLE;
   }
}
