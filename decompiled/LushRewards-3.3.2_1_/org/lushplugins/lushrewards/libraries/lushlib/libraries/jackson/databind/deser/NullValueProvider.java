package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.AccessPattern;

public interface NullValueProvider {
   Object getNullValue(DeserializationContext var1) throws JsonMappingException;

   AccessPattern getNullAccessPattern();

   default Object getAbsentValue(DeserializationContext ctxt) throws JsonMappingException {
      return this.getNullValue(ctxt);
   }
}
