package com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axrankmenu.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketType;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.PacketWrapper;

public final class ClientboundSetTitlesAnimationWrapper extends PacketWrapper {
   private int fadeIn;
   private int stay;
   private int fadeOut;

   public ClientboundSetTitlesAnimationWrapper(int fadeIn, int stay, int fadeOut) {
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
   }

   public ClientboundSetTitlesAnimationWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeInt(this.fadeIn);
      out.writeInt(this.stay);
      out.writeInt(this.fadeOut);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.fadeIn = buf.readInt();
      this.stay = buf.readInt();
      this.fadeOut = buf.readInt();
   }

   @Override
   public PacketType packetType() {
      return ClientboundPacketTypes.SET_TITLE_ANIMATION;
   }
}
