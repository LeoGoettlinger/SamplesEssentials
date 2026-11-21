package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

public enum CustomEntityPropertyType {
   BOOLEAN(Boolean.class),
   INT(Integer.class),
   FLOAT(Float.class),
   STRING(String.class);

   private final Class<?> valueClass;

   private CustomEntityPropertyType(Class<?> valueClass) {
      this.valueClass = valueClass;
   }

   public Class<?> valueClass() {
      return this.valueClass;
   }
}
