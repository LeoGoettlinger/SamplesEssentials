package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.process.ContextResolverFactory;
import com.artillexstudios.axtrade.libs.lamp.process.ValueResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
