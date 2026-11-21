package com.github.retrooper.packetevents.protocol.mapper;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public interface StaticMappedEntity extends MappedEntity {
   int getId();

   @Override
   default int getId(ClientVersion version) {
      return this.getId();
   }
}
