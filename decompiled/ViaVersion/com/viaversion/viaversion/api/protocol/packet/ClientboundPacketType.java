package com.viaversion.viaversion.api.protocol.packet;

public non-sealed interface ClientboundPacketType extends PacketType {
   @Override
   default Direction direction() {
      return Direction.CLIENTBOUND;
   }
}
