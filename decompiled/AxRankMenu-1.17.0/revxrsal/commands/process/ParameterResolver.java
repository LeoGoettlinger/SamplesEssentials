package revxrsal.commands.process;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;

public interface ParameterResolver<T> {
   boolean mutatesArguments();

   @Nullable
   T resolve(@NotNull ParameterResolver.ParameterResolverContext var1);

   public interface ParameterResolverContext {
      @NotNull
      @Unmodifiable
      List<String> input();

      @NotNull
      <A extends CommandActor> A actor();

      @NotNull
      CommandParameter parameter();

      @NotNull
      ExecutableCommand command();

      @NotNull
      CommandHandler commandHandler();

      @NotNull
      <T> T getResolvedArgument(@NotNull Class<T> var1);

      @NotNull
      <T> T getResolvedParameter(@NotNull CommandParameter var1);
   }
}
