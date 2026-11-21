package su.nightexpress.nightcore.commands.argument.type;

import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.SuggestionsProvider;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.argument.ArgumentType;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.util.Lists;

public class StringArgumentType implements ArgumentType<String>, SuggestionsProvider {
   private final boolean greedy;

   public StringArgumentType(boolean greedy) {
      this.greedy = greedy;
   }

   @NonNull
   public String parse(@NonNull CommandContextBuilder contextBuilder, @NonNull String string) throws CommandSyntaxException {
      return string;
   }

   @NonNull
   @Override
   public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
      return Lists.newList("...");
   }

   public boolean isGreedy() {
      return this.greedy;
   }
}
