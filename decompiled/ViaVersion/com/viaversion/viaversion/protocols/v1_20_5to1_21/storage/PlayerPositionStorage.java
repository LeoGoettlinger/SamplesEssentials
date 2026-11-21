package com.viaversion.viaversion.protocols.v1_20_5to1_21.storage;

public final class PlayerPositionStorage {
   private double x;
   private double y;
   private double z;
   private boolean onGround;

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }

   public void setPosition(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public boolean onGround() {
      return this.onGround;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
