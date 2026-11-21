package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEntityMovement extends PacketWrapper<WrapperPlayServerEntityMovement> {
   private int entityId;

   public WrapperPlayServerEntityMovement(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEntityMovement(int entityId) {
      super(PacketType.Play.Server.ENTITY_MOVEMENT);
      this.entityId = entityId;
   }

   @Override
   public void read() {
      this.entityId = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityId);
   }

   public void copy(WrapperPlayServerEntityMovement wrapper) {
      this.entityId = wrapper.entityId;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }
}
