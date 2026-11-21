package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.process.ContextResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ValueResolver;
import org.jetbrains.annotations.NotNull;

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
