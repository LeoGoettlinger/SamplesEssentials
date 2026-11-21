package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.NullValueProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.AccessPattern;

public class NullsAsEmptyProvider implements NullValueProvider, Serializable {
   private static final long serialVersionUID = 1L;
   protected final JsonDeserializer<?> _deserializer;

   public NullsAsEmptyProvider(JsonDeserializer<?> deser) {
      this._deserializer = deser;
   }

   @Override
   public AccessPattern getNullAccessPattern() {
      return AccessPattern.DYNAMIC;
   }

   @Override
   public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      return this._deserializer.getEmptyValue(ctxt);
   }
}
