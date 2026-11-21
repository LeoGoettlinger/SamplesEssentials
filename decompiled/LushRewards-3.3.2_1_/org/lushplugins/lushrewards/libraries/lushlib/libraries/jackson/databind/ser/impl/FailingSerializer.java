package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.StdSerializer;

public class FailingSerializer extends StdSerializer<Object> {
   protected final String _msg;

   public FailingSerializer(String msg) {
      super(Object.class);
      this._msg = msg;
   }

   @Override
   public void serialize(Object value, JsonGenerator g, SerializerProvider ctxt) throws IOException {
      ctxt.reportMappingProblem(this._msg);
   }
}
