package revxrsal.commands.core;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;

enum DependencyResolverFactory implements ContextResolverFactory {
   INSTANCE;

   @Nullable
   @Override
   public ContextResolver<?> create(@NotNull CommandParameter parameter) {
      if (!parameter.hasAnnotation(Dependency.class)) {
         return null;
      } else {
         Supplier<?> value = parameter.getCommandHandler().getDependency(parameter.getType());
         if (value == null) {
            throw new IllegalArgumentException(
               "Unable to resolve dependency for parameter " + parameter.getName() + " in " + parameter.getDeclaringCommand().getPath().toRealString()
            );
         } else {
            return context -> value.get();
         }
      }
   }
}
