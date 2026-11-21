package ac.grim.grimac.command;

import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandInput;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.Suggestion;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.SuggestionProvider;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;

public final class CommandUtils {
   @Contract("_ -> new")
   @NotNull
   public static SuggestionProvider<Sender> fromStrings(@NotNull String... strings) {
      List<Suggestion> suggestions = new ArrayList<>(strings.length);

      for (String s : strings) {
         suggestions.add(Suggestion.suggestion(s));
      }

      return new CommandUtils.SenderSuggestionProvider(Collections.unmodifiableList(suggestions));
   }

   @Generated
   private CommandUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   private record SenderSuggestionProvider(List<Suggestion> suggestions) implements SuggestionProvider<Sender> {
      @NotNull
      @Override
      public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(@NotNull CommandContext context, @NotNull CommandInput input) {
         return CompletableFuture.completedFuture(this.suggestions);
      }
   }
}
