package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.util.EitherImpl;

public final class ResolvableFloat extends EitherImpl<Float, String> {
   private ResolvableFloat(Float left, String right) {
      super(left, right);
   }

   public static ResolvableFloat of(String value) {
      return new ResolvableFloat(null, value);
   }

   public static ResolvableFloat of(Float value) {
      return new ResolvableFloat(value, null);
   }
}
