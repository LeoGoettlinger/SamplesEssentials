package org.evenmorefish.fish.libs.daisylib.messages;

import java.util.function.Function;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.MiniMessage.Builder;
import org.jspecify.annotations.NonNull;

public class MessageSettings {
   @NonNull
   private static Builder miniMessageBuilder = MiniMessage.builder().postProcessor(component -> component);
   @NonNull
   private static MiniMessage miniMessage = miniMessageBuilder.build();
   private static boolean allowEmptyAppend = false;
   private static boolean allowEmptyPrepend = false;
   private static boolean allowDebug = false;

   private MessageSettings() {
   }

   public static boolean isAllowEmptyAppend() {
      return allowEmptyAppend;
   }

   public static void setAllowEmptyAppend(boolean allow) {
      allowEmptyAppend = allow;
   }

   public static boolean isAllowEmptyPrepend() {
      return allowEmptyPrepend;
   }

   public static void setAllowEmptyPrepend(boolean allow) {
      allowEmptyPrepend = allow;
   }

   public static boolean isAllowDebug() {
      return allowDebug;
   }

   public static void setAllowDebug(boolean allow) {
      allowDebug = allow;
   }

   @NonNull
   public static MiniMessage getMiniMessage() {
      return miniMessage;
   }

   public static void setMiniMessage(@NonNull Builder builder) {
      miniMessageBuilder = builder;
      miniMessage = builder.build();
   }

   public static void editMiniMessage(@NonNull Function<Builder, Builder> editor) {
      miniMessageBuilder = editor.apply(miniMessageBuilder);
      miniMessage = miniMessageBuilder.build();
   }
}
