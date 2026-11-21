package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.StdSerializer;

public class UnsupportedTypeSerializer extends StdSerializer<Object> {
   private static final long serialVersionUID = 1L;
   protected final JavaType _type;
   protected final String _message;

   public UnsupportedTypeSerializer(JavaType t, String msg) {
      super(Object.class);
      this._type = t;
      this._message = msg;
   }

   @Override
   public void serialize(Object value, JsonGenerator g, SerializerProvider ctxt) throws IOException {
      ctxt.reportBadDefinition(this._type, this._message);
   }
}
