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
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;

@JacksonStdImpl
public class ByteArraySerializer extends StdSerializer<byte[]> {
   private static final long serialVersionUID = 1L;

   public ByteArraySerializer() {
      super(byte[].class);
   }

   public boolean isEmpty(SerializerProvider prov, byte[] value) {
      return value.length == 0;
   }

   public void serialize(byte[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeBinary(provider.getConfig().getBase64Variant(), value, 0, value.length);
   }

   public void serializeWithType(byte[] value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
      g.writeBinary(provider.getConfig().getBase64Variant(), value, 0, value.length);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      ObjectNode o = this.createSchemaNode("array", true);
      ObjectNode itemSchema = this.createSchemaNode("byte");
      return o.set("items", itemSchema);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
      if (v2 != null) {
         v2.itemsFormat(JsonFormatTypes.INTEGER);
      }
   }
}
