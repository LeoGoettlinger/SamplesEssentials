package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage;

public final class PlayerRotationStorage {
   private float yaw;
   private float pitch;

   public void setRotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }
}
