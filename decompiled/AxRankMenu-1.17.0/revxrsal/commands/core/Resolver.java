package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ValueResolver;

final class Resolver implements ParameterResolver<Object> {
   private final boolean mutates;
   private final ContextResolver<?> contextResolver;
   private final ValueResolver<?> valueResolver;

   public Resolver(ContextResolver<?> contextResolver, ValueResolver<?> valueResolver) {
      this.contextResolver = contextResolver;
      this.valueResolver = valueResolver;
      this.mutates = valueResolver != null;
   }

   @Override
   public boolean mutatesArguments() {
      return this.mutates;
   }

   @Override
   public Object resolve(@NotNull ParameterResolver.ParameterResolverContext context) {
      try {
         return this.valueResolver != null
            ? this.valueResolver.resolve((ValueResolver.ValueResolverContext)context)
            : this.contextResolver.resolve((ContextResolver.ContextResolverContext)context);
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public static Resolver wrap(Object resolver) {
      return resolver instanceof ValueResolver ? new Resolver(null, (ValueResolver<?>)resolver) : new Resolver((ContextResolver<?>)resolver, null);
   }
}
