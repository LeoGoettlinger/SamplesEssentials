package eu.raidersheaven.rhsignitem.handlers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class ColorFormatHandler {
   private static final MiniMessage msg = MiniMessage.miniMessage();
   private static final Pattern HEX_PATTERN = Pattern.compile("&(#[0-9a-fA-F]{6})");
   private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fA-Fk-oK-OrR])");

   public static String convertToMiniMessage(String text) {
      if (text != null && !text.isEmpty()) {
         Matcher hexMatcher = HEX_PATTERN.matcher(text);

         while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            text = text.replace("&" + hexColor, "<" + hexColor + ">");
         }

         Matcher legacyMatcher = LEGACY_PATTERN.matcher(text);

         while (legacyMatcher.find()) {
            String legacyCode = legacyMatcher.group(1);
            String miniMessageColor = convertLegacyToMiniMessage(legacyCode);
            text = text.replace("&" + legacyCode, miniMessageColor);
         }

         return text;
      } else {
         return "";
      }
   }

   private static String convertLegacyToMiniMessage(String code) {
      String var1 = code.toLowerCase();

      return switch (var1) {
         case "0" -> "<black>";
         case "1" -> "<dark_blue>";
         case "2" -> "<dark_green>";
         case "3" -> "<dark_aqua>";
         case "4" -> "<dark_red>";
         case "5" -> "<dark_purple>";
         case "6" -> "<gold>";
         case "7" -> "<gray>";
         case "8" -> "<dark_gray>";
         case "9" -> "<blue>";
         case "a" -> "<green>";
         case "b" -> "<aqua>";
         case "c" -> "<red>";
         case "d" -> "<light_purple>";
         case "e" -> "<yellow>";
         case "f" -> "<white>";
         case "k" -> "<obfuscated>";
         case "l" -> "<bold>";
         case "m" -> "<strikethrough>";
         case "n" -> "<underlined>";
         case "o" -> "<italic>";
         case "r" -> "<reset>";
         default -> "";
      };
   }

   public static Component formatComponent(String content) {
      if (content != null && !content.isEmpty()) {
         content = convertToMiniMessage(content);
         return msg.deserialize(content).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
      } else {
         return Component.empty();
      }
   }

   public static String formatString(String content) {
      return content != null && !content.isEmpty() ? convertToMiniMessage(content) : "";
   }

   public static List<Component> formatStringList(List<String> lines) {
      return lines != null && !lines.isEmpty() ? lines.stream().map(ColorFormatHandler::formatComponent).toList() : List.of();
   }
}
