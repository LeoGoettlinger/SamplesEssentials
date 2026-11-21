package com.viaversion.viaversion.protocols.v1_15_2to1_16.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_16 extends ProtocolStorablesBase {
   private final InventoryTracker1_16 inventoryTracker = new InventoryTracker1_16();

   public InventoryTracker1_16 inventoryTracker() {
      return this.inventoryTracker;
   }
}
