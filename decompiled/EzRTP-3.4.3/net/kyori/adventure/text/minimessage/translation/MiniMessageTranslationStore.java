package net.kyori.adventure.text.minimessage.translation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.TranslationStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface MiniMessageTranslationStore extends TranslationStore.StringBased<String> {
   @NotNull
   static MiniMessageTranslationStore create(@NotNull final Key name) {
      return create(name, MiniMessage.miniMessage());
   }

   @NotNull
   static MiniMessageTranslationStore create(@NotNull final Key name, @NotNull final MiniMessage miniMessage) {
      return new MiniMessageTranslationStoreImpl(name, miniMessage);
   }
}
