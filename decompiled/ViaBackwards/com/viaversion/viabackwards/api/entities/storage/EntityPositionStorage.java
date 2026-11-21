package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public abstract class EntityPositionStorage implements StorableObject {
   private double x;
   private double y;
   private double z;

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

   public void addRelativePosition(double relX, double relY, double relZ) {
      this.x += relX;
      this.y += relY;
      this.z += relZ;
   }
}
