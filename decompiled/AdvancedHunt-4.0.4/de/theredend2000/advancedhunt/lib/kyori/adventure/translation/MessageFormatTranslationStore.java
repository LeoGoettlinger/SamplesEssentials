package de.theredend2000.advancedhunt.lib.kyori.adventure.translation;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import java.text.MessageFormat;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

final class MessageFormatTranslationStore extends AbstractTranslationStore.StringBased<MessageFormat> {
   MessageFormatTranslationStore(final Key name) {
      super(name);
   }

   protected MessageFormat parse(final String string, final Locale locale) {
      return new MessageFormat(string, locale);
   }

   @Nullable
   @Override
   public MessageFormat translate(final String key, final Locale locale) {
      return this.translationValue(key, locale);
   }
}
