package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;

public class AtomicBooleanDeserializer extends StdScalarDeserializer<AtomicBoolean> {
   private static final long serialVersionUID = 1L;

   public AtomicBooleanDeserializer() {
      super(AtomicBoolean.class);
   }

   public AtomicBoolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonToken t = p.currentToken();
      if (t == JsonToken.VALUE_TRUE) {
         return new AtomicBoolean(true);
      } else if (t == JsonToken.VALUE_FALSE) {
         return new AtomicBoolean(false);
      } else {
         Boolean b = this._parseBoolean(p, ctxt, AtomicBoolean.class);
         return b == null ? null : new AtomicBoolean(b);
      }
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Boolean;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return new AtomicBoolean(false);
   }
}
