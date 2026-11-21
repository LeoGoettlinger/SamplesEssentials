package com.artillexstudios.axtrade.libs.axapi.libraries.config;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axtrade.libs.axapi.libraries.Repository;
import java.lang.reflect.Type;

public final class RepositoryAdapter implements TypeAdapter<Repository, String> {
   public Repository deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof String string) {
         return new Repository(string);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public String serialize(TypeAdapterHolder holder, Repository value, Type type) {
      return value.url();
   }
}
