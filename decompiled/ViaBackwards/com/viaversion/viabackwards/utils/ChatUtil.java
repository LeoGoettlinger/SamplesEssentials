package com.viaversion.viabackwards.utils;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.StringFormat;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling.ColorHandling;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling.DeserializerUnknownHandling;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ChatUtil {
   private static final Pattern UNUSED_COLOR_PATTERN = Pattern.compile("(?>(?>§[0-fk-or])*(§r|\\Z))|(?>(?>§[0-f])*(§[0-f]))");
   private static final Pattern UNUSED_COLOR_PATTERN_PREFIX = Pattern.compile("(?>(?>§[0-fk-or])*(§r))|(?>(?>§[0-f])*(§[0-f]))");

   public static Tag translate(String key, Tag... arguments) {
      TextComponent component = new TranslationComponent(key, Arrays.stream(arguments).map(SerializerVersion.V1_21_6::toComponent).toArray());
      return SerializerVersion.V1_21_6.toTag(component);
   }

   public static Tag translate(String key, Object... arguments) {
      TextComponent component = new TranslationComponent(key, arguments);
      return SerializerVersion.V1_21_6.toTag(component);
   }

   public static Tag[] split(Tag tag, String delimiter) {
      TextComponent component = SerializerVersion.V1_21_6.toComponent(tag);
      return Arrays.stream(TextUtils.split(component, delimiter, false)).map(SerializerVersion.V1_21_6::toTag).toArray(Tag[]::new);
   }

   public static Tag fixStyle(Tag tag) {
      TextComponent component = SerializerVersion.V1_21_6.toComponent(tag);
      if (component.getStyle().getColor() == null) {
         component.getStyle().setFormatting(TextFormatting.WHITE);
      }

      component.getStyle().setItalic(false);
      return SerializerVersion.V1_21_6.toTag(component);
   }

   public static CompoundTag translate(String key) {
      CompoundTag tag = new CompoundTag();
      tag.putString("translate", key);
      tag.putString("color", "white");
      tag.putBoolean("italic", false);
      return tag;
   }

   public static String removeUnusedColor(String legacy, char defaultColor) {
      return removeUnusedColor(legacy, defaultColor, false);
   }

   public static String legacyToJsonString(String legacy, String translation, boolean itemData) {
      return legacyToJsonString(legacy, text -> {
         text.append(new String[]{" "});
         text.append(new TranslationComponent(translation, new Object[0]));
      }, itemData);
   }

   public static String legacyToJsonString(String legacy, Consumer<TextComponent> consumer, boolean itemData) {
      TextComponent component = StringFormat.vanilla().fromString(legacy, ColorHandling.RESET, DeserializerUnknownHandling.WHITE);
      consumer.accept(component);
      if (itemData) {
         component.setParentStyle(new Style().setItalic(false));
      }

      return SerializerVersion.V1_12.toString(component);
   }

   public static String fromLegacy(String legacy, char defaultColor, int limit) {
      return fromLegacy(legacy, defaultColor, limit, false);
   }

   public static String fromLegacyPrefix(String legacy, char defaultColor, int limit) {
      return fromLegacy(legacy, defaultColor, limit, true);
   }

   public static String fromLegacy(String legacy, char defaultColor, int limit, boolean isPrefix) {
      legacy = removeUnusedColor(legacy, defaultColor, isPrefix);
      if (legacy.length() > limit) {
         legacy = legacy.substring(0, limit);
      }

      if (legacy.endsWith("§")) {
         legacy = legacy.substring(0, legacy.length() - 1);
      }

      return legacy;
   }

   public static String removeUnusedColor(String legacy, char defaultColor, boolean isPrefix) {
      if (legacy == null) {
         return null;
      } else {
         Pattern pattern = isPrefix ? UNUSED_COLOR_PATTERN_PREFIX : UNUSED_COLOR_PATTERN;
         legacy = pattern.matcher(legacy).replaceAll("$1$2");
         StringBuilder builder = new StringBuilder();
         ChatUtil.ChatFormattingState builderState = new ChatUtil.ChatFormattingState(defaultColor);
         ChatUtil.ChatFormattingState lastState = new ChatUtil.ChatFormattingState(defaultColor);

         for (int i = 0; i < legacy.length(); i++) {
            char current = legacy.charAt(i);
            if (current == 167 && i != legacy.length() - 1) {
               current = legacy.charAt(++i);
               lastState.processNextControlChar(current);
            } else {
               if (!lastState.equals(builderState)) {
                  lastState.appendTo(builder);
                  builderState = lastState.copy();
               }

               builder.append(current);
            }
         }

         if (isPrefix && !lastState.equals(builderState)) {
            lastState.appendTo(builder);
         }

         return builder.toString();
      }
   }

   private static class ChatFormattingState {
      private final Set<Character> formatting;
      private final char defaultColor;
      private char color;

      private ChatFormattingState(char defaultColor) {
         this(new HashSet<>(), defaultColor, defaultColor);
      }

      public ChatFormattingState(Set<Character> formatting, char defaultColor, char color) {
         this.formatting = formatting;
         this.defaultColor = defaultColor;
         this.color = color;
      }

      private void setColor(char newColor) {
         this.formatting.clear();
         this.color = newColor;
      }

      public ChatUtil.ChatFormattingState copy() {
         return new ChatUtil.ChatFormattingState(new HashSet<>(this.formatting), this.defaultColor, this.color);
      }

      public void appendTo(StringBuilder builder) {
         builder.append('§').append(this.color);

         for (Character formatCharacter : this.formatting) {
            builder.append('§').append(formatCharacter);
         }
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ChatUtil.ChatFormattingState that = (ChatUtil.ChatFormattingState)o;
            return this.defaultColor == that.defaultColor && this.color == that.color && Objects.equals(this.formatting, that.formatting);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.formatting, this.defaultColor, this.color);
      }

      public void processNextControlChar(char controlChar) {
         if (controlChar == 'r') {
            this.setColor(this.defaultColor);
         } else if (controlChar != 'l' && controlChar != 'm' && controlChar != 'n' && controlChar != 'o') {
            this.setColor(controlChar);
         } else {
            this.formatting.add(controlChar);
         }
      }
   }
}
