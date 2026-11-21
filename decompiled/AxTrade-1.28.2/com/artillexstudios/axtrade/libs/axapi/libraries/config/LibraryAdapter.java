package com.artillexstudios.axtrade.libs.axapi.libraries.config;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axtrade.libs.axapi.libraries.Library;
import com.artillexstudios.axtrade.libs.axapi.utils.UncheckedUtils;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LibraryAdapter implements TypeAdapter<Library, Map<String, Object>> {
   public Library deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof Map<?, ?> map) {
         return new Library(
            (String)map.get("group"),
            (String)map.get("artifact-id"),
            (String)map.get("version"),
            (String)map.get("classifier"),
            UncheckedUtils.unsafeCast(
               holder.deserialize(map.get("transitive-dependencies"), TypeToken.getParameterized(List.class, new Type[]{Library.class}).getType())
            )
         );
      } else {
         throw new IllegalArgumentException();
      }
   }

   public Map<String, Object> serialize(TypeAdapterHolder holder, Library value, Type type) {
      Map<String, Object> serialized = new HashMap<>();
      serialized.put("group", value.group());
      serialized.put("artifact-id", value.artifactId());
      serialized.put("version", value.version());
      serialized.put("classifier", value.classifier());
      serialized.put("transitive-dependencies", holder.serialize(value.transitiveDependencies(), List.class));
      return serialized;
   }
}
