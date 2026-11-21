package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

public interface ParameterValidator<T> {
   void validate(T var1, @NotNull CommandParameter var2, @NotNull CommandActor var3);
}
