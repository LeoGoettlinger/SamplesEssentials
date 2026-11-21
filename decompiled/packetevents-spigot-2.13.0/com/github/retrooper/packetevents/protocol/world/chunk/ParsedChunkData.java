package com.github.retrooper.packetevents.protocol.world.chunk;

@Deprecated
public class ParsedChunkData {
   BaseChunk[] chunks;

   public ParsedChunkData(BaseChunk[] chunks) {
      this.chunks = chunks;
   }
}
