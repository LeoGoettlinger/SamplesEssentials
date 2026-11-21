package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;

public class AtomicIntegerDeserializer extends StdScalarDeserializer<AtomicInteger> {
   private static final long serialVersionUID = 1L;

   public AtomicIntegerDeserializer() {
      super(AtomicInteger.class);
   }

   public AtomicInteger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.isExpectedNumberIntToken()) {
         return new AtomicInteger(p.getIntValue());
      } else {
         Integer I = this._parseInteger(p, ctxt, AtomicInteger.class);
         return I == null ? null : new AtomicInteger(I);
      }
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Integer;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return new AtomicInteger();
   }
}
