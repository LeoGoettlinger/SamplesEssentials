package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;

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
