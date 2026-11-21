package com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type;

import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlag;
import java.util.Arrays;

public final class EnumFlag<T extends Enum<?>> extends FeatureFlag<T> {
   private final Class<T> clazz;

   public EnumFlag(T def, Class<T> clazz) {
      super(def);
      this.clazz = clazz;
   }

   public T transform(String string) {
      return string == null
         ? null
         : Arrays.<T>stream(this.clazz.getEnumConstants()).filter(constant -> constant.name().equalsIgnoreCase(string)).findFirst().orElseThrow();
   }
}
