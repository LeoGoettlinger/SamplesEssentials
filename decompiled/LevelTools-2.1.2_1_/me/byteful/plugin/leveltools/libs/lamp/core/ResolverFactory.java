package me.byteful.plugin.leveltools.libs.lamp.core;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.process.ContextResolverFactory;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolverFactory;
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
