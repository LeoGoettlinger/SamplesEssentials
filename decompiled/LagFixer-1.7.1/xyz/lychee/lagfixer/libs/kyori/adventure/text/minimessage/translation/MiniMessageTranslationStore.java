package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.translation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.MiniMessage;
import xyz.lychee.lagfixer.libs.kyori.adventure.translation.TranslationStore;

@ApiStatus.NonExtendable
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
