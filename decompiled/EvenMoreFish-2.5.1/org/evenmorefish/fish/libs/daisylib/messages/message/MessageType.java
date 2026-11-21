package org.evenmorefish.fish.libs.daisylib.messages.message;

import java.util.function.BiConsumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public enum MessageType {
   CHAT(Audience::sendMessage),
   ACTION_BAR(Audience::sendActionBar),
   TITLE((audience, component) -> {
      Title title = Title.title(component, Component.empty());
      audience.showTitle(title);
   }),
   SUBTITLE((audience, component) -> {
      Title title = Title.title(Component.empty(), component);
      audience.showTitle(title);
   });

   private final BiConsumer<Audience, Component> consumer;

   private MessageType(@NonNull BiConsumer<Audience, Component> consumer) {
      this.consumer = consumer;
   }

   public void send(@Nullable Audience audience, @NonNull Component message) {
      if (audience != null) {
         this.consumer.accept(audience, message);
      }
   }

   @NonNull
   public static MessageType getFromString(@Nullable String type) {
      if (type == null) {
         return CHAT;
      } else {
         try {
            return valueOf(type.toUpperCase());
         } catch (IllegalArgumentException var2) {
            return CHAT;
         }
      }
   }
}
