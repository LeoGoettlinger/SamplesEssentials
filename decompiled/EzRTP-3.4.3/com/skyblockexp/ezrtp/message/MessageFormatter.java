package com.skyblockexp.ezrtp.message;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Map;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;

public final class MessageFormatter {
   private MessageFormatter() {
   }

   public static Component format(String message, Map<String, String> placeholders) {
      if (message == null || message.isEmpty()) {
         return Component.empty();
      } else if (message != null && !message.isEmpty()) {
         String processed = message;
         if (placeholders != null && !placeholders.isEmpty()) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               processed = processed.replace("<" + entry.getKey() + ">", entry.getValue());
            }
         }

         return (processed.contains("&") || processed.contains("§")) && !processed.contains("<")
            ? fromLegacy(processed)
            : MessageUtil.parseMiniMessage(processed);
      } else {
         return Component.empty();
      }
   }

   public static Component format(String message) {
      return format(message, null);
   }

   public static String toLegacy(Component component) {
      return component == null ? "" : MessageUtil.componentToLegacy(component);
   }

   public static Component fromLegacy(String legacyMessage) {
      if (legacyMessage != null && !legacyMessage.isEmpty()) {
         String normalized = legacyMessage.replace('&', '§');

         try {
            return MessageUtil.legacyToComponent(normalized);
         } catch (Throwable var5) {
            try {
               return MessageUtil.parseMiniMessage(normalized);
            } catch (Throwable var4) {
               return Component.empty();
            }
         }
      } else {
         return Component.empty();
      }
   }

   public static String stripColors(String message) {
      return message == null ? "" : message.replaceAll("[§&][0-9A-FK-ORa-fk-or]", "");
   }
}
