package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;

public interface ContextualDeserializer {
   JsonDeserializer<?> createContextual(DeserializationContext var1, BeanProperty var2) throws JsonMappingException;
}
