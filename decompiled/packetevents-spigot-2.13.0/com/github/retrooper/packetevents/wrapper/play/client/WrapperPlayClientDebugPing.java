package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientDebugPing extends PacketWrapper<WrapperPlayClientDebugPing> {
   private long timestamp;

   public WrapperPlayClientDebugPing(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientDebugPing(long timestamp) {
      super(PacketType.Play.Client.DEBUG_PING);
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

   public void copy(WrapperPlayClientDebugPing wrapper) {
      this.timestamp = wrapper.timestamp;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }
}
