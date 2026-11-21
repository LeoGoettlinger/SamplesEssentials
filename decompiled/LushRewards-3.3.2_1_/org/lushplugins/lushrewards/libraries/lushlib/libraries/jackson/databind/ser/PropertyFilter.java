package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;

public interface PropertyFilter {
   void serializeAsField(Object var1, JsonGenerator var2, SerializerProvider var3, PropertyWriter var4) throws Exception;

   void serializeAsElement(Object var1, JsonGenerator var2, SerializerProvider var3, PropertyWriter var4) throws Exception;

   @Deprecated
   void depositSchemaProperty(PropertyWriter var1, ObjectNode var2, SerializerProvider var3) throws JsonMappingException;

   void depositSchemaProperty(PropertyWriter var1, JsonObjectFormatVisitor var2, SerializerProvider var3) throws JsonMappingException;
}
