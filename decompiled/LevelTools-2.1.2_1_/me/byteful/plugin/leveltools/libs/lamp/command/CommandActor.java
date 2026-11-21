package me.byteful.plugin.leveltools.libs.lamp.command;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.locales.Translator;
import org.jetbrains.annotations.NotNull;

public interface CommandActor {
   @NotNull
   String getName();

   @NotNull
   UUID getUniqueId();

   void reply(@NotNull String var1);

   void error(@NotNull String var1);

   CommandHandler getCommandHandler();

   default Translator getTranslator() {
      return this.getCommandHandler().getTranslator();
   }

   @NotNull
   default Locale getLocale() {
      return this.getTranslator().getLocale();
   }

   default void replyLocalized(@NotNull String key, Object... args) {
      String message = MessageFormat.format(this.getTranslator().get(key, this.getLocale()), args);
      this.reply(message);
   }

   default void errorLocalized(@NotNull String key, Object... args) {
      String message = MessageFormat.format(this.getTranslator().get(key, this.getLocale()), args);
      this.error(message);
   }

   default <T> Optional<T> dispatch(@NotNull String input) {
      return this.getCommandHandler().dispatch(this, input);
   }

   default <T> Optional<T> dispatch(@NotNull ArgumentStack input) {
      return this.getCommandHandler().dispatch(this, input);
   }

   default <T extends CommandActor> T as(@NotNull Class<T> type) {
      return type.cast(this);
   }
}
