package com.github.retrooper.packetevents.wrapper.status.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperStatusServerPong extends PacketWrapper<WrapperStatusServerPong> {
   private long time;

   public WrapperStatusServerPong(PacketSendEvent event) {
      super(event);
   }

   public WrapperStatusServerPong(long time) {
      super(PacketType.Status.Server.PONG);
      this.time = time;
   }

   @Override
   public void read() {
      this.time = this.readLong();
   }

   @Override
   public void write() {
      this.writeLong(this.time);
   }

   public void copy(WrapperStatusServerPong wrapper) {
      this.time = wrapper.time;
   }

   public long getTime() {
      return this.time;
   }

   public void setTime(long time) {
      this.time = time;
   }
}
