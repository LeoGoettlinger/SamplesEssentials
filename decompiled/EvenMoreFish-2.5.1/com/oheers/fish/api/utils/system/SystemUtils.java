package com.oheers.fish.api.utils.system;

import org.jspecify.annotations.NonNull;

public class SystemUtils {
   public static final String JAVA_VERSION = System.getProperty("java.specification.version");

   private SystemUtils() {
      throw new UnsupportedOperationException();
   }

   public static boolean isJavaVersionAtLeast(@NonNull JavaSpecVersion required) {
      return isJavaVersionAtLeast(JAVA_VERSION, required);
   }

   static boolean isJavaVersionAtLeast(@NonNull String currentVersion, @NonNull JavaSpecVersion required) {
      float current = parseJavaVersion(currentVersion);
      return current >= required.getValue();
   }

   static float parseJavaVersion(@NonNull String version) {
      String[] parts = version.split("\\.");

      try {
         if (parts[0].equals("1")) {
            return parts.length > 1 && !parts[1].isBlank() ? Float.parseFloat(parts[0] + "." + parts[1]) : -1.0F;
         } else {
            return parts.length == 1 && !parts[0].isBlank() ? Float.parseFloat(parts[0]) : -1.0F;
         }
      } catch (NumberFormatException var3) {
         return -1.0F;
      }
   }
}
