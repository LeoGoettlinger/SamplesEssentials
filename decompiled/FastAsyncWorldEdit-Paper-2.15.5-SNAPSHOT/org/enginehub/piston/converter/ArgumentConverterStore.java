package org.enginehub.piston.converter;

import org.enginehub.piston.inject.Key;

public interface ArgumentConverterStore extends ArgumentConverterAccess {
   <T> void registerConverter(Key<T> var1, ArgumentConverter<T> var2);

   default void registerConverters(ArgumentConverterAccess converters) {
      for (Key<?> key : converters.keySet()) {
         ArgumentConverter<Object> converter = (ArgumentConverter<Object>)converters.getConverter(key).orElseThrow(IllegalStateException::new);
         this.registerConverter(key, converter);
      }
   }
}
