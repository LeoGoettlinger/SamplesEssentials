package com.viaversion.viabackwards.protocol.v1_17to1_16_4.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_17 extends ProtocolStorablesBase {
   private final PlayerLastCursorItem playerLastCursorItem = new PlayerLastCursorItem();

   public PlayerLastCursorItem playerLastCursorItem() {
      return this.playerLastCursorItem;
   }
}
