package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson;

import com.earth2me.essentials.libs.kyori.adventure.key.Key;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

final class KeySerializer extends TypeAdapter<Key> {
   static final TypeAdapter<Key> INSTANCE = new KeySerializer().nullSafe();

   private KeySerializer() {
   }

   public void write(final JsonWriter out, final Key value) throws IOException {
      out.value(value.asString());
   }

   public Key read(final JsonReader in) throws IOException {
      return Key.key(in.nextString());
   }
}
