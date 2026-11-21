package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;

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
