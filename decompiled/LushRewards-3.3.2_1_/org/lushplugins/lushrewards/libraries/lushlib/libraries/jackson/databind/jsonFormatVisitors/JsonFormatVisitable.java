package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;

public interface JsonFormatVisitable {
   void acceptJsonFormatVisitor(JsonFormatVisitorWrapper var1, JavaType var2) throws JsonMappingException;
}
