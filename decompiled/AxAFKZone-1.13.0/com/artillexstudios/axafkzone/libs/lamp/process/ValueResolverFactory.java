package com.artillexstudios.axafkzone.libs.lamp.process;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ValueResolverFactory {
   @Nullable
   ValueResolver<?> create(@NotNull CommandParameter var1);

   @NotNull
   static <T> ValueResolverFactory forType(Class<T> type, ValueResolver<T> resolver) {
      return parameter -> parameter.getType() == type ? resolver : null;
   }

   @NotNull
   static <T> ValueResolverFactory forHierarchyType(Class<T> type, ValueResolver<T> resolver) {
      return parameter -> parameter.getType() != type && !parameter.getType().isAssignableFrom(type) ? null : resolver;
   }
}
