package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

@Deprecated
public class StdKeySerializer extends StdSerializer<Object> {
   public StdKeySerializer() {
      super(Object.class);
   }

   @Override
   public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeFieldName(value.toString());
   }
}
