package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;

public class NullifyingDeserializer extends StdDeserializer<Object> {
   private static final long serialVersionUID = 1L;
   public static final NullifyingDeserializer instance = new NullifyingDeserializer();

   public NullifyingDeserializer() {
      super(Object.class);
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.FALSE;
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.FIELD_NAME)) {
         while (true) {
            JsonToken t = p.nextToken();
            if (t == null || t == JsonToken.END_OBJECT) {
               break;
            }

            p.skipChildren();
         }
      } else {
         p.skipChildren();
      }

      return null;
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      switch (p.currentTokenId()) {
         case 1:
         case 3:
         case 5:
            return typeDeserializer.deserializeTypedFromAny(p, ctxt);
         case 2:
         case 4:
         default:
            return null;
      }
   }
}
