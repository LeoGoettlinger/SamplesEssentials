package me.byteful.plugin.leveltools.libs.lamp.help;

import java.util.function.Predicate;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandHelpWriter<T> {
   @Nullable
   T generate(@NotNull ExecutableCommand var1, @NotNull CommandActor var2);

   default CommandHelpWriter<T> ignore(@NotNull Predicate<ExecutableCommand> predicate) {
      return (command, subject) -> predicate.test(command) ? null : this.generate(command, subject);
   }

   default CommandHelpWriter<T> only(@NotNull Predicate<ExecutableCommand> predicate) {
      return this.ignore(predicate.negate());
   }
}
