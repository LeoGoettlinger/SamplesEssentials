package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEnterCombatEvent extends PacketWrapper<WrapperPlayServerEnterCombatEvent> {
   public WrapperPlayServerEnterCombatEvent(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEnterCombatEvent() {
      super(PacketType.Play.Server.ENTER_COMBAT_EVENT);
   }

   @Override
   public void read() {
   }

   @Override
   public void write() {
   }

   public void copy(WrapperPlayServerEnterCombatEvent wrapper) {
   }
}
