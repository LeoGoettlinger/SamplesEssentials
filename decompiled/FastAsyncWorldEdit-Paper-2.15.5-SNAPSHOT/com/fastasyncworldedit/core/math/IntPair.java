package com.fastasyncworldedit.core.math;

public record IntPair(int x, int z) {
   @Override
   public int hashCode() {
      int i = 1664525 * this.x + 1013904223;
      int j = 1664525 * (this.z ^ -559038737) + 1013904223;
      return i ^ j;
   }

   @Override
   public String toString() {
      return this.x + "," + this.z;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         IntPair other = (IntPair)obj;
         return this.x == other.x && this.z == other.z;
      } else {
         return false;
      }
   }
}
