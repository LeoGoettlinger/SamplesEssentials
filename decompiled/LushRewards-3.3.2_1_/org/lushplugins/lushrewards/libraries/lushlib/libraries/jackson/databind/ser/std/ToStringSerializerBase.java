package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

public abstract class ToStringSerializerBase extends StdSerializer<Object> {
   public ToStringSerializerBase(Class<?> handledType) {
      super(handledType, false);
   }

   @Override
   public boolean isEmpty(SerializerProvider prov, Object value) {
      return this.valueToString(value).isEmpty();
   }

   @Override
   public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString(this.valueToString(value));
   }

   @Override
   public void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.VALUE_STRING));
      this.serialize(value, g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return this.createSchemaNode("string", true);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      this.visitStringFormat(visitor, typeHint);
   }

   public abstract String valueToString(Object var1);
}
