package com.earth2me.essentials.libs.snakeyaml.serializer;

import com.earth2me.essentials.libs.snakeyaml.error.YAMLException;

public class SerializerException extends YAMLException {
   private static final long serialVersionUID = 2632638197498912433L;

   public SerializerException(String message) {
      super(message);
   }
}
