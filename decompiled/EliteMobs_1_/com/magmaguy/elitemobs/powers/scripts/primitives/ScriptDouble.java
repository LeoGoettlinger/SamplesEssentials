package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptDouble {
   private Double value = null;
   private Double lowestRange = null;
   private Double highestRange = null;

   public ScriptDouble(double lowestRange, double highestRange) {
      if (Double.isFinite(lowestRange) && Double.isFinite(highestRange) && !(lowestRange > highestRange) && Double.isFinite(highestRange - lowestRange)) {
         if (lowestRange == highestRange) {
            this.value = lowestRange;
         }

         this.lowestRange = lowestRange;
         this.highestRange = highestRange;
      } else {
         throw new IllegalArgumentException("Invalid floating script range: " + lowestRange + "~" + highestRange);
      }
   }

   public ScriptDouble(double value) {
      if (!Double.isFinite(value)) {
         throw new IllegalArgumentException("Nonfinite script value: " + value);
      } else {
         this.value = value;
      }
   }

   public boolean isRandom() {
      return this.value == null;
   }

   public Double getValue() {
      return this.value != null ? this.value : ThreadLocalRandom.current().nextDouble(this.lowestRange, this.highestRange);
   }
}
