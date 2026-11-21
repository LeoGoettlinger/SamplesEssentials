package com.viaversion.viaversion.protocols.v1_18_2to1_19.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public final class LoomStorage implements StorableObject {
   private int containerId = -1;

   public int containerId() {
      return this.containerId;
   }

   public void setContainerId(int containerId) {
      this.containerId = containerId;
   }

   public void reset() {
      this.containerId = -1;
   }
}
