package com.artillexstudios.axrankmenu.libs.axapi.utils;

import org.jspecify.annotations.Nullable;

public class NumberUtils {
   public static boolean isDouble(@Nullable String value) {
      if (value == null) {
         return false;
      } else {
         try {
            Double.parseDouble(value);
            return true;
         } catch (Exception var2) {
            return false;
         }
      }
   }

   public static boolean isInt(@Nullable String value) {
      if (value == null) {
         return false;
      } else {
         try {
            Integer.parseInt(value);
            return true;
         } catch (Exception var2) {
            return false;
         }
      }
   }

   public static boolean isLong(@Nullable String value) {
      if (value == null) {
         return false;
      } else {
         try {
            Long.parseLong(value);
            return true;
         } catch (Exception var2) {
            return false;
         }
      }
   }

   public static boolean isFloat(@Nullable String value) {
      if (value == null) {
         return false;
      } else {
         try {
            Float.parseFloat(value);
            return true;
         } catch (Exception var2) {
            return false;
         }
      }
   }
}
