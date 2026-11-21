package revxrsal.commands.autocomplete;

import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;

public interface AutoCompleter {
   AutoCompleter registerSuggestion(@NotNull String var1, @NotNull SuggestionProvider var2);

   AutoCompleter registerSuggestion(@NotNull String var1, @NotNull Collection<String> var2);

   AutoCompleter registerSuggestion(@NotNull String var1, @NotNull String... var2);

   AutoCompleter registerParameterSuggestions(@NotNull Class<?> var1, @NotNull SuggestionProvider var2);

   AutoCompleter registerParameterSuggestions(@NotNull Class<?> var1, @NotNull String var2);

   AutoCompleter registerSuggestionFactory(@NotNull SuggestionProviderFactory var1);

   AutoCompleter registerSuggestionFactory(int var1, @NotNull SuggestionProviderFactory var2);

   SuggestionProvider getSuggestionProvider(@NotNull String var1);

   List<String> complete(@NotNull CommandActor var1, @NotNull ArgumentStack var2);

   List<String> complete(@NotNull CommandActor var1, @NotNull String var2);

   void filterToClosestInput(boolean var1);

   CommandHandler and();
}
