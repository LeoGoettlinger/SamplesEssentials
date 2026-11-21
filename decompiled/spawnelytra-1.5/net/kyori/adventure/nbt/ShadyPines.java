package net.kyori.adventure.nbt;

final class ShadyPines {
   private ShadyPines() {
   }

   static int floor(final double dv) {
      int iv = (int)dv;
      return dv < iv ? iv - 1 : iv;
   }

   static int floor(final float fv) {
      int iv = (int)fv;
      return fv < iv ? iv - 1 : iv;
   }
}
