package com.viaversion.viabackwards.protocol.v1_17_1to1_17.storage;

import com.viaversion.viabackwards.protocol.v1_17to1_16_4.storage.PlayerLastCursorItem;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_17_1 extends ProtocolStorablesBase {
   private final InventoryStateIds inventoryStateIds = new InventoryStateIds();
   private final PlayerLastCursorItem playerLastCursorItem = new PlayerLastCursorItem();

   public InventoryStateIds inventoryStateIds() {
      return this.inventoryStateIds;
   }

   public PlayerLastCursorItem playerLastCursorItem() {
      return this.playerLastCursorItem;
   }
}
