package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperConfigClientConfigurationEndAck extends PacketWrapper<WrapperConfigClientConfigurationEndAck> {
   public WrapperConfigClientConfigurationEndAck(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientConfigurationEndAck() {
      super(PacketType.Configuration.Client.CONFIGURATION_END_ACK);
   }
}
