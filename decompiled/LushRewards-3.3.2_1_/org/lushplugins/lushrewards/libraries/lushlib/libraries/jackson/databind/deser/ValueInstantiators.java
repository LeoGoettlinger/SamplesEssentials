package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;

public interface ValueInstantiators {
   ValueInstantiator findValueInstantiator(DeserializationConfig var1, BeanDescription var2, ValueInstantiator var3);

   public static class Base implements ValueInstantiators {
      @Override
      public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
         return defaultInstantiator;
      }
   }
}
