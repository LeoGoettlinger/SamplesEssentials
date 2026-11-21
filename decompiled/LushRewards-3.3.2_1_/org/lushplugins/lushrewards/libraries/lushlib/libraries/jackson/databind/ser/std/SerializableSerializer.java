package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

@JacksonStdImpl
public class SerializableSerializer extends StdSerializer<JsonSerializable> {
   public static final SerializableSerializer instance = new SerializableSerializer();

   protected SerializableSerializer() {
      super(JsonSerializable.class);
   }

   public boolean isEmpty(SerializerProvider serializers, JsonSerializable value) {
      return value instanceof JsonSerializable.Base ? ((JsonSerializable.Base)value).isEmpty(serializers) : false;
   }

   public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      value.serialize(gen, serializers);
   }

   public final void serializeWithType(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
      value.serializeWithType(gen, serializers, typeSer);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      visitor.expectAnyFormat(typeHint);
   }
}
