package com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlag;

public final class BooleanFlag extends FeatureFlag<Boolean> {
   public static final BooleanFlag TRANSFORMER = new BooleanFlag(true);

   public BooleanFlag(Boolean def) {
      super(def);
   }

   public Boolean transform(String string) {
      return string == null ? null : Boolean.parseBoolean(string);
   }
}
