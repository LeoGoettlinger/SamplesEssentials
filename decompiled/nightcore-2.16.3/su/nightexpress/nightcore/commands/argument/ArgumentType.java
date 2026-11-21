package su.nightexpress.nightcore.commands.argument;

import org.jspecify.annotations.NullMarked;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;

@NullMarked
public interface ArgumentType<T> {
   T parse(CommandContextBuilder var1, String var2) throws CommandSyntaxException;
}
