package com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlag;

public final class IntegerFlag extends FeatureFlag<Integer> {
   public static final IntegerFlag TRANSFORMER = new IntegerFlag(0);

   public IntegerFlag(Integer def) {
      super(def);
   }

   public Integer transform(String string) {
      return string == null ? null : Integer.parseInt(string);
   }
}
