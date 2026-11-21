package xyz.lychee.lagfixer.libs.kyori.option.value;

import org.jspecify.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.option.Option;

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
