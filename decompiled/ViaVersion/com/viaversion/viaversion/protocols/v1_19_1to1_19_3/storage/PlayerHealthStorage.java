package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public final class PlayerHealthStorage implements StorableObject {
   private boolean dead;
   private int food;
   private float saturation;

   public void update(float health, int food, float saturation) {
      this.dead = health <= 0.0F;
      this.food = food;
      this.saturation = saturation;
   }

   public boolean dead() {
      return this.dead;
   }

   public void setDead(boolean dead) {
      this.dead = dead;
   }

   public int food() {
      return this.food;
   }

   public float saturation() {
      return this.saturation;
   }
}
