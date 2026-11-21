package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientConfigurationAck extends PacketWrapper<WrapperPlayClientConfigurationAck> {
   public WrapperPlayClientConfigurationAck(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientConfigurationAck() {
      super(PacketType.Play.Client.CONFIGURATION_ACK);
   }
}
