package com.artillexstudios.axafkzone.libs.axapi.utils;

public record Vector3d(double x, double y, double z) {
   public Vector3d() {
      this(0.0, 0.0, 0.0);
   }

   public Vector3d(Vector3f location) {
      this(location.x(), location.y(), location.z());
   }
}
