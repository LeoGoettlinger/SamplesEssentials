package su.nightexpress.nightcore.locale.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightSound;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.AbstractSound;
import su.nightexpress.nightcore.util.sound.VanillaSound;

public record MessageData(@NonNull MessageType type, boolean usePrefix, boolean replacePlaceholders, @Nullable NightSound sound, int[] titleTimes) {
   public static final MessageData CHAT_DEFAULT = chat().build();
   public static final MessageData CHAT_NO_PREFIX = chat().usePrefix(false).build();
   public static final MessageData ACTION_BAR = actionBar().build();
   public static final MessageData TITLES = titles().build();
   private static final Pattern ENTRY_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");
   private static final Pattern LEGACY_TAG_PATTERN = Pattern.compile("<(\\w+)(?:\\s*:\\s*\"([^\"]*)\")?>");
   private static final String OPEN_BRACKET = "[";
   private static final String CLOSE_BRACKET = "]";

   @NonNull
   public String serialize() {
      Map<MessageData.Option, String> values = new HashMap<>();
      if (this.type == MessageType.CHAT) {
         if (!this.usePrefix) {
            values.put(MessageData.Option.PREFIX, Boolean.toString(false));
         }

         if (this.replacePlaceholders) {
            values.put(MessageData.Option.PLACEHOLDERS, Boolean.toString(true));
         }
      }

      if (this.sound != null) {
         values.put(MessageData.Option.SOUND, this.sound.serialize());
      }

      if (this.titleTimes != null && this.titleTimes.length == 3 && this.type == MessageType.TITLE) {
         values.put(MessageData.Option.TITLE_TIMES, IntStream.of(this.titleTimes).mapToObj(String::valueOf).collect(Collectors.joining(":")));
      }

      if (this.type != MessageType.CHAT) {
         values.put(MessageData.Option.TYPE, this.type.name().toLowerCase());
      }

      if (values.isEmpty()) {
         return "";
      } else {
         String serialized = values.entrySet()
            .stream()
            .filter(e -> e.getValue() != null)
            .map(entry -> entry.getKey().name().toLowerCase() + "=\"" + entry.getValue() + "\"")
            .collect(Collectors.joining(","));
         return "[" + serialized + "]";
      }
   }

   @NonNull
   public static MessageData.Builder builder() {
      return new MessageData.Builder();
   }

   @NonNull
   public static MessageData.Builder chat() {
      return builder().type(MessageType.CHAT);
   }

   @NonNull
   public static MessageData.Builder actionBar() {
      return builder().type(MessageType.ACTION_BAR);
   }

   @NonNull
   public static MessageData.Builder titles() {
      return builder().type(MessageType.TITLE).titleTimes(20, 60, 20);
   }

   @NonNull
   public static String extractAndParse(@NonNull String string, @NonNull MessageData.Builder builder) {
      if (!string.startsWith("[")) {
         return string;
      } else {
         int endIndex = string.indexOf("]");
         if (endIndex < 0) {
            return string;
         } else {
            String paramsText = string.substring(1, endIndex);
            String text = string.substring(endIndex + 1);
            Matcher matcher = ENTRY_PATTERN.matcher(paramsText);

            while (matcher.find()) {
               String key = matcher.group(1);
               String value = matcher.group(2);
               MessageData.Option option = Enums.get(key, MessageData.Option.class);
               if (option != null) {
                  parseOption(option, value, builder);
               }
            }

            return text.trim();
         }
      }
   }

   public static boolean hasLegacyData(@NonNull String string) {
      return string.startsWith("<sound:") || string.startsWith("<papi>") || string.startsWith("<noprefix>") || string.startsWith("<output:");
   }

   @NonNull
   public static String extractAndParseOld(@NonNull String string, @NonNull MessageData.Builder builder) {
      Matcher matcher = LEGACY_TAG_PATTERN.matcher(string);
      StringBuilder remainderBuffer = new StringBuilder();

      while (matcher.find()) {
         String key = matcher.group(1);
         String value = matcher.group(2);
         if (key.equalsIgnoreCase("papi")) {
            key = MessageData.Option.PLACEHOLDERS.name();
            value = "true";
         } else if (key.equalsIgnoreCase("noprefix")) {
            key = MessageData.Option.PREFIX.name();
            value = "false";
         } else if (key.equalsIgnoreCase("sound")) {
            value = value.replace(':', ';').replace("minecraft;", "minecraft:");
         } else if (key.equalsIgnoreCase("output")) {
            if (value.startsWith("titles:")) {
               parseOption(MessageData.Option.TITLE_TIMES, value.substring("titles:".length()), builder);
               parseOption(MessageData.Option.TYPE, MessageType.TITLE.name(), builder);
               matcher.appendReplacement(remainderBuffer, "");
               continue;
            }

            key = MessageData.Option.TYPE.name();
            value = value.replace("none", MessageType.SILENT.name());
         }

         MessageData.Option option = Enums.get(key, MessageData.Option.class);
         if (option != null) {
            parseOption(option, value, builder);
            matcher.appendReplacement(remainderBuffer, "");
         }
      }

      matcher.appendTail(remainderBuffer);
      return remainderBuffer.toString().trim();
   }

   private static void parseOption(@NonNull MessageData.Option option, @NonNull String value, @NonNull MessageData.Builder builder) {
      switch (option) {
         case PREFIX:
            builder.usePrefix(Boolean.parseBoolean(value));
            break;
         case SOUND:
            builder.sound(AbstractSound.deserialize(value));
            break;
         case PLACEHOLDERS:
            builder.replacePlaceholders(Boolean.parseBoolean(value));
            break;
         case TITLE_TIMES:
            String[] split = value.split(":");
            int length = split.length;
            int fadeIn = NumberUtil.getAnyInteger(split[0], 20);
            int stay = length >= 2 ? NumberUtil.getAnyInteger(split[1], 60) : 60;
            int fadeOut = length >= 3 ? NumberUtil.getAnyInteger(split[2], 20) : fadeIn;
            builder.titleTimes(fadeIn, stay, fadeOut);
            break;
         case TYPE:
            builder.type(Enums.parse(value, MessageType.class).orElse(MessageType.CHAT));
      }
   }

   @NonNull
   @Override
   public String toString() {
      return "MessageData{, type="
         + this.type
         + ", usePrefix="
         + this.usePrefix
         + ", replacePlaceholders="
         + this.replacePlaceholders
         + ", sound="
         + this.sound
         + ", titleTimes="
         + Arrays.toString(this.titleTimes)
         + "}";
   }

   public static class Builder {
      private MessageType type = MessageType.CHAT;
      private boolean usePrefix = true;
      private boolean replacePlaceholders = false;
      private NightSound sound;
      private int[] titleTimes;

      @NonNull
      public MessageData build() {
         return new MessageData(this.type, this.usePrefix, this.replacePlaceholders, this.sound, this.titleTimes);
      }

      @NonNull
      public MessageData.Builder type(@NonNull MessageType type) {
         this.type = type;
         return this;
      }

      @NonNull
      public MessageData.Builder usePrefix(boolean usePrefix) {
         this.usePrefix = usePrefix;
         return this;
      }

      @NonNull
      public MessageData.Builder replacePlaceholders(boolean replacePlaceholders) {
         this.replacePlaceholders = replacePlaceholders;
         return this;
      }

      @NonNull
      public MessageData.Builder sound(@Nullable Sound sound) {
         return this.sound(sound == null ? null : VanillaSound.of(sound));
      }

      @NonNull
      public MessageData.Builder sound(@Nullable NightSound sound) {
         this.sound = sound;
         return this;
      }

      @NonNull
      public MessageData.Builder titleTimes(int fadeIn, int stay, int fadeOut) {
         this.titleTimes = new int[]{fadeIn, stay, fadeOut};
         return this;
      }
   }

   public static enum Option {
      PREFIX,
      SOUND,
      PLACEHOLDERS,
      TITLE_TIMES,
      TYPE;
   }
}
