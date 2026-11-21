package com.artillexstudios.axtrade.libs.lamp.process;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface ContextResolver<T> {
   T resolve(@NotNull ContextResolver.ContextResolverContext var1) throws Throwable;

   static <T> ContextResolver<T> of(@NotNull T value) {
      return context -> value;
   }

   static <T> ContextResolver<T> of(@NotNull Supplier<T> value) {
      return context -> value.get();
   }

   public interface ContextResolverContext extends ParameterResolver.ParameterResolverContext {
   }
}
