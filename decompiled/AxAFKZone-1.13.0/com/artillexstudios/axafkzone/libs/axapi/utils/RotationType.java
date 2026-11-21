package com.artillexstudios.axafkzone.libs.axapi.utils;

public enum RotationType {
   HEAD(16),
   BODY(17),
   LEFT_ARM(18),
   RIGHT_ARM(19),
   LEFT_LEG(20),
   RIGHT_LEG(21);

   public final int index;

   private RotationType(int index) {
      this.index = index;
   }
}
