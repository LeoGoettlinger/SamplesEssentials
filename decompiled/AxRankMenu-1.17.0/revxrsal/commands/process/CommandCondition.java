package revxrsal.commands.process;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

public interface CommandCondition {
   void test(@NotNull CommandActor var1, @NotNull ExecutableCommand var2, @NotNull @Unmodifiable List<String> var3);
}
