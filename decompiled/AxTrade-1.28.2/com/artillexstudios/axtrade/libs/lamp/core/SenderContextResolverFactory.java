package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.annotation.NotSender;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.process.ContextResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ContextResolverFactory;
import com.artillexstudios.axtrade.libs.lamp.process.SenderResolver;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SenderContextResolverFactory implements ContextResolverFactory {
   private static final SenderResolver SELF = new SenderResolver() {
      @Override
      public boolean isCustomType(Class<?> type) {
         return CommandActor.class.isAssignableFrom(type);
      }

      @NotNull
      @Override
      public Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
         return actor;
      }
   };
   private final List<SenderResolver> resolvers;

   public SenderContextResolverFactory(List<SenderResolver> resolvers) {
      this.resolvers = resolvers;
      resolvers.add(SELF);
   }

   private static boolean definitelyNotSender(CommandParameter parameter) {
      return Arrays.stream(parameter.getJavaParameter().getAnnotations())
         .anyMatch(a -> a.annotationType().isAnnotationPresent(NotSender.ImpliesNotSender.class));
   }

   @Nullable
   @Override
   public ContextResolver<?> create(@NotNull CommandParameter parameter) {
      if (parameter.getMethodIndex() != 0) {
         return null;
      } else if (definitelyNotSender(parameter)) {
         return null;
      } else {
         for (SenderResolver resolver : this.resolvers) {
            if (resolver.isCustomType(parameter.getType())) {
               return context -> Preconditions.notNull(
                  resolver.getSender(parameter.getType(), context.actor(), context.command()), "SenderResolver#getSender() must not return null!"
               );
            }
         }

         return null;
      }
   }
}
