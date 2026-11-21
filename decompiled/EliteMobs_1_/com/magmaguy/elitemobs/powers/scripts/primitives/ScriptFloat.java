package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptFloat {
   private Float value = null;
   private Float lowestRange = null;
   private Float highestRange = null;

   public ScriptFloat(float lowestRange, float highestRange) {
      if (Float.isFinite(lowestRange) && Float.isFinite(highestRange) && !(lowestRange > highestRange) && Float.isFinite(highestRange - lowestRange)) {
         if (lowestRange == highestRange) {
            this.value = lowestRange;
         }

         this.lowestRange = lowestRange;
         this.highestRange = highestRange;
      } else {
         throw new IllegalArgumentException("Invalid floating script range: " + lowestRange + "~" + highestRange);
      }
   }

   public ScriptFloat(float value) {
      if (!Float.isFinite(value)) {
         throw new IllegalArgumentException("Nonfinite script value: " + value);
      } else {
         this.value = value;
      }
   }

   public boolean isRandom() {
      return this.value == null;
   }

   public Float getValue() {
      return this.value != null ? this.value : ThreadLocalRandom.current().nextFloat(this.lowestRange, this.highestRange);
   }
}
