package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_9;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

public class ComponentConverter {
   public static class Serializer {
      private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

      private Serializer() {
      }

      static MutableComponent deserialize(JsonElement json, Provider registries) {
         return (MutableComponent)ComponentSerialization.CODEC
            .parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
            .getOrThrow(JsonParseException::new);
      }

      static JsonElement serialize(Component text, Provider registries) {
         return (JsonElement)ComponentSerialization.CODEC
            .encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), text)
            .getOrThrow(JsonParseException::new);
      }

      public static String toJson(Component text, Provider registries) {
         return GSON.toJson(serialize(text, registries));
      }

      @Nullable
      public static MutableComponent fromJson(String json, Provider registries) {
         JsonElement jsonelement = JsonParser.parseString(json);
         return jsonelement == null ? null : deserialize(jsonelement, registries);
      }

      @Nullable
      public static MutableComponent fromJson(@Nullable JsonElement json, Provider registries) {
         return json == null ? null : deserialize(json, registries);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String json, Provider registries) {
         JsonReader jsonreader = new JsonReader(new StringReader(json));
         jsonreader.setStrictness(Strictness.LENIENT);
         JsonElement jsonelement = JsonParser.parseReader(jsonreader);
         return jsonelement == null ? null : deserialize(jsonelement, registries);
      }
   }
}
