package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.lang.reflect.Type;

public class StyleSerializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
   public static final StyleSerializer INSTANCE = new StyleSerializer();
   private static final TextDecoration[] DECORATIONS = TextDecoration.values();
   static final boolean PRE_1215;
   static final String COLOR = "color";
   static final String INSERTION = "insertion";
   static final String CLICK_EVENT;
   static final String CLICK_EVENT_ACTION = "action";
   static final String CLICK_EVENT_VALUE = "value";
   static final String CLICK_EVENT_URL = "url";
   static final String CLICK_EVENT_COMMAND = "command";
   static final String CLICK_EVENT_PAGE = "page";
   static final String HOVER_EVENT;
   static final String HOVER_EVENT_ACTION = "action";
   static final String HOVER_EVENT_VALUE = "value";

   public Style deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      JsonObject object = json.getAsJsonObject();
      return this.deserialize(object, context);
   }

   private Style deserialize(final JsonObject json, final JsonDeserializationContext context) throws JsonParseException {
      Style.Builder style = Style.builder();
      if (json.has("color")) {
         TextColorWrapper color = (TextColorWrapper)context.deserialize(json.get("color"), TextColorWrapper.class);
         if (color.color != null) {
            style.color(color.color);
         } else if (color.decoration != null) {
            style.decoration(color.decoration, true);
         }
      }

      int i = 0;

      for (int length = DECORATIONS.length; i < length; i++) {
         TextDecoration decoration = DECORATIONS[i];
         String name = TextDecoration.NAMES.name(decoration);
         if (json.has(name)) {
            style.decoration(decoration, json.get(name).getAsBoolean());
         }
      }

      if (json.has("insertion")) {
         style.insertion(json.get("insertion").getAsString());
      }

      if (json.has(CLICK_EVENT)) {
         JsonObject clickEvent = json.getAsJsonObject(CLICK_EVENT);
         if (clickEvent != null) {
            JsonPrimitive rawAction = clickEvent.getAsJsonPrimitive("action");
            ClickEvent.Action action = rawAction == null ? null : (ClickEvent.Action)context.deserialize(rawAction, ClickEvent.Action.class);
            if (action != null && action.readable()) {
               JsonPrimitive rawValue = clickEvent.getAsJsonPrimitive(clickActionToKey(action));
               String value = rawValue == null ? null : rawValue.getAsString();
               if (value != null) {
                  style.clickEvent(ClickEvent.of(action, value));
               }
            }
         }
      }

      if (json.has(HOVER_EVENT)) {
         JsonObject hoverEvent = json.getAsJsonObject(HOVER_EVENT);
         if (hoverEvent != null) {
            JsonPrimitive rawAction = hoverEvent.getAsJsonPrimitive("action");
            HoverEvent.Action action = rawAction == null ? null : (HoverEvent.Action)context.deserialize(rawAction, HoverEvent.Action.class);
            if (action != null && action.readable()) {
               if (!PRE_1215 && action != HoverEvent.Action.SHOW_TEXT) {
                  throw new IllegalArgumentException("Don't know how to serialize " + hoverEvent);
               }

               JsonElement rawValue = hoverEvent.get("value");
               Component value = rawValue == null ? null : (Component)context.deserialize(rawValue, Component.class);
               if (value != null) {
                  style.hoverEvent(HoverEvent.of(action, value));
               }
            }
         }
      }

      return style.build();
   }

   public JsonElement serialize(final Style src, final Type typeOfSrc, final JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      TextColor color = src.color();
      if (color != null) {
         json.add("color", context.serialize(color));
      }

      int i = 0;

      for (int length = DECORATIONS.length; i < length; i++) {
         TextDecoration decoration = DECORATIONS[i];
         TextDecoration.State state = src.decoration(decoration);
         if (state != TextDecoration.State.NOT_SET) {
            String name = TextDecoration.NAMES.name(decoration);
            json.addProperty(name, state == TextDecoration.State.TRUE);
         }
      }

      String insertion = src.insertion();
      if (insertion != null) {
         json.add("insertion", context.serialize(insertion));
      }

      ClickEvent clickEvent = src.clickEvent();
      if (clickEvent != null) {
         JsonObject eventJson = new JsonObject();
         eventJson.add("action", context.serialize(clickEvent.action()));
         if (!PRE_1215 && clickEvent.action() == ClickEvent.Action.CHANGE_PAGE) {
            eventJson.addProperty("page", Integer.valueOf(clickEvent.value()));
         } else {
            eventJson.addProperty(clickActionToKey(clickEvent.action()), clickEvent.value());
         }

         json.add(CLICK_EVENT, eventJson);
      }

      HoverEvent hoverEvent = src.hoverEvent();
      if (hoverEvent != null) {
         if (!PRE_1215 && hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) {
            throw new IllegalArgumentException("Don't know how to serialize " + hoverEvent);
         }

         JsonObject eventJson = new JsonObject();
         eventJson.add("action", context.serialize(hoverEvent.action()));
         eventJson.add("value", context.serialize(hoverEvent.value()));
         json.add(HOVER_EVENT, eventJson);
      }

      return json;
   }

   private static String clickActionToKey(ClickEvent.Action action) {
      if (PRE_1215) {
         return "value";
      } else {
         return switch (action) {
            case OPEN_URL -> "url";
            case CHANGE_PAGE -> "page";
            case RUN_COMMAND, SUGGEST_COMMAND -> "command";
            case COPY_TO_CLIPBOARD -> "value";
            default -> throw new IllegalArgumentException("Can't convert action " + action + " to serialization key.");
         };
      }
   }

   static {
      boolean tmp = false;

      try {
         tmp = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.USER_COMMANDS).getDataVersion() < 4325;
      } catch (Exception var2) {
      }

      PRE_1215 = tmp;
      CLICK_EVENT = PRE_1215 ? "clickEvent" : "click_event";
      HOVER_EVENT = PRE_1215 ? "hoverEvent" : "hover_event";
   }
}
