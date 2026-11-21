package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

class TextColorWrapper {
   @Nullable
   final TextColor color;
   @Nullable
   final TextDecoration decoration;
   final boolean reset;

   TextColorWrapper(@Nullable final TextColor color, @Nullable final TextDecoration decoration, final boolean reset) {
      this.color = color;
      this.decoration = decoration;
      this.reset = reset;
   }

   @Nullable
   private static TextColor deserializeColor(final JsonElement json, final JsonDeserializationContext context) {
      try {
         return (TextColor)context.deserialize(json, TextColor.class);
      } catch (JsonParseException var3) {
         return null;
      }
   }

   @Nullable
   private static TextDecoration deserializeDecoration(final JsonElement json, final JsonDeserializationContext context) {
      try {
         return (TextDecoration)context.deserialize(json, TextDecoration.class);
      } catch (JsonParseException var3) {
         return null;
      }
   }

   static class Serializer implements JsonDeserializer<TextColorWrapper> {
      public TextColorWrapper deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
         TextColor color = TextColorWrapper.deserializeColor(json, context);
         TextDecoration decoration = color == null ? TextColorWrapper.deserializeDecoration(json, context) : null;
         boolean reset = decoration == null && json.isJsonPrimitive() && json.getAsString().equals("reset");
         if (color == null && decoration == null && !reset) {
            throw new JsonParseException("Don't know how to parse " + json);
         } else {
            return new TextColorWrapper(color, decoration, reset);
         }
      }
   }
}
