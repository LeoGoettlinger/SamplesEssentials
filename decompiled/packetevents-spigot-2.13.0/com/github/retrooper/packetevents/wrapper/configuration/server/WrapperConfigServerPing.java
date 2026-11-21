package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerPing;

public class WrapperConfigServerPing extends WrapperCommonServerPing<WrapperConfigServerPing> {
   public WrapperConfigServerPing(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerPing(int id) {
      super(PacketType.Configuration.Server.PING, id);
   }
}
