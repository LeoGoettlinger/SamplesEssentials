package net.luckperms.api.util;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum Tristate {
   TRUE(true),
   FALSE(false),
   UNDEFINED(false);

   private final boolean booleanValue;

   @NonNull
   public static Tristate of(boolean val) {
      return val ? TRUE : FALSE;
   }

   @NonNull
   public static Tristate of(Boolean val) {
      return val == null ? UNDEFINED : (val ? TRUE : FALSE);
   }

   private Tristate(boolean booleanValue) {
      this.booleanValue = booleanValue;
   }

   public boolean asBoolean() {
      return this.booleanValue;
   }
}
