package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ext;

import java.io.IOException;
import java.nio.file.Path;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.StdScalarSerializer;

public class NioPathSerializer extends StdScalarSerializer<Path> {
   private static final long serialVersionUID = 1L;

   public NioPathSerializer() {
      super(Path.class);
   }

   public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeString(value.toUri().toString());
   }

   public void serializeWithType(Path value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, Path.class, JsonToken.VALUE_STRING));
      this.serialize(value, g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }
}
