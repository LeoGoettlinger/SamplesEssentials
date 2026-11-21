package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.trait.CommandAnnotationHolder;

public interface PermissionReader {
   @Nullable
   CommandPermission getPermission(@NotNull CommandAnnotationHolder var1);
}
