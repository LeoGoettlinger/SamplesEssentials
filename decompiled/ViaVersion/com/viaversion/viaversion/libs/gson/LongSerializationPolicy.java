package com.viaversion.viaversion.libs.gson;

import com.viaversion.viaversion.libs.gson.internal.bind.TypeAdapters;

public enum LongSerializationPolicy {
   DEFAULT {
      @Override
      public JsonElement serialize(Long value) {
         return (JsonElement)(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
      }

      @Override
      TypeAdapter<Number> typeAdapter() {
         return TypeAdapters.LONG;
      }
   },
   STRING {
      @Override
      public JsonElement serialize(Long value) {
         return (JsonElement)(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value.toString()));
      }

      @Override
      TypeAdapter<Number> typeAdapter() {
         return TypeAdapters.LONG_AS_STRING;
      }
   };

   private LongSerializationPolicy() {
   }

   public abstract JsonElement serialize(Long var1);

   abstract TypeAdapter<Number> typeAdapter();
}
