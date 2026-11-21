package com.magmaguy.elitemobs.magmacore.ai;

public interface MindSensor {
   String identifier();

   default int intervalTicks() {
      return 1;
   }

   void sense(MindContext var1);
}
