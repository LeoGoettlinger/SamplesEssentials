package com.artillexstudios.axtrade.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlag;

public final class StringFlag extends FeatureFlag<String> {
   public static final StringFlag TRANSFORMER = new StringFlag("");

   public StringFlag(String def) {
      super(def);
   }

   public String transform(String string) {
      return string;
   }
}
