package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

final class UrlSerializer extends ScalarSerializer<URL> {
   UrlSerializer() {
      super(URL.class);
   }

   public URL deserialize(final Type type, final Object obj) throws SerializationException {
      String plainUri = obj.toString();

      try {
         return new URL(plainUri);
      } catch (MalformedURLException var5) {
         throw new CoercionFailedException(obj, "URL");
      }
   }

   public Object serialize(final URL item, final Predicate<Class<?>> typeSupported) {
      return item.toString();
   }
}
