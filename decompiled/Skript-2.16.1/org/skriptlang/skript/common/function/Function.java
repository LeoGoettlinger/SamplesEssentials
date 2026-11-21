package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
@Internal
@Experimental
public interface Function<T> {
   T execute(@NotNull FunctionEvent<?> var1, @NotNull FunctionArguments var2);

   @NotNull
   Signature<T> signature();

   @Experimental
   boolean resetReturnValue();

   @Experimental
   @NotNull
   @Nullable
   String[] returnedKeys();
}
