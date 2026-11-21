package com.viaversion.viabackwards.utils;

public class VelocityUtil {
   public static short toLegacyVelocity(double value) {
      return (short)Math.max(-32768L, Math.min(32767L, Math.round(value * 8000.0)));
   }
}
