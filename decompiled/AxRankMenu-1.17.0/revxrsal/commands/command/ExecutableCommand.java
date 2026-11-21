package revxrsal.commands.command;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.process.ResponseHandler;

public interface ExecutableCommand extends CommandAnnotationHolder, PermissionHolder, Comparable<ExecutableCommand> {
   @NotNull
   String getName();

   @Range(
      from = 0L,
      to = Long.MAX_VALUE
   )
   int getId();

   @NotNull
   String getUsage();

   @Nullable
   String getDescription();

   @NotNull
   CommandPath getPath();

   @Nullable
   CommandCategory getParent();

   @NotNull
   @Unmodifiable
   List<CommandParameter> getParameters();

   @NotNull
   @Unmodifiable
   Map<Integer, CommandParameter> getValueParameters();

   @NotNull
   CommandHandler getCommandHandler();

   @NotNull
   <T> ResponseHandler<T> getResponseHandler();

   void execute(@NotNull CommandActor var1, @Nullable Collection<String> var2);

   void execute(@NotNull CommandActor var1, @Nullable String... var2);

   boolean isSecret();
}
