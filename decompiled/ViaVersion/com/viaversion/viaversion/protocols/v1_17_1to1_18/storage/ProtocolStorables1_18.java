package com.viaversion.viaversion.protocols.v1_17_1to1_18.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_18 extends ProtocolStorablesBase {
   private final ChunkLightStorage chunkLightStorage = new ChunkLightStorage();

   public ChunkLightStorage chunkLightStorage() {
      return this.chunkLightStorage;
   }
}
