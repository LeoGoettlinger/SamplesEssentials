package com.artillexstudios.axtrade.libs.lamp.autocomplete;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

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
