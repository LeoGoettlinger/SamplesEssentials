package de.theredend2000.advancedhunt.lib.cronutils.model.time;

class NearestValue {
   private final int shifts;
   private final int value;

   public NearestValue(int value, int shifts) {
      this.shifts = shifts;
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public int getShifts() {
      return this.shifts;
   }
}
