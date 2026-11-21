package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

public final class GroundFlagTracker {
   private boolean onGround;
   private boolean horizontalCollision;

   public boolean onGround() {
      return this.onGround;
   }

   public boolean setOnGround(boolean onGround) {
      return this.onGround = onGround;
   }

   public boolean horizontalCollision() {
      return this.horizontalCollision;
   }

   public void setHorizontalCollision(boolean horizontalCollision) {
      this.horizontalCollision = horizontalCollision;
   }
}
