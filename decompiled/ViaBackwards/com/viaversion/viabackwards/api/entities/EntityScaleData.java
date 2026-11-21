package com.viaversion.viabackwards.api.entities;

import com.viaversion.viaversion.api.connection.StorableObject;

public final class EntityScaleData implements StorableObject {
   private boolean isBaby;
   private float scale = 1.0F;

   public boolean isBaby() {
      return this.isBaby;
   }

   public void setBaby(boolean baby) {
      this.isBaby = baby;
   }

   public float scale() {
      return this.scale;
   }

   public void setScale(float scale) {
      this.scale = scale;
   }
}
