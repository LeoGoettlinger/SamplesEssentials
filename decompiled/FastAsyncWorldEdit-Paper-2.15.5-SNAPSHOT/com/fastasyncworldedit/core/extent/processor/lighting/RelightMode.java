package com.fastasyncworldedit.core.extent.processor.lighting;

import java.util.HashMap;
import java.util.Map;

public enum RelightMode {
   NONE(0),
   OPTIMAL(1),
   ALL(2);

   private static final Map<Integer, RelightMode> map = new HashMap<>();
   private final int mode;

   private RelightMode(int mode) {
      this.mode = mode;
   }

   public static RelightMode valueOf(int mode) {
      return map.get(mode);
   }

   public int getMode() {
      return this.mode;
   }

   static {
      for (RelightMode mode : values()) {
         map.put(mode.mode, mode);
      }
   }
}
