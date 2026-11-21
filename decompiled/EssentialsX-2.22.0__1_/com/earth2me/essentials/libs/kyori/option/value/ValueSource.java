package com.earth2me.essentials.libs.kyori.option.value;

import com.earth2me.essentials.libs.kyori.option.Option;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ValueSource {
   static ValueSource environmentVariable() {
      return environmentVariable("");
   }

   static ValueSource environmentVariable(final String prefix) {
      return new ValueSources.EnvironmentVariable(prefix);
   }

   static ValueSource systemProperty() {
      return systemProperty("");
   }

   static ValueSource systemProperty(final String prefix) {
      return new ValueSources.SystemProperty(prefix);
   }

   @Nullable
   <T> T value(final Option<T> option);
}
