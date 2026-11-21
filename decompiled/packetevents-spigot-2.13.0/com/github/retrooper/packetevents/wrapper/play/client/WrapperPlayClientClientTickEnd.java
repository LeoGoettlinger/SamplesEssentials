package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientClientTickEnd extends PacketWrapper<WrapperPlayClientClientTickEnd> {
   public WrapperPlayClientClientTickEnd(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientClientTickEnd() {
      super(PacketType.Play.Client.CLIENT_TICK_END);
   }
}
