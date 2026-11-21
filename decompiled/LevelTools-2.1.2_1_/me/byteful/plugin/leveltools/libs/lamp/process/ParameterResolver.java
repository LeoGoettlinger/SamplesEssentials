package me.byteful.plugin.leveltools.libs.lamp.process;

import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
