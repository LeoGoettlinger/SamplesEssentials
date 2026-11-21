package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

public interface SenderResolver {
   boolean isCustomType(Class<?> var1);

   @NotNull
   Object getSender(@NotNull Class<?> var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3);
}
