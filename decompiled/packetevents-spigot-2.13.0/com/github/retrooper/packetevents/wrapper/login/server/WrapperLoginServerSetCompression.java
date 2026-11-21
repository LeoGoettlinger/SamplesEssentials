package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperLoginServerSetCompression extends PacketWrapper<WrapperLoginServerSetCompression> {
   private int threshold;

   public WrapperLoginServerSetCompression(PacketSendEvent event) {
      super(event);
   }

   public WrapperLoginServerSetCompression(int threshold) {
      super(PacketType.Login.Server.SET_COMPRESSION);
      this.threshold = threshold;
   }

   @Override
   public void read() {
      this.threshold = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.threshold);
   }

   public void copy(WrapperLoginServerSetCompression wrapper) {
      this.threshold = wrapper.threshold;
   }

   public int getThreshold() {
      return this.threshold;
   }

   public void setThreshold(int threshold) {
      this.threshold = threshold;
   }
}
