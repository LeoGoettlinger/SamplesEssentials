package com.ranull.graves.util;

import java.util.UUID;

public class UUIDUtil {
   private UUIDUtil() {
   }

   public static UUID getUUID(String input) {
      String s = input.trim();
      if (s.isEmpty()) {
         return null;
      } else if (s.contains("-")) {
         try {
            return UUID.fromString(s);
         } catch (IllegalArgumentException var4) {
            return null;
         }
      } else if (s.length() != 32) {
         return null;
      } else if (!s.matches("[0-9a-fA-F]{32}")) {
         return null;
      } else {
         String withDashes = s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20);

         try {
            return UUID.fromString(withDashes);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }
}
