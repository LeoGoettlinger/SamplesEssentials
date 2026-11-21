package net.kyori.adventure.text.minimessage.translation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.AbstractTranslationStore;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MiniMessageTranslationStoreImpl extends AbstractTranslationStore.StringBased<String> implements MiniMessageTranslationStore {
   private final MiniMessageTranslationStoreImpl.Translator translator;

   MiniMessageTranslationStoreImpl(@NotNull final Key name, @NotNull final MiniMessage miniMessage) {
      super(name);
      this.translator = new MiniMessageTranslationStoreImpl.Translator(Objects.requireNonNull(miniMessage, "miniMessage"));
   }

   @NotNull
   protected String parse(@NotNull final String string, @NotNull final Locale locale) {
      return string;
   }

   @Nullable
   @Override
   public MessageFormat translate(@NotNull final String key, @NotNull final Locale locale) {
      return null;
   }

   @Nullable
   @Override
   public Component translate(@NotNull final TranslatableComponent component, @NotNull final Locale locale) {
      return this.translator.translate(component, locale);
   }

   private final class Translator extends MiniMessageTranslator {
      private Translator(@NotNull final MiniMessage miniMessage) {
         super(miniMessage);
      }

      @Nullable
      @Override
      protected String getMiniMessageString(@NotNull final String key, @NotNull final Locale locale) {
         return MiniMessageTranslationStoreImpl.this.translationValue(key, locale);
      }

      @NotNull
      @Override
      public Key name() {
         return MiniMessageTranslationStoreImpl.this.name();
      }

      @NotNull
      @Override
      public TriState hasAnyTranslations() {
         return MiniMessageTranslationStoreImpl.this.hasAnyTranslations();
      }
   }
}
