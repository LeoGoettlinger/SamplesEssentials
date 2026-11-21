package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerConfigurationStart extends PacketWrapper<WrapperPlayServerConfigurationStart> {
   public WrapperPlayServerConfigurationStart(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerConfigurationStart() {
      super(PacketType.Play.Server.CONFIGURATION_START);
   }
}
