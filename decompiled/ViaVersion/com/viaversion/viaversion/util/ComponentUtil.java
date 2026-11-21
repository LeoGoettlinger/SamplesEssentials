package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.EntityHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.ItemHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.StringFormat;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling.ColorHandling;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling.DeserializerUnknownHandling;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentUtil {
   private static final int MAX_UNSIGNED_SHORT = 65535;

   public static JsonObject emptyJsonComponent() {
      return plainToJson("");
   }

   public static String emptyJsonComponentString() {
      return "{\"text\":\"\"}";
   }

   public static JsonObject plainToJson(String message) {
      JsonObject object = new JsonObject();
      object.addProperty("text", message);
      return object;
   }

   @Nullable
   public static JsonElement tagToJson(@Nullable Tag tag) {
      try {
         TextComponent component = SerializerVersion.V1_20_3.toComponent(tag);
         return component != null ? SerializerVersion.V1_19_4.toJson(component) : null;
      } catch (Exception var2) {
         if (Via.getConfig().logTextComponentConversionErrors()) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + StringUtil.forLogging(tag), (Throwable)var2);
         }

         return plainToJson("<error>");
      }
   }

   @Nullable
   public static Tag jsonToTag(@Nullable JsonElement element) {
      if (element != null && !element.isJsonNull()) {
         try {
            TextComponent component = SerializerVersion.V1_19_4.toComponent(element);
            return trimStrings(SerializerVersion.V1_20_3.toTag(component));
         } catch (Exception var2) {
            if (Via.getConfig().logTextComponentConversionErrors()) {
               Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + StringUtil.forLogging(element), (Throwable)var2);
            }

            return new StringTag("<error>");
         }
      } else {
         return null;
      }
   }

   public static Tag trimStrings(Tag input) {
      return input == null ? null : TagUtil.handleDeep(input, (key, tag) -> {
         if (tag instanceof StringTag stringTag) {
            String value = stringTag.getValue();
            if (value.length() > 21845 && value.getBytes(StandardCharsets.UTF_8).length > 65535) {
               stringTag.setValue("{}");
            }
         }

         return tag;
      });
   }

   @Nullable
   public static String tagToJsonString(@Nullable Tag tag) {
      try {
         TextComponent component = SerializerVersion.V1_20_5.toComponent(tag);
         return component != null ? SerializerVersion.V1_20_3.toString(component) : null;
      } catch (Exception var2) {
         if (Via.getConfig().logTextComponentConversionErrors()) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + StringUtil.forLogging(tag), (Throwable)var2);
         }

         return plainToJson("<error>").toString();
      }
   }

   @Nullable
   public static Tag jsonStringToTag(@Nullable String json) {
      return jsonStringToTag(json, SerializerVersion.V1_20_3, SerializerVersion.V1_20_5);
   }

   @Nullable
   public static Tag jsonStringToTag(@Nullable String json, SerializerVersion from, SerializerVersion to) {
      return json == null ? null : to.toTag(from.jsonSerializer.deserialize(json));
   }

   @Nullable
   public static JsonElement convertJson(@Nullable JsonElement element, SerializerVersion from, SerializerVersion to) {
      return element != null ? convert(from, to, from.toComponent(element)) : null;
   }

   @Nullable
   public static JsonElement convertJson(@Nullable String json, SerializerVersion from, SerializerVersion to) {
      return json != null ? convert(from, to, from.toComponent(json)) : null;
   }

   @Nullable
   public static JsonElement convertJsonOrEmpty(@Nullable String json, SerializerVersion from, SerializerVersion to) {
      TextComponent component = from.toComponent(json);
      return (JsonElement)(component == null ? emptyJsonComponent() : to.toJson(component));
   }

   private static JsonElement convert(SerializerVersion from, SerializerVersion to, TextComponent component) {
      if (from.ordinal() >= SerializerVersion.V1_16.ordinal() && to.ordinal() < SerializerVersion.V1_16.ordinal()) {
         component.forEach(c -> convertHoverToLegacy(to, c));
      }

      return to.toJson(component);
   }

   private static void convertHoverToLegacy(SerializerVersion to, TextComponent component) {
      if (component instanceof TranslationComponent translationComponent) {
         for (Object arg : translationComponent.getArgs()) {
            if (arg instanceof TextComponent componentArg) {
               convertHoverToLegacy(to, componentArg);
            }
         }
      }

      Style style = component.getStyle();
      HoverEvent hoverEvent = style.getHoverEvent();
      if (hoverEvent instanceof EntityHoverEvent entityHoverEvent && entityHoverEvent.isModern()) {
         EntityHoverEvent.ModernHolder entityData = entityHoverEvent.asModern();
         CompoundTag tag = new CompoundTag();
         tag.putString("type", entityData.getType().get());
         tag.putString("id", entityData.getUuid().toString());
         tag.putString("name", to.toString((TextComponent)(entityData.getName() != null ? entityData.getName() : new StringComponent(""))));
         entityHoverEvent.setLegacyData(new StringComponent(to.toSNBT(tag)));
      } else if (hoverEvent instanceof ItemHoverEvent itemHoverEvent && itemHoverEvent.isModern()) {
         ItemHoverEvent.ModernHolder itemData = itemHoverEvent.asModern();
         CompoundTag tag = new CompoundTag();
         tag.putString("id", itemData.getId().get());
         tag.putByte("Count", (byte)itemData.getCount());
         if (itemData.getTag() != null) {
            tag.put("tag", itemData.getTag());
         }

         itemHoverEvent.setLegacyData(to.toSNBT(tag));
      }
   }

   public static JsonElement legacyToJson(String message) {
      return SerializerVersion.V1_12.toJson(StringFormat.vanilla().fromString(message, ColorHandling.RESET, DeserializerUnknownHandling.WHITE));
   }

   public static String legacyToJsonString(String message) {
      return legacyToJsonString(message, false);
   }

   public static String legacyToJsonString(String message, boolean itemData) {
      TextComponent component = StringFormat.vanilla().fromString(message, ColorHandling.RESET, DeserializerUnknownHandling.WHITE);
      if (itemData) {
         TextUtils.iterateAll(component, c -> {
            if (!c.getStyle().isEmpty()) {
               c.setParentStyle(new Style().setItalic(false));
            }
         });
      }

      return SerializerVersion.V1_12.toString(component);
   }

   public static String jsonToLegacy(String value) {
      return TextComponentSerializer.V1_12.deserializeReader(value).asLegacyFormatString();
   }

   public static String jsonToLegacy(JsonElement value) {
      return SerializerVersion.V1_12.toComponent(value).asLegacyFormatString();
   }

   public static CompoundTag deserializeLegacyShowItem(JsonElement element, SerializerVersion version) {
      return (CompoundTag)version.toTag(version.toComponent(element).asUnformattedString());
   }

   public static CompoundTag deserializeShowItem(Tag value, SerializerVersion version) {
      return (CompoundTag)version.toTag(version.toComponent(value).asUnformattedString());
   }
}
