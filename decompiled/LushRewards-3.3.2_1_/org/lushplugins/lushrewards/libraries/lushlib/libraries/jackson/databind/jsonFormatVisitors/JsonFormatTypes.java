package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors;

import java.util.HashMap;
import java.util.Map;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonValue;

public enum JsonFormatTypes {
   STRING,
   NUMBER,
   INTEGER,
   BOOLEAN,
   OBJECT,
   ARRAY,
   NULL,
   ANY;

   private static final Map<String, JsonFormatTypes> _byLCName = new HashMap<>();

   @JsonValue
   public String value() {
      return this.name().toLowerCase();
   }

   @JsonCreator
   public static JsonFormatTypes forValue(String s) {
      return _byLCName.get(s);
   }

   static {
      for (JsonFormatTypes t : values()) {
         _byLCName.put(t.name().toLowerCase(), t);
      }
   }
}
