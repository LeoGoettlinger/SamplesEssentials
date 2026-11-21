package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperConfigServerConfigurationEnd extends PacketWrapper<WrapperConfigServerConfigurationEnd> {
   public WrapperConfigServerConfigurationEnd(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerConfigurationEnd() {
      super(PacketType.Configuration.Server.CONFIGURATION_END);
   }
}
