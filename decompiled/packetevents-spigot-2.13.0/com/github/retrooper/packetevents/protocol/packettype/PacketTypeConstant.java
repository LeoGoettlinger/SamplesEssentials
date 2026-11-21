package com.github.retrooper.packetevents.protocol.packettype;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public interface PacketTypeConstant extends PacketTypeCommon {
   @Override
   default int getId(ClientVersion version) {
      return this.getId();
   }

   int getId();
}
