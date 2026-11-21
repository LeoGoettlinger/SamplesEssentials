package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import net.kyori.adventure.key.Key;

public final class ClientboundCustomPayloadWrapper extends PacketWrapper {
   private Key identifier;
   private FriendlyByteBuf data;

   public ClientboundCustomPayloadWrapper(Key identifier, FriendlyByteBuf data) {
      this.identifier = identifier;
      this.data = data;
   }

   public ClientboundCustomPayloadWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeResourceLocation(this.identifier);
      out.writeBytes(this.data.copy());
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.identifier = buf.readResourceLocation();
      int i = buf.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = buf.readBytes(i);
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   public FriendlyByteBuf data() {
      return this.data;
   }

   @Override
   public PacketType packetType() {
      return ClientboundPacketTypes.CUSTOM_PAYLOAD;
   }
}
