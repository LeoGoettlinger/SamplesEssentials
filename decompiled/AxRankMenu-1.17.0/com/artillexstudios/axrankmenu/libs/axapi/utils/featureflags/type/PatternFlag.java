package com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlag;
import java.util.regex.Pattern;

public final class PatternFlag extends FeatureFlag<Pattern> {
   public static final PatternFlag TRANSFORMER = new PatternFlag(Pattern.compile(" "));

   public PatternFlag(Pattern def) {
      super(def);
   }

   public Pattern transform(String string) {
      return string == null ? null : Pattern.compile(string);
   }
}
