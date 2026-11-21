package me.clip.placeholderapi.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

final class GsonHacks {
   private GsonHacks() {
   }

   static boolean isNullOrEmpty(@Nullable final JsonElement element) {
      return element == null
         || element.isJsonNull()
         || element.isJsonArray() && element.getAsJsonArray().size() == 0
         || element.isJsonObject() && element.getAsJsonObject().entrySet().isEmpty();
   }

   static boolean readBoolean(final JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.BOOLEAN) {
         return in.nextBoolean();
      } else if (peek == JsonToken.STRING) {
         return Boolean.parseBoolean(in.nextString());
      } else if (peek == JsonToken.NUMBER) {
         return in.nextString().equals("1");
      } else {
         throw new JsonParseException("Token of type " + peek + " cannot be interpreted as a boolean");
      }
   }

   static String readString(final JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.STRING || peek == JsonToken.NUMBER) {
         return in.nextString();
      } else if (peek == JsonToken.BOOLEAN) {
         return String.valueOf(in.nextBoolean());
      } else {
         throw new JsonParseException("Token of type " + peek + " cannot be interpreted as a string");
      }
   }
}
