package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;

@JacksonStdImpl
public class StringDeserializer extends StdScalarDeserializer<String> {
   private static final long serialVersionUID = 1L;
   public static final StringDeserializer instance = new StringDeserializer();

   public StringDeserializer() {
      super(String.class);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Textual;
   }

   @Override
   public boolean isCachable() {
      return true;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return "";
   }

   public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_STRING)) {
         return p.getText();
      } else {
         return p.hasToken(JsonToken.START_ARRAY) ? this._deserializeFromArray(p, ctxt) : this._parseString(p, ctxt, this);
      }
   }

   public String deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return this.deserialize(p, ctxt);
   }
}
