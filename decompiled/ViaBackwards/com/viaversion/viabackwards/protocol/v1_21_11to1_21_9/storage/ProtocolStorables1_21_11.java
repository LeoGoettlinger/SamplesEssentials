package com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_21_11 extends ProtocolStorablesBase {
   private final GameTimeStorage gameTimeStorage = new GameTimeStorage();

   public GameTimeStorage gameTimeStorage() {
      return this.gameTimeStorage;
   }
}
