package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.serializer.ComponentSerializer;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GsonComponentSerializer implements ComponentSerializer<Component, Component, String>, JsonDeserializer<Component>, JsonSerializer<Component> {
   public static final GsonComponentSerializer INSTANCE = new GsonComponentSerializer();
   static final Gson GSON = populate(new GsonBuilder()).create();

   @NonNull
   public static GsonBuilder populate(@NonNull final GsonBuilder builder) {
      builder.registerTypeHierarchyAdapter(Component.class, ComponentSerializerImpl.INSTANCE)
         .registerTypeAdapter(Style.class, StyleSerializer.INSTANCE)
         .registerTypeAdapter(ClickEvent.Action.class, new NameMapSerializer<>("click action", ClickEvent.Action.NAMES))
         .registerTypeAdapter(HoverEvent.Action.class, new NameMapSerializer<>("hover action", HoverEvent.Action.NAMES))
         .registerTypeAdapter(TextColorWrapper.class, new TextColorWrapper.Serializer())
         .registerTypeAdapter(TextColor.class, new NameMapSerializer<>("text color", TextColor.NAMES))
         .registerTypeAdapter(TextDecoration.class, new NameMapSerializer<>("text decoration", TextDecoration.NAMES))
         .registerTypeHierarchyAdapter(BlockNbtComponent.Pos.class, BlockNbtComponentPosSerializer.INSTANCE);
      return builder;
   }

   @NonNull
   public Component deserialize(@NonNull final String string) {
      return (Component)GSON.fromJson(string, Component.class);
   }

   @NonNull
   public String serialize(@NonNull final Component component) {
      return GSON.toJson(component);
   }

   @Deprecated
   public Component deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      return ComponentSerializerImpl.INSTANCE.deserialize(json, typeOfT, context);
   }

   @Deprecated
   public JsonElement serialize(final Component src, final Type typeOfSrc, final JsonSerializationContext context) {
      return ComponentSerializerImpl.INSTANCE.serialize(src, typeOfSrc, context);
   }
}
