package com.viaversion.viabackwards.protocol.v1_16to1_15_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public final class WolfDataMaskStorage implements StorableObject {
   private byte tameableMask;

   public WolfDataMaskStorage(byte tameableMask) {
      this.tameableMask = tameableMask;
   }

   public void setTameableMask(byte tameableMask) {
      this.tameableMask = tameableMask;
   }

   public byte tameableMask() {
      return this.tameableMask;
   }
}
