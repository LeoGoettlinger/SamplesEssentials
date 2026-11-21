package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerDebugPong extends PacketWrapper<WrapperPlayServerDebugPong> {
   private long timestamp;

   public WrapperPlayServerDebugPong(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerDebugPong(long timestamp) {
      super(PacketType.Play.Server.DEBUG_PONG);
      this.timestamp = timestamp;
   }

   @Override
   public void read() {
      this.timestamp = this.readLong();
   }

   @Override
   public void write() {
      this.writeLong(this.timestamp);
   }

   public void copy(WrapperPlayServerDebugPong wrapper) {
      this.timestamp = wrapper.timestamp;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }
}
