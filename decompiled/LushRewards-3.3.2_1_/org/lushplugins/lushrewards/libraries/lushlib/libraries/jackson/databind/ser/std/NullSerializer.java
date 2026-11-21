package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

@JacksonStdImpl
public class NullSerializer extends StdSerializer<Object> {
   public static final NullSerializer instance = new NullSerializer();

   private NullSerializer() {
      super(Object.class);
   }

   @Override
   public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeNull();
   }

   @Override
   public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
      gen.writeNull();
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return this.createSchemaNode("null");
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      visitor.expectNullFormat(typeHint);
   }
}
