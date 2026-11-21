package com.magmaguy.elitemobs.pathfinding.patrol;

import java.util.Locale;

public enum PatrolMode {
   LOOP,
   REVERSE;

   public static PatrolMode parse(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            throw new IllegalArgumentException("patrol.mode must be LOOP or REVERSE", var2);
         }
      } else {
         return LOOP;
      }
   }
}
