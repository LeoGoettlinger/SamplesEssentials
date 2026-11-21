package com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axrankmenu.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketType;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public final class ClientboundSetTitleTextWrapper extends PacketWrapper {
   private Component text;

   public ClientboundSetTitleTextWrapper(Component text) {
      this.text = text;
   }

   public ClientboundSetTitleTextWrapper(PacketEvent event) {
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
      return ClientboundPacketTypes.SET_TITLE_TEXT;
   }
}
