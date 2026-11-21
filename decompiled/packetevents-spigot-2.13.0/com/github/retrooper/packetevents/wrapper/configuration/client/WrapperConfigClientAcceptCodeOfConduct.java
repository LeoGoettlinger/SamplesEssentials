package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperConfigClientAcceptCodeOfConduct extends PacketWrapper<WrapperConfigClientAcceptCodeOfConduct> {
   public WrapperConfigClientAcceptCodeOfConduct(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientAcceptCodeOfConduct() {
      super(PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT);
   }
}
