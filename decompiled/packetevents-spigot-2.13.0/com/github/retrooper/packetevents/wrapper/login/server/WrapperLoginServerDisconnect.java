package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public class WrapperLoginServerDisconnect extends PacketWrapper<WrapperLoginServerDisconnect> {
   private Component reason;

   public WrapperLoginServerDisconnect(PacketSendEvent event) {
      super(event);
   }

   public WrapperLoginServerDisconnect(Component reason) {
      super(PacketType.Login.Server.DISCONNECT);
      this.reason = reason;
   }

   @Override
   public void read() {
      this.reason = this.readComponentAsJSON();
   }

   @Override
   public void write() {
      this.writeComponentAsJSON(this.reason);
   }

   public void copy(WrapperLoginServerDisconnect wrapper) {
      this.reason = wrapper.reason;
   }

   public Component getReason() {
      return this.reason;
   }

   public void setReason(Component reason) {
      this.reason = reason;
   }
}
