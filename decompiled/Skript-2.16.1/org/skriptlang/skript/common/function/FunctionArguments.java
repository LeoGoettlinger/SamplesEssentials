package org.skriptlang.skript.common.function;

import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface FunctionArguments permits FunctionArgumentsImpl {
   <T> T get(@NotNull String var1);

   <T> T getOrDefault(@NotNull String var1, T var2);

   <T> T getOrDefault(@NotNull String var1, Supplier<T> var2);

   @NotNull
   @Unmodifiable
   Set<String> names();
}
