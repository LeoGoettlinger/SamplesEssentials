package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public class WrapperConfigServerDisconnect extends PacketWrapper<WrapperConfigServerDisconnect> {
   private Component reason;

   public WrapperConfigServerDisconnect(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerDisconnect(Component reason) {
      super(PacketType.Configuration.Server.DISCONNECT);
      this.reason = reason;
   }

   @Override
   public void read() {
      this.reason = this.readComponent();
   }

   @Override
   public void write() {
      this.writeComponent(this.reason);
   }

   public void copy(WrapperConfigServerDisconnect wrapper) {
      this.reason = wrapper.reason;
   }

   public Component getReason() {
      return this.reason;
   }

   public void setReason(Component reason) {
      this.reason = reason;
   }
}
