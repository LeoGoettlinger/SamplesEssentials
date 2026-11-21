package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import com.sk89q.worldedit.util.formatting.text.BuildableComponent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.ComponentBuilder;
import com.sk89q.worldedit.util.formatting.text.EntityNbtComponent;
import com.sk89q.worldedit.util.formatting.text.KeybindComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponentBuilder;
import com.sk89q.worldedit.util.formatting.text.ScoreComponent;
import com.sk89q.worldedit.util.formatting.text.SelectorComponent;
import com.sk89q.worldedit.util.formatting.text.StorageNbtComponent;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.kyori.Key;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

final class ComponentSerializerImpl implements JsonDeserializer<Component>, JsonSerializer<Component> {
   static final ComponentSerializerImpl INSTANCE = new ComponentSerializerImpl();
   static final String TEXT = "text";
   static final String TRANSLATE = "translate";
   static final String TRANSLATE_WITH = "with";
   static final String SCORE = "score";
   static final String SCORE_NAME = "name";
   static final String SCORE_OBJECTIVE = "objective";
   static final String SCORE_VALUE = "value";
   static final String SELECTOR = "selector";
   static final String KEYBIND = "keybind";
   static final String EXTRA = "extra";
   static final String NBT = "nbt";
   static final String NBT_INTERPRET = "interpret";
   static final String NBT_BLOCK = "block";
   static final String NBT_ENTITY = "entity";
   static final String NBT_STORAGE = "storage";

   public Component deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      return this.deserialize0(json, context);
   }

   private BuildableComponent<?, ?> deserialize0(final JsonElement element, final JsonDeserializationContext context) throws JsonParseException {
      if (element.isJsonPrimitive()) {
         return TextComponent.of(element.getAsString());
      } else if (element.isJsonArray()) {
         ComponentBuilder<?, ?> parent = null;

         for (JsonElement childElement : element.getAsJsonArray()) {
            BuildableComponent<?, ?> child = this.deserialize0(childElement, context);
            if (parent == null) {
               parent = child.toBuilder();
            } else {
               parent.append(child);
            }
         }

         if (parent == null) {
            throw notSureHowToDeserialize(element);
         } else {
            return parent.build();
         }
      } else if (!element.isJsonObject()) {
         throw notSureHowToDeserialize(element);
      } else {
         JsonObject object = element.getAsJsonObject();
         ComponentBuilder<?, ?> component;
         if (object.has("text")) {
            component = TextComponent.builder(object.get("text").getAsString());
         } else if (object.has("translate")) {
            String key = object.get("translate").getAsString();
            if (!object.has("with")) {
               component = TranslatableComponent.builder(key);
            } else {
               JsonArray with = object.getAsJsonArray("with");
               List<Component> args = new ArrayList<>(with.size());
               int i = 0;

               for (int size = with.size(); i < size; i++) {
                  JsonElement argElement = with.get(i);
                  args.add(this.deserialize0(argElement, context));
               }

               component = TranslatableComponent.builder(key).args(args);
            }
         } else if (object.has("score")) {
            JsonObject score = object.getAsJsonObject("score");
            if (!score.has("name") || !score.has("objective")) {
               throw new JsonParseException("A score component requires a name and objective");
            }

            ScoreComponent.Builder builder = ScoreComponent.builder().name(score.get("name").getAsString()).objective(score.get("objective").getAsString());
            if (score.has("value")) {
               component = builder.value(score.get("value").getAsString());
            } else {
               component = builder;
            }
         } else if (object.has("selector")) {
            component = SelectorComponent.builder().pattern(object.get("selector").getAsString());
         } else if (object.has("keybind")) {
            component = KeybindComponent.builder().keybind(object.get("keybind").getAsString());
         } else {
            if (!object.has("nbt")) {
               throw notSureHowToDeserialize(element);
            }

            String nbt = object.get("nbt").getAsString();
            boolean interpret = object.has("interpret") && object.getAsJsonPrimitive("interpret").getAsBoolean();
            if (object.has("block")) {
               BlockNbtComponent.Pos pos = (BlockNbtComponent.Pos)context.deserialize(object.get("block"), BlockNbtComponent.Pos.class);
               component = ((BlockNbtComponent.Builder)nbt(BlockNbtComponent.builder(), nbt, interpret)).pos(pos);
            } else if (object.has("entity")) {
               component = ((EntityNbtComponent.Builder)nbt(EntityNbtComponent.builder(), nbt, interpret)).selector(object.get("entity").getAsString());
            } else {
               if (!object.has("storage")) {
                  throw notSureHowToDeserialize(element);
               }

               component = ((StorageNbtComponent.Builder)nbt(StorageNbtComponent.builder(), nbt, interpret))
                  .storage(Key.of(object.get("storage").getAsString()));
            }
         }

         if (object.has("extra")) {
            JsonArray extra = object.getAsJsonArray("extra");
            int i = 0;

            for (int size = extra.size(); i < size; i++) {
               JsonElement extraElement = extra.get(i);
               component.append(this.deserialize0(extraElement, context));
            }
         }

         Style style = (Style)context.deserialize(element, Style.class);
         if (!style.isEmpty()) {
            component.style(style);
         }

         return component.build();
      }
   }

   private static <C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>> B nbt(final B builder, final String nbt, final boolean interpret) {
      return builder.nbtPath(nbt).interpret(interpret);
   }

   public JsonElement serialize(final Component src, final Type typeOfSrc, final JsonSerializationContext context) {
      JsonObject object = new JsonObject();
      if (src instanceof TextComponent) {
         object.addProperty("text", ((TextComponent)src).content());
      } else if (src instanceof TranslatableComponent) {
         TranslatableComponent tc = (TranslatableComponent)src;
         object.addProperty("translate", tc.key());
         if (!tc.args().isEmpty()) {
            JsonArray with = new JsonArray();

            for (Component arg : tc.args()) {
               with.add(context.serialize(arg));
            }

            object.add("with", with);
         }
      } else if (src instanceof ScoreComponent) {
         ScoreComponent sc = (ScoreComponent)src;
         JsonObject score = new JsonObject();
         score.addProperty("name", sc.name());
         score.addProperty("objective", sc.objective());
         String value = sc.value();
         if (value != null) {
            score.addProperty("value", value);
         }

         object.add("score", score);
      } else if (src instanceof SelectorComponent) {
         object.addProperty("selector", ((SelectorComponent)src).pattern());
      } else if (src instanceof KeybindComponent) {
         object.addProperty("keybind", ((KeybindComponent)src).keybind());
      } else {
         if (!(src instanceof NbtComponent)) {
            throw notSureHowToSerialize(src);
         }

         NbtComponent<?, ?> nc = (NbtComponent<?, ?>)src;
         object.addProperty("nbt", nc.nbtPath());
         object.addProperty("interpret", nc.interpret());
         if (src instanceof BlockNbtComponent) {
            JsonElement position = context.serialize(((BlockNbtComponent)nc).pos());
            object.add("block", position);
         } else if (src instanceof EntityNbtComponent) {
            object.addProperty("entity", ((EntityNbtComponent)nc).selector());
         } else {
            if (!(src instanceof StorageNbtComponent)) {
               throw notSureHowToSerialize(src);
            }

            object.addProperty("storage", ((StorageNbtComponent)nc).storage().asString());
         }
      }

      List<Component> children = src.children();
      if (!children.isEmpty()) {
         JsonArray extra = new JsonArray();

         for (Component child : children) {
            extra.add(context.serialize(child));
         }

         object.add("extra", extra);
      }

      if (src.hasStyling()) {
         JsonElement style = context.serialize(src.style());
         if (style.isJsonObject()) {
            for (Entry<String, JsonElement> entry : ((JsonObject)style).entrySet()) {
               object.add(entry.getKey(), entry.getValue());
            }
         }
      }

      return object;
   }

   private static JsonParseException notSureHowToDeserialize(final JsonElement element) {
      return new JsonParseException("Don't know how to turn " + element + " into a Component");
   }

   private static IllegalArgumentException notSureHowToSerialize(final Component component) {
      return new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
   }
}
