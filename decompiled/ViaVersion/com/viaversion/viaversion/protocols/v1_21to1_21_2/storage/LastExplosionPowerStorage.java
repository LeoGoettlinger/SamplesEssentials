package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

public final class LastExplosionPowerStorage {
   private float power;
   private int affectedBlocks;

   public float power() {
      return this.power;
   }

   public void setPower(float power) {
      this.power = power;
   }

   public int affectedBlocks() {
      return this.affectedBlocks;
   }

   public void setAffectedBlocks(int affectedBlocks) {
      this.affectedBlocks = affectedBlocks;
   }
}
