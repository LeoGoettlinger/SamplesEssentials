package su.nightexpress.nightcore.command.experimental.node;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;

@Deprecated
public interface DirectExecutor {
   boolean execute(@NonNull CommandContext var1, @NonNull ParsedArguments var2);
}
