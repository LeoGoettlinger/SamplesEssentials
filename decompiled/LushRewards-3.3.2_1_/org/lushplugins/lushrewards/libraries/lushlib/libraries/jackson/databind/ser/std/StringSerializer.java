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
public final class StringSerializer extends StdScalarSerializer<Object> {
   private static final long serialVersionUID = 1L;

   public StringSerializer() {
      super(String.class, false);
   }

   @Override
   public boolean isEmpty(SerializerProvider prov, Object value) {
      String str = (String)value;
      return str.isEmpty();
   }

   @Override
   public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString((String)value);
   }

   @Override
   public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      gen.writeString((String)value);
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return this.createSchemaNode("string", true);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      this.visitStringFormat(visitor, typeHint);
   }
}
