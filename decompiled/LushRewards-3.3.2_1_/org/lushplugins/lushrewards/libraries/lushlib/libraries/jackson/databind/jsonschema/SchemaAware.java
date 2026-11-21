package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonschema;

import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

@Deprecated
public interface SchemaAware {
   JsonNode getSchema(SerializerProvider var1, Type var2) throws JsonMappingException;

   JsonNode getSchema(SerializerProvider var1, Type var2, boolean var3) throws JsonMappingException;
}
