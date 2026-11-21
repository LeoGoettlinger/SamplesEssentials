package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface JoinConfiguration permits JoinConfigurationImpl {
   static JoinConfiguration.Builder builder() {
      return new JoinConfigurationImpl.BuilderImpl();
   }

   static JoinConfiguration noSeparators() {
      return JoinConfigurationImpl.NULL;
   }

   static JoinConfiguration newlines() {
      return JoinConfigurationImpl.STANDARD_NEW_LINES;
   }

   static JoinConfiguration spaces() {
      return JoinConfigurationImpl.STANDARD_SPACES;
   }

   static JoinConfiguration commas(final boolean spaces) {
      return spaces ? JoinConfigurationImpl.STANDARD_COMMA_SPACE_SEPARATED : JoinConfigurationImpl.STANDARD_COMMA_SEPARATED;
   }

   static JoinConfiguration arrayLike() {
      return JoinConfigurationImpl.STANDARD_ARRAY_LIKE;
   }

   static JoinConfiguration separator(@Nullable final ComponentLike separator) {
      return (JoinConfiguration)(separator == null ? JoinConfigurationImpl.NULL : builder().separator(separator).build());
   }

   static JoinConfiguration separators(@Nullable final ComponentLike separator, @Nullable final ComponentLike lastSeparator) {
      return (JoinConfiguration)(separator == null && lastSeparator == null
         ? JoinConfigurationImpl.NULL
         : builder().separator(separator).lastSeparator(lastSeparator).build());
   }

   @Nullable
   Component prefix();

   @Nullable
   Component suffix();

   @Nullable
   Component separator();

   @Nullable
   Component lastSeparator();

   @Nullable
   Component lastSeparatorIfSerial();

   Function<ComponentLike, Component> convertor();

   Predicate<ComponentLike> predicate();

   Style parentStyle();

   public sealed interface Builder extends AbstractBuilder<JoinConfiguration> permits JoinConfigurationImpl.BuilderImpl {
      @Contract("_ -> this")
      JoinConfiguration.Builder prefix(@Nullable final ComponentLike prefix);

      @Contract("_ -> this")
      JoinConfiguration.Builder suffix(@Nullable final ComponentLike suffix);

      @Contract("_ -> this")
      JoinConfiguration.Builder separator(@Nullable final ComponentLike separator);

      @Contract("_ -> this")
      JoinConfiguration.Builder lastSeparator(@Nullable final ComponentLike lastSeparator);

      @Contract("_ -> this")
      JoinConfiguration.Builder lastSeparatorIfSerial(@Nullable final ComponentLike lastSeparatorIfSerial);

      @Contract("_ -> this")
      JoinConfiguration.Builder convertor(final Function<ComponentLike, Component> convertor);

      @Contract("_ -> this")
      JoinConfiguration.Builder predicate(final Predicate<ComponentLike> predicate);

      @Contract("_ -> this")
      JoinConfiguration.Builder parentStyle(final Style parentStyle);
   }
}
