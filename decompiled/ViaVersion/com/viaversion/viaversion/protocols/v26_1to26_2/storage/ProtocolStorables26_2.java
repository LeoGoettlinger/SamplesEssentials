package com.viaversion.viaversion.protocols.v26_1to26_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables26_2 extends ProtocolStorablesBase {
   private int fakeEntityId;
   private boolean encrypted;

   public int fakeEntityId() {
      return this.fakeEntityId;
   }

   public void setFakeEntityId(int fakeEntityId) {
      this.fakeEntityId = fakeEntityId;
   }

   public boolean encrypted() {
      return this.encrypted;
   }

   public void setEncrypted(boolean encrypted) {
      this.encrypted = encrypted;
   }
}
