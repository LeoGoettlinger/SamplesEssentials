package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerChunkBatchBegin extends PacketWrapper<WrapperPlayServerChunkBatchBegin> {
   public WrapperPlayServerChunkBatchBegin(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerChunkBatchBegin() {
      super(PacketType.Play.Server.CHUNK_BATCH_BEGIN);
   }
}
