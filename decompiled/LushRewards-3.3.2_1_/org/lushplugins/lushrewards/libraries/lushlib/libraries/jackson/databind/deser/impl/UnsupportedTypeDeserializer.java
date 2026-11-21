package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std.StdDeserializer;

public class UnsupportedTypeDeserializer extends StdDeserializer<Object> {
   private static final long serialVersionUID = 1L;
   protected final JavaType _type;
   protected final String _message;

   public UnsupportedTypeDeserializer(JavaType t, String m) {
      super(t);
      this._type = t;
      this._message = m;
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
         Object value = p.getEmbeddedObject();
         if (value == null || this._type.getRawClass().isAssignableFrom(value.getClass())) {
            return value;
         }
      }

      ctxt.reportBadDefinition(this._type, this._message);
      return null;
   }
}
