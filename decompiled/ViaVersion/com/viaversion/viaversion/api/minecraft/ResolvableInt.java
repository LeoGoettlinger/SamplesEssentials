package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.util.EitherImpl;

public final class ResolvableInt extends EitherImpl<Integer, String> {
   private ResolvableInt(Integer left, String right) {
      super(left, right);
   }

   public static ResolvableInt of(String value) {
      return new ResolvableInt(null, value);
   }

   public static ResolvableInt of(Integer value) {
      return new ResolvableInt(value, null);
   }
}
