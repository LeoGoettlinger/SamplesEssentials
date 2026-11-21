package com.viaversion.viabackwards.protocol.v1_16_4to1_16_3.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_16_4 extends ProtocolStorablesBase {
   private final PlayerHandStorage playerHandStorage = new PlayerHandStorage();

   public PlayerHandStorage playerHandStorage() {
      return this.playerHandStorage;
   }
}
