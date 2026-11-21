package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonschema;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonValue;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.JsonNodeFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;

@Deprecated
public class JsonSchema {
   private final ObjectNode schema;

   @JsonCreator
   public JsonSchema(ObjectNode schema) {
      this.schema = schema;
   }

   @JsonValue
   public ObjectNode getSchemaNode() {
      return this.schema;
   }

   @Override
   public String toString() {
      return this.schema.toString();
   }

   @Override
   public int hashCode() {
      return this.schema.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (!(o instanceof JsonSchema)) {
         return false;
      } else {
         JsonSchema other = (JsonSchema)o;
         return this.schema == null ? other.schema == null : this.schema.equals(other.schema);
      }
   }

   public static JsonNode getDefaultSchemaNode() {
      ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
      objectNode.put("type", "any");
      return objectNode;
   }
}
