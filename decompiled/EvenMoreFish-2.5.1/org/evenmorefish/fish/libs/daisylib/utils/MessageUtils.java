package org.evenmorefish.fish.libs.daisylib.utils;

import java.util.regex.Matcher;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MessageUtils {
   private static final char SECTION = '§';
   public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
      .character('&')
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER_SECTION = LegacyComponentSerializer.builder()
      .character('§')
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   public static final boolean PAPI_AVAILABLE = CommonUtils.classExists("me.clip.placeholderapi.PlaceholderAPI");

   private MessageUtils() {
   }

   public static boolean containsLegacy(@NonNull String message) {
      if (message.isEmpty()) {
         return false;
      } else if (!DaisyLib.Settings.ALLOW_LEGACY_MESSAGES.get()) {
         return false;
      } else {
         return message.contains(Character.toString('§')) ? true : MiniMessage.miniMessage().stripTags(message).equals(message);
      }
   }

   @NonNull
   public static Component parseString(@NonNull String message) {
      return parseString(message, MiniMessage.miniMessage());
   }

   @NonNull
   public static Component parseString(@NonNull String message, @NonNull MiniMessage miniMessage) {
      if (message.isEmpty()) {
         return Component.empty();
      } else if (containsLegacy(message)) {
         LegacyComponentSerializer serializer = message.contains(Character.toString('§')) ? LEGACY_COMPONENT_SERIALIZER_SECTION : LEGACY_COMPONENT_SERIALIZER;
         return serializer.deserialize(message);
      } else {
         return miniMessage.deserialize(message);
      }
   }

   public static boolean isEmpty(@NonNull Component component) {
      return PlainTextComponentSerializer.plainText().serialize(component).isEmpty();
   }

   public static Component parsePlaceholderAPI(@NonNull Component component, @Nullable OfflinePlayer player) {
      if (!PAPI_AVAILABLE) {
         DaisyLib.get().getLogging().debug("PlaceholderAPI not found. It's either not installed or not a dependency.");
         return component;
      } else {
         MiniMessage mm = MiniMessage.miniMessage();
         String input = (String)mm.serialize(component);
         if (!input.contains("%")) {
            return component;
         } else {
            Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(input);
            String result = matcher.replaceAll("<papi:$1>");
            return mm.deserialize(result, getPapiResolver(player));
         }
      }
   }

   @NonNull
   private static TagResolver getPapiResolver(@Nullable OfflinePlayer player) {
      return TagResolver.resolver("papi", (argumentQueue, context) -> {
         String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();
         String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, "%" + papiPlaceholder + "%");
         Component componentPlaceholder = LEGACY_COMPONENT_SERIALIZER_SECTION.deserialize(parsedPlaceholder);
         return Tag.selfClosingInserting(componentPlaceholder);
      });
   }
}
