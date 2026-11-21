package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std.StdDeserializer;

public class FailingDeserializer extends StdDeserializer<Object> {
   private static final long serialVersionUID = 1L;
   protected final String _message;

   public FailingDeserializer(String m) {
      this(Object.class, m);
   }

   public FailingDeserializer(Class<?> rawType, String m) {
      super(rawType);
      this._message = m;
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      ctxt.reportInputMismatch(this, this._message);
      return null;
   }
}
