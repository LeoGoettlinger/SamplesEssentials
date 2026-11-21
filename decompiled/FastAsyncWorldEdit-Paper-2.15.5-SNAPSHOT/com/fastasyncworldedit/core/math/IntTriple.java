package com.fastasyncworldedit.core.math;

public record IntTriple(int x, int y, int z) {
   @Override
   public int hashCode() {
      return this.x ^ this.z << 12 ^ this.y << 24;
   }

   @Override
   public String toString() {
      return this.x + "," + this.y + "," + this.z;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof IntTriple other) ? false : other.x == this.x && other.z == this.z && other.y == this.y;
   }
}
