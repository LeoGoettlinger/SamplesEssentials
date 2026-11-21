package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

public final class InventoryStateIdStorage {
   private boolean smithingTableOpen;
   private int stateId = -1;
   private int nextClearedSlot;

   public int stateId() {
      return this.stateId;
   }

   public void setStateId(int stateId) {
      this.stateId = stateId;
   }

   public int nextClearedSlot() {
      return this.nextClearedSlot;
   }

   public void setNextClearedSlot(int nextClearedSlot) {
      this.nextClearedSlot = nextClearedSlot;
   }

   public boolean smithingTableOpen() {
      return this.smithingTableOpen;
   }

   public void setSmithingTableOpen(boolean smithingTableOpen) {
      this.smithingTableOpen = smithingTableOpen;
   }
}
