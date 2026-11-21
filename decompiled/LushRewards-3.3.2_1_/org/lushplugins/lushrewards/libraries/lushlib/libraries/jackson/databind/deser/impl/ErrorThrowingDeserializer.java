package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;

public class ErrorThrowingDeserializer extends JsonDeserializer<Object> {
   private final Error _cause;

   public ErrorThrowingDeserializer(NoClassDefFoundError cause) {
      this._cause = cause;
   }

   @Override
   public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      throw this._cause;
   }
}
