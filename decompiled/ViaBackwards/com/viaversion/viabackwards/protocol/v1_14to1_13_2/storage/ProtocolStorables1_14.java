package com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_14 extends ProtocolStorablesBase {
   private final ChunkLightStorage chunkLightStorage = new ChunkLightStorage();
   private final DifficultyStorage difficultyStorage = new DifficultyStorage();

   public ChunkLightStorage chunkLightStorage() {
      return this.chunkLightStorage;
   }

   public DifficultyStorage difficultyStorage() {
      return this.difficultyStorage;
   }
}
