package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;

@Deprecated
public interface BeanPropertyFilter {
   void serializeAsField(Object var1, JsonGenerator var2, SerializerProvider var3, BeanPropertyWriter var4) throws Exception;

   @Deprecated
   void depositSchemaProperty(BeanPropertyWriter var1, ObjectNode var2, SerializerProvider var3) throws JsonMappingException;

   void depositSchemaProperty(BeanPropertyWriter var1, JsonObjectFormatVisitor var2, SerializerProvider var3) throws JsonMappingException;
}
