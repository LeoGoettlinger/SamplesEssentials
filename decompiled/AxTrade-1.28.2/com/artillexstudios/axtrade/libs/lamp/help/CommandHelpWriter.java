package com.artillexstudios.axtrade.libs.lamp.help;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import java.util.function.Predicate;
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
