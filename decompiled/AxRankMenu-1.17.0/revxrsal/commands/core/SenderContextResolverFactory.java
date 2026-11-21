package revxrsal.commands.core;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.NotSender;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.util.Preconditions;

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
