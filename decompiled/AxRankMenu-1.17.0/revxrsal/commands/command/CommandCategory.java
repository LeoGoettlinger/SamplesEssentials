package revxrsal.commands.command;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.core.CommandPath;

public interface CommandCategory extends PermissionHolder, Comparable<CommandCategory> {
   @NotNull
   String getName();

   @NotNull
   CommandPath getPath();

   @NotNull
   CommandHandler getCommandHandler();

   @Nullable
   CommandCategory getParent();

   @Nullable
   ExecutableCommand getDefaultAction();

   @NotNull
   @Override
   CommandPermission getPermission();

   boolean isSecret();

   boolean isEmpty();

   @NotNull
   @UnmodifiableView
   Map<CommandPath, CommandCategory> getCategories();

   @NotNull
   @UnmodifiableView
   Map<CommandPath, ExecutableCommand> getCommands();
}
