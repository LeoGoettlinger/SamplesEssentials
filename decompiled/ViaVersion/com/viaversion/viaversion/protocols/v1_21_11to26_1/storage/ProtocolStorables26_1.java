package com.viaversion.viaversion.protocols.v1_21_11to26_1.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables26_1 extends ProtocolStorablesBase {
   private boolean sneaking;
   private boolean tagsSent;

   public boolean sneaking() {
      return this.sneaking;
   }

   public void setSneaking(boolean sneaking) {
      this.sneaking = sneaking;
   }

   public boolean tagsSent() {
      return this.tagsSent;
   }

   public void setTagsSent(boolean tagsSent) {
      this.tagsSent = tagsSent;
   }
}
