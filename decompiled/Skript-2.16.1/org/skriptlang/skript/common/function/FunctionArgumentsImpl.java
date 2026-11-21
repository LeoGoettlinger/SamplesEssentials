package org.skriptlang.skript.common.function;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

record FunctionArgumentsImpl(@Unmodifiable @NotNull Map<String, Object> arguments) implements FunctionArguments {
   FunctionArgumentsImpl(@NotNull @Unmodifiable Map<String, Object> arguments) {
      Preconditions.checkNotNull(arguments, "arguments cannot be null");
      this.arguments = arguments;
   }

   @Override
   public <T> T get(@NotNull String name) {
      Preconditions.checkNotNull(name, "name cannot be null");
      return (T)this.arguments.get(name);
   }

   @Override
   public <T> T getOrDefault(@NotNull String name, T defaultValue) {
      Preconditions.checkNotNull(name, "name cannot be null");
      return (T)this.arguments.getOrDefault(name, defaultValue);
   }

   @Override
   public <T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue) {
      Preconditions.checkNotNull(name, "name cannot be null");
      Object existing = this.arguments.get(name);
      return (T)(existing == null ? defaultValue.get() : existing);
   }

   @NotNull
   @Unmodifiable
   @Override
   public Set<String> names() {
      return Collections.unmodifiableSet(this.arguments.keySet());
   }
}
