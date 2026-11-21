package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_21_11 extends ProtocolStorablesBase {
   private final GameTimeStorage gameTimeStorage = new GameTimeStorage();

   public GameTimeStorage gameTimeStorage() {
      return this.gameTimeStorage;
   }
}
