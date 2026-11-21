package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;

public interface ResolvableDeserializer {
   void resolve(DeserializationContext var1) throws JsonMappingException;
}
