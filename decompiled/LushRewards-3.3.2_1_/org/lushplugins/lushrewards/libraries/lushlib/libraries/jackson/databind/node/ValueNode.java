package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node;

import java.io.IOException;
import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonPointer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

public abstract class ValueNode extends BaseJsonNode {
   private static final long serialVersionUID = 1L;

   protected ValueNode() {
   }

   @Override
   protected JsonNode _at(JsonPointer ptr) {
      return null;
   }

   @Override
   public <T extends JsonNode> T deepCopy() {
      return (T)this;
   }

   @Override
   public abstract JsonToken asToken();

   @Override
   public void serializeWithType(JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(this, this.asToken()));
      this.serialize(g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public final JsonNode get(int index) {
      return null;
   }

   @Override
   public final JsonNode path(int index) {
      return MissingNode.getInstance();
   }

   @Override
   public final boolean has(int index) {
      return false;
   }

   @Override
   public final boolean hasNonNull(int index) {
      return false;
   }

   @Override
   public final JsonNode get(String fieldName) {
      return null;
   }

   @Override
   public final JsonNode path(String fieldName) {
      return MissingNode.getInstance();
   }

   @Override
   public final boolean has(String fieldName) {
      return false;
   }

   @Override
   public final boolean hasNonNull(String fieldName) {
      return false;
   }

   @Override
   public final JsonNode findValue(String fieldName) {
      return null;
   }

   public final ObjectNode findParent(String fieldName) {
      return null;
   }

   @Override
   public final List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
      return foundSoFar;
   }

   @Override
   public final List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
      return foundSoFar;
   }

   @Override
   public final List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
      return foundSoFar;
   }
}
