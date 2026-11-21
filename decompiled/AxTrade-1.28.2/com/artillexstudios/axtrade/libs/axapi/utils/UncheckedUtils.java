package com.artillexstudios.axtrade.libs.axapi.utils;

public class UncheckedUtils {
   public static <T> T unsafeCast(Object object) {
      return (T)object;
   }
}
