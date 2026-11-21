package com.skyblockexp.ezrtp.teleport.heatmap;

import java.util.Locale;

public enum ClaimOverlayStyle {
   BORDER;

   public static ClaimOverlayStyle parse(String raw) {
      if (raw == null) {
         return BORDER;
      } else {
         String var1 = raw.trim().toLowerCase(Locale.ROOT);
         byte var2 = -1;
         var1.hashCode();
         switch (var2) {
            default:
               return BORDER;
         }
      }
   }
}
