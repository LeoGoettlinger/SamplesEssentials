package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.ValueResolverFactory;

final class ResolverFactory {
   private final Object factory;

   public ResolverFactory(Object factory) {
      this.factory = factory;
   }

   @Nullable
   public Resolver create(@NotNull CommandParameter parameter) {
      Object resolver;
      if (this.factory instanceof ContextResolverFactory) {
         resolver = ((ContextResolverFactory)this.factory).create(parameter);
      } else {
         resolver = ((ValueResolverFactory)this.factory).create(parameter);
      }

      return resolver == null ? null : Resolver.wrap(resolver);
   }
}
