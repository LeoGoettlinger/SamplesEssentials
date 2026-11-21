package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerHurtAnimation extends PacketWrapper<WrapperPlayServerHurtAnimation> {
   private int entityId;
   private float yaw;

   public WrapperPlayServerHurtAnimation(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerHurtAnimation(int entityId, float yaw) {
      super(PacketType.Play.Server.HURT_ANIMATION);
      this.entityId = entityId;
      this.yaw = yaw;
   }

   @Override
   public void read() {
      this.entityId = this.readVarInt();
      this.yaw = this.readFloat();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityId);
      this.writeFloat(this.yaw);
   }

   public void copy(WrapperPlayServerHurtAnimation wrapper) {
      this.entityId = wrapper.entityId;
      this.yaw = wrapper.yaw;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
