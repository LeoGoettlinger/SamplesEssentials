package me.byteful.plugin.leveltools.libs.lamp.core;

import java.lang.reflect.Type;
import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.process.ParameterResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolverFactory;
import me.byteful.plugin.leveltools.libs.lamp.util.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum EitherValueResolverFactory implements ValueResolverFactory {
   INSTANCE;

   @Nullable
   @Override
   public ValueResolver<?> create(@NotNull CommandParameter parameter) {
      Class<?> rawType = parameter.getType();
      if (!Either.class.isAssignableFrom(rawType)) {
         return null;
      } else {
         Type[] types = EitherParameter.getTypes(parameter);
         EitherParameter first = generate(parameter, types[0]);
         EitherParameter second = generate(parameter, types[1]);
         return context -> {
            ArgumentStack original = context.arguments().copy();

            try {
               return Either.first(first.getResolver().resolve(context));
            } catch (Throwable var5x) {
               ((BaseCommandDispatcher.ValueContextR)context).argumentStack = original;
               return Either.second(second.getResolver().resolve(context));
            }
         };
      }
   }

   private static EitherParameter generate(CommandParameter parameter, Type type) {
      EitherParameter either = new EitherParameter(parameter, type);
      ParameterResolver<Object> resolver = ((BaseCommandHandler)parameter.getCommandHandler()).getResolver(either);
      if (resolver == null) {
         throw new IllegalStateException("Unable to find a resolver for parameter type " + either.getType());
      } else if (!resolver.mutatesArguments()) {
         throw new IllegalStateException("Only value-based arguments are allowed in the Either type (found " + either.getType() + ")");
      } else {
         either.setResolver(resolver);
         return either;
      }
   }
}
