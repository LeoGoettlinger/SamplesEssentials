package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

public class BundleStateTracker {
   private boolean bundling;

   public boolean isBundling() {
      return this.bundling;
   }

   public void toggleBundling() {
      this.bundling = !this.bundling;
   }
}
