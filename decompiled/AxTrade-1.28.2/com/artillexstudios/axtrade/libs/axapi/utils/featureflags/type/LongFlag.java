package com.artillexstudios.axtrade.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlag;

public final class LongFlag extends FeatureFlag<Long> {
   public static final LongFlag TRANSFORMER = new LongFlag(0L);

   public LongFlag(Long def) {
      super(def);
   }

   public Long transform(String string) {
      return string == null ? null : Long.parseLong(string);
   }
}
