package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;

public interface JsonNumberFormatVisitor extends JsonValueFormatVisitor {
   void numberType(JsonParser.NumberType var1);

   public static class Base extends JsonValueFormatVisitor.Base implements JsonNumberFormatVisitor {
      @Override
      public void numberType(JsonParser.NumberType type) {
      }
   }
}
