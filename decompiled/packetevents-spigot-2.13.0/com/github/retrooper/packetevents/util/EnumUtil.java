package com.github.retrooper.packetevents.util;

public class EnumUtil {
   public static Enum<?> valueOf(Class<? extends Enum<?>> cls, String constantName) {
      if (cls == null) {
         return null;
      } else {
         for (Enum<?> enumConstant : cls.getEnumConstants()) {
            if (enumConstant.name().equals(constantName)) {
               return enumConstant;
            }
         }

         return null;
      }
   }

   public static Enum<?> valueByIndex(Class<? extends Enum<?>> cls, int index) {
      return (Enum<?>)(cls == null ? null : cls.getEnumConstants()[index]);
   }
}
