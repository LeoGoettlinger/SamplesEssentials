package com.viaversion.viaversion.api.protocol.packet;

public sealed interface PacketType permits ClientboundPacketType, ServerboundPacketType {
   int getId();

   String getName();

   Direction direction();

   default State state() {
      return State.PLAY;
   }
}
