package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public class NullNode extends ValueNode {
   private static final long serialVersionUID = 1L;
   public static final NullNode instance = new NullNode();

   protected NullNode() {
   }

   protected Object readResolve() {
      return instance;
   }

   public static NullNode getInstance() {
      return instance;
   }

   @Override
   public JsonNodeType getNodeType() {
      return JsonNodeType.NULL;
   }

   @Override
   public JsonToken asToken() {
      return JsonToken.VALUE_NULL;
   }

   @Override
   public String asText(String defaultValue) {
      return defaultValue;
   }

   @Override
   public String asText() {
      return "null";
   }

   @Override
   public JsonNode requireNonNull() {
      return this._reportRequiredViolation("requireNonNull() called on `NullNode`", new Object[0]);
   }

   @Override
   public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
      provider.defaultSerializeNull(g);
   }

   @Override
   public boolean equals(Object o) {
      return o == this || o instanceof NullNode;
   }

   @Override
   public int hashCode() {
      return JsonNodeType.NULL.ordinal();
   }
}
