package org.skriptlang.skript.bukkit.misc.rotation;

import java.util.Locale;

@FunctionalInterface
public interface Rotator<T> {
   T rotate(T var1);

   public static enum Axis {
      X,
      LOCAL_X,
      Y,
      LOCAL_Y,
      Z,
      LOCAL_Z,
      ARBITRARY,
      LOCAL_ARBITRARY;

      @Override
      public String toString() {
         return super.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
      }

      public static Rotator.Axis fromBukkit(org.bukkit.Axis axis) {
         return switch (axis) {
            case X -> X;
            case Y -> Y;
            case Z -> Z;
            default -> throw new MatchException(null, null);
         };
      }
   }
}
