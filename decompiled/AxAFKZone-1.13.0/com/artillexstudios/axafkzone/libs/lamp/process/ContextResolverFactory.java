package com.artillexstudios.axafkzone.libs.lamp.process;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextResolverFactory {
   @Nullable
   ContextResolver<?> create(@NotNull CommandParameter var1);

   @NotNull
   static <T> ContextResolverFactory forType(Class<T> type, ContextResolver<T> resolver) {
      return parameter -> parameter.getType() == type ? resolver : null;
   }

   @NotNull
   static <T> ContextResolverFactory forHierarchyType(Class<T> type, ContextResolver<T> resolver) {
      return parameter -> parameter.getType() != type && !parameter.getType().isAssignableFrom(type) ? null : resolver;
   }
}
