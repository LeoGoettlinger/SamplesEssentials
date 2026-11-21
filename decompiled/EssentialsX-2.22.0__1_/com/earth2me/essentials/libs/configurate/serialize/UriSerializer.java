package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Predicate;

final class UriSerializer extends ScalarSerializer<URI> {
   UriSerializer() {
      super(URI.class);
   }

   public URI deserialize(final Type type, final Object obj) throws SerializationException {
      String plainUri = obj.toString();

      try {
         return new URI(plainUri);
      } catch (URISyntaxException var5) {
         throw new CoercionFailedException(obj, "URI");
      }
   }

   public Object serialize(final URI item, final Predicate<Class<?>> typeSupported) {
      return item.toString();
   }
}
