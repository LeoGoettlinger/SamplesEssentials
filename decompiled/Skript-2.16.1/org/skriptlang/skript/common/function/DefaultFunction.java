package org.skriptlang.skript.common.function;

import ch.njol.skript.doc.Documentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;

public sealed interface DefaultFunction<T> extends Function<T>, Documentable permits DefaultFunctionImpl {
   @Contract("_, _, _ -> new")
   @NotNull
   static <T> DefaultFunction.Builder<T> builder(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
      return new DefaultFunctionImpl.BuilderImpl<>(source, name, returnType);
   }

   @NotNull
   SkriptAddon source();

   public interface Builder<T> {
      @Contract("_ -> this")
      DefaultFunction.Builder<T> contract(@NotNull ch.njol.skript.util.Contract var1);

      @Contract("_ -> this")
      DefaultFunction.Builder<T> description(@NotNull String... var1);

      @Contract("_ -> this")
      DefaultFunction.Builder<T> since(@NotNull String... var1);

      @Contract("_ -> this")
      DefaultFunction.Builder<T> examples(@NotNull String... var1);

      @Contract("_ -> this")
      DefaultFunction.Builder<T> keywords(@NotNull String... var1);

      @Contract("_ -> this")
      DefaultFunction.Builder<T> requires(@NotNull String... var1);

      @Contract("_, _, _ -> this")
      DefaultFunction.Builder<T> parameter(@NotNull String var1, @NotNull Class<?> var2, @NotNull Parameter.Modifier... var3);

      DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> var1);
   }
}
