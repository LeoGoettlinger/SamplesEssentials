package ch.njol.util;

import java.lang.invoke.StringConcatFactory;
import java.util.Locale;

public enum Kleenean {
   FALSE,
   UNKNOWN,
   TRUE;

   @Override
   public final String toString() {
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.name().toLowerCase(Locale.ENGLISH));
   }

   public final Kleenean is(Kleenean other) {
      if (other == UNKNOWN || this == UNKNOWN) {
         return UNKNOWN;
      } else {
         return other == this ? TRUE : FALSE;
      }
   }

   public final Kleenean and(Kleenean other) {
      if (this == FALSE || other == FALSE) {
         return FALSE;
      } else {
         return this == TRUE && other == TRUE ? TRUE : UNKNOWN;
      }
   }

   public final Kleenean or(Kleenean other) {
      if (this == TRUE || other == TRUE) {
         return TRUE;
      } else {
         return this == FALSE && other == FALSE ? FALSE : UNKNOWN;
      }
   }

   public final Kleenean not() {
      if (this == TRUE) {
         return FALSE;
      } else {
         return this == FALSE ? TRUE : UNKNOWN;
      }
   }

   public final Kleenean implies(Kleenean other) {
      if (this == FALSE || other == TRUE) {
         return TRUE;
      } else {
         return this == TRUE && other == FALSE ? FALSE : UNKNOWN;
      }
   }

   public final boolean isTrue() {
      return this == TRUE;
   }

   public final boolean isUnknown() {
      return this == UNKNOWN;
   }

   public final boolean isFalse() {
      return this == FALSE;
   }

   public static Kleenean get(boolean b) {
      return b ? TRUE : FALSE;
   }

   public static Kleenean get(int i) {
      return i > 0 ? TRUE : (i < 0 ? FALSE : UNKNOWN);
   }

   public static Kleenean get(double d) {
      return d > 0.0 ? TRUE : (d < 0.0 ? FALSE : UNKNOWN);
   }
}
