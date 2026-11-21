package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.TokenBuffer;

@JacksonStdImpl
public class TokenBufferDeserializer extends StdScalarDeserializer<TokenBuffer> {
   private static final long serialVersionUID = 1L;

   public TokenBufferDeserializer() {
      super(TokenBuffer.class);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Untyped;
   }

   public TokenBuffer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return ctxt.bufferForInputBuffering(p).deserialize(p, ctxt);
   }
}
