package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public interface JsonArrayFormatVisitor extends JsonFormatVisitorWithSerializerProvider {
   void itemsFormat(JsonFormatVisitable var1, JavaType var2) throws JsonMappingException;

   void itemsFormat(JsonFormatTypes var1) throws JsonMappingException;

   public static class Base implements JsonArrayFormatVisitor {
      protected SerializerProvider _provider;

      public Base() {
      }

      public Base(SerializerProvider p) {
         this._provider = p;
      }

      @Override
      public SerializerProvider getProvider() {
         return this._provider;
      }

      @Override
      public void setProvider(SerializerProvider p) {
         this._provider = p;
      }

      @Override
      public void itemsFormat(JsonFormatVisitable handler, JavaType elementType) throws JsonMappingException {
      }

      @Override
      public void itemsFormat(JsonFormatTypes format) throws JsonMappingException {
      }
   }
}
