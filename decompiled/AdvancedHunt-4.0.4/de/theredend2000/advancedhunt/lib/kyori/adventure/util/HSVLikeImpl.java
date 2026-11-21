package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

record HSVLikeImpl(float h, float s, float v) implements HSVLike {
   HSVLikeImpl(float h, float s, float v) {
      requireInsideRange(h, "h");
      requireInsideRange(s, "s");
      requireInsideRange(v, "v");
      this.h = h;
      this.s = s;
      this.v = v;
   }

   private static void requireInsideRange(final float number, final String name) throws IllegalArgumentException {
      if (number < 0.0F || 1.0F < number) {
         throw new IllegalArgumentException(name + " (" + number + ") is not inside the required range: [0,1]");
      }
   }
}
