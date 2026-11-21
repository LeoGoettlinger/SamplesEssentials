package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptInteger {
   private Integer value = null;
   private Integer lowestRange = null;
   private Integer highestRange = null;

   public ScriptInteger(int lowestRange, int highestRange) {
      if (lowestRange > highestRange) {
         throw new IllegalArgumentException("Reversed script range: " + lowestRange + "~" + highestRange);
      } else {
         if (lowestRange == highestRange) {
            this.value = lowestRange;
         }

         this.lowestRange = lowestRange;
         this.highestRange = highestRange;
      }
   }

   public ScriptInteger(int value) {
      this.value = value;
   }

   public boolean isRandom() {
      return this.value == null;
   }

   public Integer getValue() {
      return this.value != null ? this.value : (int)ThreadLocalRandom.current().nextLong(this.lowestRange.intValue(), this.highestRange.intValue() + 1L);
   }
}
