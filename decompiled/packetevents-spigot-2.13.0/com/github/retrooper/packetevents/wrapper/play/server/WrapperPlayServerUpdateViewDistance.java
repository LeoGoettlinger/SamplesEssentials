package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerUpdateViewDistance extends PacketWrapper<WrapperPlayServerUpdateViewDistance> {
   private int viewDistance;

   public WrapperPlayServerUpdateViewDistance(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerUpdateViewDistance(int viewDistance) {
      super(PacketType.Play.Server.UPDATE_VIEW_DISTANCE);
      this.viewDistance = viewDistance;
   }

   @Override
   public void read() {
      this.viewDistance = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.viewDistance);
   }

   public void copy(WrapperPlayServerUpdateViewDistance wrapper) {
      this.viewDistance = wrapper.viewDistance;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public void setViewDistance(int viewDistance) {
      this.viewDistance = viewDistance;
   }
}
