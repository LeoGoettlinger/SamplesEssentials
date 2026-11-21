package com.artillexstudios.axtrade.libs.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketType;
import com.artillexstudios.axtrade.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axtrade.libs.axapi.packet.wrapper.PacketWrapper;

public final class ServerboundAttackWrapper extends PacketWrapper {
   private int entityId;

   public ServerboundAttackWrapper(PacketEvent event) {
      super(event);
   }

   public ServerboundAttackWrapper(int entityId) {
      this.entityId = entityId;
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
      return ServerboundPacketTypes.ATTACK;
   }
}
