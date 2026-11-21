package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientPlayerLoaded extends PacketWrapper<WrapperPlayClientPlayerLoaded> {
   public WrapperPlayClientPlayerLoaded(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientPlayerLoaded() {
      super(PacketType.Play.Client.PLAYER_LOADED);
   }
}
