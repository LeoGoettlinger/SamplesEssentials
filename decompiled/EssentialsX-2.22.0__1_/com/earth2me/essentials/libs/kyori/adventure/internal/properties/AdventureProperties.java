package com.earth2me.essentials.libs.kyori.adventure.internal.properties;

import com.earth2me.essentials.libs.kyori.adventure.util.PlatformAPI;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@Internal
public final class AdventureProperties {
   public static final AdventureProperties.Property<Boolean> DEBUG = property("debug", Boolean::parseBoolean, false);
   public static final AdventureProperties.Property<String> DEFAULT_TRANSLATION_LOCALE = property("defaultTranslationLocale", Function.identity(), null);
   public static final AdventureProperties.Property<Boolean> SERVICE_LOAD_FAILURES_ARE_FATAL = property(
      "serviceLoadFailuresAreFatal", Boolean::parseBoolean, Boolean.TRUE, false
   );
   public static final AdventureProperties.Property<Boolean> TEXT_WARN_WHEN_LEGACY_FORMATTING_DETECTED = property(
      "text.warnWhenLegacyFormattingDetected", Boolean::parseBoolean, Boolean.FALSE
   );
   public static final AdventureProperties.Property<Integer> DEFAULT_FLATTENER_NESTING_LIMIT = property("defaultFlattenerNestingLimit", Integer::parseInt, -1);

   private AdventureProperties() {
   }

   @NotNull
   public static <T> AdventureProperties.Property<T> property(
      @NotNull final String name, @NotNull final Function<String, T> parser, @Nullable final T defaultValue
   ) {
      return property(name, parser, defaultValue, true);
   }

   @NotNull
   public static <T> AdventureProperties.Property<T> property(
      @NotNull final String name, @NotNull final Function<String, T> parser, @Nullable final T defaultValue, final boolean allowProviderDefaultOverride
   ) {
      return AdventurePropertiesImpl.property(name, parser, defaultValue, allowProviderDefaultOverride);
   }

   @PlatformAPI
   @Internal
   public interface DefaultOverrideProvider {
      @Nullable
      <T> T overrideDefault(@NotNull final AdventureProperties.Property<T> property, @Nullable final T existingDefault);
   }

   @Internal
   @NonExtendable
   public interface Property<T> {
      @Nullable
      T value();

      @NotNull
      default T valueOr(@NotNull final T defaultValue) {
         T value = this.value();
         return value == null ? Objects.requireNonNull(defaultValue, "defaultValue") : value;
      }
   }
}
