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
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

@JacksonStdImpl
public class ToEmptyObjectSerializer extends StdSerializer<Object> {
   protected ToEmptyObjectSerializer(Class<?> raw) {
      super(raw, false);
   }

   public ToEmptyObjectSerializer(JavaType type) {
      super(type);
   }

   @Override
   public void serialize(Object value, JsonGenerator gen, SerializerProvider ctxt) throws IOException {
      gen.writeStartObject(value, 0);
      gen.writeEndObject();
   }

   @Override
   public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider ctxt, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT));
      typeSer.writeTypeSuffix(gen, typeIdDef);
   }

   @Override
   public boolean isEmpty(SerializerProvider provider, Object value) {
      return true;
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return null;
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      visitor.expectObjectFormat(typeHint);
   }
}
