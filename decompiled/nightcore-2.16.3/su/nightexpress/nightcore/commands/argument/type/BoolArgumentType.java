package su.nightexpress.nightcore.commands.argument.type;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.SuggestionsProvider;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.argument.ArgumentType;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;

public class BoolArgumentType implements ArgumentType<Boolean>, SuggestionsProvider {
   private static final List<String> EXAMPLES = Arrays.asList("true", "false");

   @NonNull
   public Boolean parse(@NonNull CommandContextBuilder contextBuilder, @NonNull String string) throws CommandSyntaxException {
      return Boolean.parseBoolean(string);
   }

   @NonNull
   @Override
   public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
      return EXAMPLES;
   }
}
