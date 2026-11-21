package com.artillexstudios.axtrade.libs.axapi.config.adapters.other;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axtrade.libs.axapi.database.DatabaseType;
import com.artillexstudios.axtrade.libs.axapi.database.DatabaseTypes;
import java.lang.reflect.Type;
import java.util.List;

public final class DatabaseTypeAdapter implements TypeAdapter<DatabaseType, String> {
   public DatabaseType deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof DatabaseType databaseType) {
         return databaseType;
      } else if (input instanceof String string) {
         return DatabaseTypes.fetch(string);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public String serialize(TypeAdapterHolder holder, DatabaseType value, Type type) {
      return value.name();
   }

   @Override
   public List<String> values(Type type) {
      return DatabaseTypes.keys();
   }
}
