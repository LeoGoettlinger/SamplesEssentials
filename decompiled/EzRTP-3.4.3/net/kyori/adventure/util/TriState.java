package net.kyori.adventure.util;

import java.util.function.BooleanSupplier;
import org.jspecify.annotations.Nullable;

public enum TriState {
   NOT_SET,
   FALSE,
   TRUE;

   @Nullable
   public Boolean toBoolean() {
      return switch (this) {
         case FALSE -> Boolean.FALSE;
         case TRUE -> Boolean.TRUE;
         default -> null;
      };
   }

   public boolean toBooleanOrElse(final boolean other) {
      return switch (this) {
         case FALSE -> false;
         case TRUE -> true;
         default -> other;
      };
   }

   public boolean toBooleanOrElseGet(final BooleanSupplier supplier) {
      return switch (this) {
         case FALSE -> false;
         case TRUE -> true;
         default -> supplier.getAsBoolean();
      };
   }

   public static TriState byBoolean(final boolean value) {
      return value ? TRUE : FALSE;
   }

   public static TriState byBoolean(@Nullable final Boolean value) {
      return value == null ? NOT_SET : byBoolean(value.booleanValue());
   }
}
