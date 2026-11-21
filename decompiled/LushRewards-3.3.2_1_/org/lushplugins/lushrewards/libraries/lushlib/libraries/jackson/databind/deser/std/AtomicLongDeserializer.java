package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;

public class AtomicLongDeserializer extends StdScalarDeserializer<AtomicLong> {
   private static final long serialVersionUID = 1L;

   public AtomicLongDeserializer() {
      super(AtomicLong.class);
   }

   public AtomicLong deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.isExpectedNumberIntToken()) {
         return new AtomicLong(p.getLongValue());
      } else {
         Long L = this._parseLong(p, ctxt, AtomicLong.class);
         return L == null ? null : new AtomicLong(L.intValue());
      }
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Integer;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return new AtomicLong();
   }
}
