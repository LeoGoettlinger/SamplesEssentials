package xyz.lychee.lagfixer.libs.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;

final class MessageFormatTranslationStore extends AbstractTranslationStore.StringBased<MessageFormat> implements TranslationRegistry {
   MessageFormatTranslationStore(final Key name) {
      super(name);
   }

   @NotNull
   protected MessageFormat parse(@NotNull final String string, @NotNull final Locale locale) {
      return new MessageFormat(string, locale);
   }

   @Nullable
   @Override
   public MessageFormat translate(@NotNull final String key, @NotNull final Locale locale) {
      return this.translationValue(key, locale);
   }
}
