package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperConfigServerResetChat extends PacketWrapper<WrapperConfigServerResetChat> {
   public WrapperConfigServerResetChat(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerResetChat() {
      super(PacketType.Configuration.Server.RESET_CHAT);
   }
}
