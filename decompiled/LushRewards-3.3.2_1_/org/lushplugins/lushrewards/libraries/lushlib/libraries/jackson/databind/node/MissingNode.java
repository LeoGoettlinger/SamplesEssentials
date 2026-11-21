package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

public final class MissingNode extends ValueNode {
   private static final long serialVersionUID = 1L;
   private static final MissingNode instance = new MissingNode();

   protected MissingNode() {
   }

   protected Object readResolve() {
      return instance;
   }

   @Override
   public boolean isMissingNode() {
      return true;
   }

   @Override
   public <T extends JsonNode> T deepCopy() {
      return (T)this;
   }

   public static MissingNode getInstance() {
      return instance;
   }

   @Override
   public JsonNodeType getNodeType() {
      return JsonNodeType.MISSING;
   }

   @Override
   public JsonToken asToken() {
      return JsonToken.NOT_AVAILABLE;
   }

   @Override
   public String asText() {
      return "";
   }

   @Override
   public String asText(String defaultValue) {
      return defaultValue;
   }

   @Override
   public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeNull();
   }

   @Override
   public void serializeWithType(JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      g.writeNull();
   }

   @Override
   public JsonNode require() {
      return this._reportRequiredViolation("require() called on `MissingNode`", new Object[0]);
   }

   @Override
   public JsonNode requireNonNull() {
      return this._reportRequiredViolation("requireNonNull() called on `MissingNode`", new Object[0]);
   }

   @Override
   public int hashCode() {
      return JsonNodeType.MISSING.ordinal();
   }

   @Override
   public String toString() {
      return "";
   }

   @Override
   public String toPrettyString() {
      return "";
   }

   @Override
   public boolean equals(Object o) {
      return o == this;
   }
}
