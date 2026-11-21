package com.github.retrooper.packetevents.wrapper.login.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperLoginClientLoginSuccessAck extends PacketWrapper<WrapperLoginClientLoginSuccessAck> {
   public WrapperLoginClientLoginSuccessAck(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperLoginClientLoginSuccessAck() {
      super(PacketType.Login.Client.LOGIN_SUCCESS_ACK);
   }
}
