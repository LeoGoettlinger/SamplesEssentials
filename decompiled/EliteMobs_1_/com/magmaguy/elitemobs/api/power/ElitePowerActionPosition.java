package com.magmaguy.elitemobs.api.power;

public record ElitePowerActionPosition(String world, double x, double y, double z) {
   public ElitePowerActionPosition(String world, double x, double y, double z) {
      if (world == null || world.isBlank()) {
         throw new IllegalArgumentException("world cannot be blank");
      } else if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
      } else {
         throw new IllegalArgumentException("Power action positions must be finite");
      }
   }
}
