package com.viaversion.viaversion.api.minecraft;

public enum Environment {
   NORMAL(0),
   NETHER(-1),
   END(1);

   private final int id;

   private Environment(int id) {
      this.id = id;
   }

   public int id() {
      return this.id;
   }

   public static Environment getEnvironmentById(int id) {
      return switch (id) {
         case 0 -> NORMAL;
         case 1 -> END;
         default -> NETHER;
      };
   }
}
