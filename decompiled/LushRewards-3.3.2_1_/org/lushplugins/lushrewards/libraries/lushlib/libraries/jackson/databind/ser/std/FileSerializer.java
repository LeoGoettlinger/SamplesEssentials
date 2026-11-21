package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;

public class FileSerializer extends StdScalarSerializer<File> {
   public FileSerializer() {
      super(File.class);
   }

   public void serialize(File value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeString(value.getAbsolutePath());
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
