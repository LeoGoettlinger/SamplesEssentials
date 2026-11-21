package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.ShadowColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.JSONOptions;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ARGBLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Codec;
import de.theredend2000.advancedhunt.lib.kyori.option.OptionState;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;

final class StyleSerializer extends TypeAdapter<Style> {
   private static final TextDecoration[] DECORATIONS = new TextDecoration[]{
      TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.UNDERLINED, TextDecoration.STRIKETHROUGH, TextDecoration.OBFUSCATED
   };
   private final de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHover;
   private final boolean emitValueFieldHover;
   private final boolean emitCamelCaseHover;
   private final boolean emitSnakeCaseHover;
   private final boolean emitCamelCaseClick;
   private final boolean emitSnakeCaseClick;
   private final boolean strictEventValues;
   private final boolean emitShadowColor;
   private final Gson gson;

   static TypeAdapter<Style> create(
      @Nullable final de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHover,
      final OptionState features,
      final Gson gson
   ) {
      JSONOptions.HoverEventValueMode hoverMode = features.value(JSONOptions.EMIT_HOVER_EVENT_TYPE);
      JSONOptions.ClickEventValueMode clickMode = features.value(JSONOptions.EMIT_CLICK_EVENT_TYPE);
      return new StyleSerializer(
            legacyHover,
            hoverMode == JSONOptions.HoverEventValueMode.VALUE_FIELD || hoverMode == JSONOptions.HoverEventValueMode.ALL,
            hoverMode == JSONOptions.HoverEventValueMode.CAMEL_CASE || hoverMode == JSONOptions.HoverEventValueMode.ALL,
            hoverMode == JSONOptions.HoverEventValueMode.SNAKE_CASE || hoverMode == JSONOptions.HoverEventValueMode.ALL,
            clickMode == JSONOptions.ClickEventValueMode.CAMEL_CASE || clickMode == JSONOptions.ClickEventValueMode.BOTH,
            clickMode == JSONOptions.ClickEventValueMode.SNAKE_CASE || clickMode == JSONOptions.ClickEventValueMode.BOTH,
            features.value(JSONOptions.VALIDATE_STRICT_EVENTS),
            features.value(JSONOptions.SHADOW_COLOR_MODE) != JSONOptions.ShadowColorEmitMode.NONE,
            gson
         )
         .nullSafe();
   }

   private StyleSerializer(
      @Nullable final de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHover,
      final boolean emitValueFieldHover,
      final boolean emitCamelCaseHover,
      final boolean emitSnakeCaseHover,
      final boolean emitCamelCaseClick,
      final boolean emitSnakeCaseClick,
      final boolean strictEventValues,
      final boolean emitShadowColor,
      final Gson gson
   ) {
      this.legacyHover = legacyHover;
      this.emitValueFieldHover = emitValueFieldHover;
      this.emitCamelCaseHover = emitCamelCaseHover;
      this.emitSnakeCaseHover = emitSnakeCaseHover;
      this.emitCamelCaseClick = emitCamelCaseClick;
      this.emitSnakeCaseClick = emitSnakeCaseClick;
      this.strictEventValues = strictEventValues;
      this.emitShadowColor = emitShadowColor;
      this.gson = gson;
   }

   public Style read(final JsonReader in) throws IOException {
      in.beginObject();
      Style.Builder style = Style.style();

      while (in.hasNext()) {
         String fieldName = in.nextName();
         if (fieldName.equals("font")) {
            style.font((Key)this.gson.fromJson(in, SerializerFactory.KEY_TYPE));
         } else if (fieldName.equals("color")) {
            TextColorWrapper color = (TextColorWrapper)this.gson.fromJson(in, SerializerFactory.COLOR_WRAPPER_TYPE);
            if (color.color != null) {
               style.color(color.color);
            } else if (color.decoration != null) {
               style.decoration(color.decoration, TextDecoration.State.TRUE);
            }
         } else if (fieldName.equals("shadow_color")) {
            style.shadowColor((ARGBLike)this.gson.fromJson(in, SerializerFactory.SHADOW_COLOR_TYPE));
         } else if (TextDecoration.NAMES.keys().contains(fieldName)) {
            style.decoration(TextDecoration.NAMES.value(fieldName), GsonHacks.readBoolean(in));
         } else if (fieldName.equals("insertion")) {
            style.insertion(in.nextString());
         } else if (!fieldName.equals("click_event") && !fieldName.equals("clickEvent")) {
            if (!fieldName.equals("hover_event") && !fieldName.equals("hoverEvent")) {
               in.skipValue();
            } else {
               JsonObject hoverEventObject = (JsonObject)this.gson.fromJson(in, JsonObject.class);
               if (hoverEventObject != null) {
                  JsonPrimitive serializedAction = hoverEventObject.getAsJsonPrimitive("action");
                  if (serializedAction != null) {
                     HoverEvent.Action<Object> action = (HoverEvent.Action<Object>)this.gson.fromJson(serializedAction, SerializerFactory.HOVER_ACTION_TYPE);
                     if (action.readable()) {
                        Class<?> actionType = action.type();
                        Object value;
                        if (hoverEventObject.has("contents")) {
                           JsonElement rawValue = hoverEventObject.get("contents");
                           if (GsonHacks.isNullOrEmpty(rawValue)) {
                              if (this.strictEventValues) {
                                 throw ComponentSerializerImpl.notSureHowToDeserialize(rawValue);
                              }

                              value = null;
                           } else if (SerializerFactory.COMPONENT_TYPE.isAssignableFrom(actionType)) {
                              value = this.gson.fromJson(rawValue, SerializerFactory.COMPONENT_TYPE);
                           } else if (SerializerFactory.SHOW_ITEM_TYPE.isAssignableFrom(actionType)) {
                              value = this.gson.fromJson(rawValue, SerializerFactory.SHOW_ITEM_TYPE);
                           } else if (SerializerFactory.SHOW_ENTITY_TYPE.isAssignableFrom(actionType)) {
                              value = this.gson.fromJson(rawValue, SerializerFactory.SHOW_ENTITY_TYPE);
                           } else {
                              value = null;
                           }
                        } else if (hoverEventObject.has("value")) {
                           JsonElement element = hoverEventObject.get("value");
                           if (GsonHacks.isNullOrEmpty(element)) {
                              if (this.strictEventValues) {
                                 throw ComponentSerializerImpl.notSureHowToDeserialize(element);
                              }

                              value = null;
                           } else if (SerializerFactory.COMPONENT_TYPE.isAssignableFrom(actionType)) {
                              Component rawValue = (Component)this.gson.fromJson(element, SerializerFactory.COMPONENT_TYPE);
                              value = this.legacyHoverEventContents(action, rawValue);
                           } else if (SerializerFactory.STRING_TYPE.isAssignableFrom(actionType)) {
                              value = this.gson.fromJson(element, SerializerFactory.STRING_TYPE);
                           } else {
                              value = null;
                           }
                        } else if (SerializerFactory.SHOW_ITEM_TYPE.isAssignableFrom(actionType)) {
                           value = this.gson.fromJson(hoverEventObject, SerializerFactory.SHOW_ITEM_TYPE);
                        } else if (SerializerFactory.SHOW_ENTITY_TYPE.isAssignableFrom(actionType)) {
                           value = this.gson.fromJson(hoverEventObject, SerializerFactory.SHOW_ENTITY_TYPE);
                        } else {
                           if (this.strictEventValues) {
                              throw ComponentSerializerImpl.notSureHowToDeserialize(hoverEventObject);
                           }

                           value = null;
                        }

                        if (value != null) {
                           style.hoverEvent(HoverEvent.hoverEvent(action, value));
                        }
                     }
                  }
               }
            }
         } else {
            in.beginObject();
            ClickEvent.Action action = null;
            String valuex = null;

            while (in.hasNext()) {
               String clickEventField = in.nextName();
               if (clickEventField.equals("action")) {
                  action = (ClickEvent.Action)this.gson.fromJson(in, SerializerFactory.CLICK_ACTION_TYPE);
               } else if (!clickEventField.equals("value")
                  && !clickEventField.equals("url")
                  && !clickEventField.equals("path")
                  && !clickEventField.equals("command")
                  && !clickEventField.equals("page")) {
                  in.skipValue();
               } else if (in.peek() == JsonToken.NULL) {
                  if (this.strictEventValues) {
                     throw ComponentSerializerImpl.notSureHowToDeserialize(clickEventField);
                  }

                  in.nextNull();
               } else {
                  valuex = in.nextString();
               }
            }

            if (action != null && action.readable() && valuex != null) {
               style.clickEvent(ClickEvent.clickEvent(action, valuex));
            }

            in.endObject();
         }
      }

      in.endObject();
      return style.build();
   }

   private Object legacyHoverEventContents(final HoverEvent.Action<?> action, final Component rawValue) {
      if (action == HoverEvent.Action.SHOW_TEXT) {
         return rawValue;
      } else {
         if (this.legacyHover != null) {
            try {
               if (action == HoverEvent.Action.SHOW_ENTITY) {
                  return this.legacyHover.deserializeShowEntity(rawValue, this.decoder());
               }

               if (action == HoverEvent.Action.SHOW_ITEM) {
                  return this.legacyHover.deserializeShowItem(rawValue);
               }
            } catch (IOException var4) {
               throw new JsonParseException(var4);
            }
         }

         throw new UnsupportedOperationException();
      }
   }

   private Codec.Decoder<Component, String, JsonParseException> decoder() {
      return string -> (Component)this.gson.fromJson(string, SerializerFactory.COMPONENT_TYPE);
   }

   private Codec.Encoder<Component, String, JsonParseException> encoder() {
      return component -> this.gson.toJson(component, SerializerFactory.COMPONENT_TYPE);
   }

   public void write(final JsonWriter out, final Style value) throws IOException {
      out.beginObject();
      int i = 0;

      for (int length = DECORATIONS.length; i < length; i++) {
         TextDecoration decoration = DECORATIONS[i];
         TextDecoration.State state = value.decoration(decoration);
         if (state != TextDecoration.State.NOT_SET) {
            String name = TextDecoration.NAMES.key(decoration);

            assert name != null;

            out.name(name);
            out.value(state == TextDecoration.State.TRUE);
         }
      }

      TextColor color = value.color();
      if (color != null) {
         out.name("color");
         this.gson.toJson(color, SerializerFactory.COLOR_TYPE, out);
      }

      ShadowColor shadowColor = value.shadowColor();
      if (shadowColor != null && this.emitShadowColor) {
         out.name("shadow_color");
         this.gson.toJson(shadowColor, SerializerFactory.SHADOW_COLOR_TYPE, out);
      }

      String insertion = value.insertion();
      if (insertion != null) {
         out.name("insertion");
         out.value(insertion);
      }

      ClickEvent clickEvent = value.clickEvent();
      if (clickEvent != null) {
         ClickEvent.Action action = clickEvent.action();
         if (this.emitSnakeCaseClick) {
            out.name("click_event");
            out.beginObject();
            out.name("action");
            this.gson.toJson(action, SerializerFactory.CLICK_ACTION_TYPE, out);
            if (action == ClickEvent.Action.OPEN_URL) {
               out.name("url");
            } else if (action == ClickEvent.Action.OPEN_FILE) {
               out.name("path");
            } else if (action == ClickEvent.Action.RUN_COMMAND || action == ClickEvent.Action.SUGGEST_COMMAND) {
               out.name("command");
            } else if (action == ClickEvent.Action.CHANGE_PAGE) {
               out.name("page");
            } else {
               out.name("value");
            }

            if (action == ClickEvent.Action.CHANGE_PAGE) {
               out.value(Integer.parseInt(clickEvent.value()));
            } else if (action == ClickEvent.Action.OPEN_URL) {
               if (!clickEvent.value().startsWith("http://") && !clickEvent.value().startsWith("https://")) {
                  out.value("https://" + clickEvent.value());
               } else {
                  out.value(clickEvent.value());
               }
            } else {
               out.value(clickEvent.value());
            }

            out.endObject();
         }

         if (this.emitCamelCaseClick) {
            out.name("clickEvent");
            out.beginObject();
            out.name("action");
            this.gson.toJson(action, SerializerFactory.CLICK_ACTION_TYPE, out);
            out.name("value");
            out.value(clickEvent.value());
            out.endObject();
         }
      }

      HoverEvent<?> hoverEvent = value.hoverEvent();
      if (hoverEvent != null
         && ((this.emitSnakeCaseHover || this.emitCamelCaseHover) && hoverEvent.action() != HoverEvent.Action.SHOW_ACHIEVEMENT || this.emitValueFieldHover)) {
         HoverEvent.Action<?> actionx = hoverEvent.action();
         if (this.emitSnakeCaseHover && actionx != HoverEvent.Action.SHOW_ACHIEVEMENT) {
            out.name("hover_event");
            out.beginObject();
            out.name("action");
            this.gson.toJson(actionx, SerializerFactory.HOVER_ACTION_TYPE, out);
            if (actionx == HoverEvent.Action.SHOW_ITEM) {
               for (Entry<String, JsonElement> entry : this.gson.toJsonTree(hoverEvent.value(), SerializerFactory.SHOW_ITEM_TYPE).getAsJsonObject().entrySet()) {
                  out.name(entry.getKey());
                  this.gson.toJson(entry.getValue(), out);
               }
            } else if (actionx == HoverEvent.Action.SHOW_ENTITY) {
               for (Entry<String, JsonElement> entry : this.gson
                  .toJsonTree(hoverEvent.value(), SerializerFactory.SHOW_ENTITY_TYPE)
                  .getAsJsonObject()
                  .entrySet()) {
                  out.name(entry.getKey());
                  this.gson.toJson(entry.getValue(), out);
               }
            } else {
               if (actionx != HoverEvent.Action.SHOW_TEXT) {
                  throw new JsonParseException("Don't know how to serialize " + hoverEvent.value());
               }

               out.name("value");
               this.gson.toJson(hoverEvent.value(), SerializerFactory.COMPONENT_TYPE, out);
            }

            out.endObject();
         }

         if (this.emitCamelCaseHover || this.emitValueFieldHover) {
            out.name("hoverEvent");
            out.beginObject();
            out.name("action");
            this.gson.toJson(actionx, SerializerFactory.HOVER_ACTION_TYPE, out);
            if (this.emitCamelCaseHover && actionx != HoverEvent.Action.SHOW_ACHIEVEMENT) {
               out.name("contents");
               if (actionx == HoverEvent.Action.SHOW_ITEM) {
                  this.gson.toJson(hoverEvent.value(), SerializerFactory.SHOW_ITEM_TYPE, out);
               } else if (actionx == HoverEvent.Action.SHOW_ENTITY) {
                  this.gson.toJson(hoverEvent.value(), SerializerFactory.SHOW_ENTITY_TYPE, out);
               } else {
                  if (actionx != HoverEvent.Action.SHOW_TEXT) {
                     throw new JsonParseException("Don't know how to serialize " + hoverEvent.value());
                  }

                  this.gson.toJson(hoverEvent.value(), SerializerFactory.COMPONENT_TYPE, out);
               }
            }

            if (this.emitValueFieldHover) {
               out.name("value");
               this.serializeLegacyHoverEvent(hoverEvent, out);
            }

            out.endObject();
         }
      }

      Key font = value.font();
      if (font != null) {
         out.name("font");
         this.gson.toJson(font, SerializerFactory.KEY_TYPE, out);
      }

      out.endObject();
   }

   private void serializeLegacyHoverEvent(final HoverEvent<?> hoverEvent, final JsonWriter out) throws IOException {
      if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
         this.gson.toJson(hoverEvent.value(), SerializerFactory.COMPONENT_TYPE, out);
      } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
         this.gson.toJson(hoverEvent.value(), String.class, out);
      } else if (this.legacyHover != null) {
         Component serialized = null;

         try {
            if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
               serialized = this.legacyHover.serializeShowEntity((HoverEvent.ShowEntity)hoverEvent.value(), this.encoder());
            } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
               serialized = this.legacyHover.serializeShowItem((HoverEvent.ShowItem)hoverEvent.value());
            }
         } catch (IOException var5) {
            throw new JsonSyntaxException(var5);
         }

         if (serialized != null) {
            this.gson.toJson(serialized, SerializerFactory.COMPONENT_TYPE, out);
         } else {
            out.nullValue();
         }
      } else {
         out.nullValue();
      }
   }

   static {
      Set<TextDecoration> knownDecorations = EnumSet.allOf(TextDecoration.class);

      for (TextDecoration decoration : DECORATIONS) {
         knownDecorations.remove(decoration);
      }

      if (!knownDecorations.isEmpty()) {
         throw new IllegalStateException("Gson serializer is missing some text decorations: " + knownDecorations);
      }
   }
}
