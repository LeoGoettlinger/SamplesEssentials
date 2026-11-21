package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.util.formatting.text.util.NameMap;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public class NameMapSerializer<E extends Enum<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
   private final String name;
   private final NameMap<E> map;

   public NameMapSerializer(final String name, final NameMap<E> map) {
      this.name = name;
      this.map = map;
   }

   public E deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
      String string = json.getAsString();
      Optional<E> value = this.map.value(string);
      if (value.isPresent()) {
         return value.get();
      } else {
         throw new JsonParseException("invalid " + this.name + ":  " + string);
      }
   }

   public JsonElement serialize(final E src, final Type typeOfT, final JsonSerializationContext context) {
      Enum hack = Objects.requireNonNull(src);
      return new JsonPrimitive(this.map.name((E)hack));
   }
}
