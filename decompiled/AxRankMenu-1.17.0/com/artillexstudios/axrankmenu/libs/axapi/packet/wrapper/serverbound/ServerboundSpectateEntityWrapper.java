package com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketType;
import com.artillexstudios.axrankmenu.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.PacketWrapper;

public final class ServerboundSpectateEntityWrapper extends PacketWrapper {
   private int entityId;

   public ServerboundSpectateEntityWrapper(int entityId) {
      this.entityId = entityId;
   }

   public ServerboundSpectateEntityWrapper(PacketEvent event) {
      super(event);
   }

   public int entityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeVarInt(this.entityId);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.entityId = buf.readVarInt();
   }

   @Override
   public PacketType packetType() {
      return ServerboundPacketTypes.SPECTATE_ENTITY;
   }
}
