package com.artillexstudios.axafkzone.libs.lamp.autocomplete;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import com.artillexstudios.axafkzone.libs.lamp.command.ExecutableCommand;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SuggestionProvider {
   SuggestionProvider EMPTY = (args, sender, command) -> Collections.emptyList();

   @NotNull
   Collection<String> getSuggestions(@NotNull List<String> var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3) throws Throwable;

   @Contract("null -> this; !null -> new")
   default SuggestionProvider compose(@Nullable SuggestionProvider other) {
      if (other == null) {
         return this;
      } else if (this == EMPTY && other == EMPTY) {
         return EMPTY;
      } else if (other == EMPTY) {
         return this;
      } else {
         return this == EMPTY ? other : (args, sender, command) -> {
            Set<String> completions = new HashSet<>(other.getSuggestions(args, sender, command));
            completions.addAll(this.getSuggestions(args, sender, command));
            return completions;
         };
      }
   }

   static SuggestionProvider of(@Nullable Collection<String> suggestions) {
      return suggestions == null ? EMPTY : (args, sender, command) -> suggestions;
   }

   static SuggestionProvider of(@Nullable String... suggestions) {
      if (suggestions == null) {
         return EMPTY;
      } else {
         List<String> values = com.artillexstudios.axafkzone.libs.lamp.util.Collections.listOf(suggestions);
         return (args, sender, command) -> values;
      }
   }

   static SuggestionProvider of(@NotNull Supplier<Collection<String>> supplier) {
      return (args, sender, command) -> supplier.get();
   }

   static <T> SuggestionProvider map(@NotNull Supplier<Collection<T>> values, Function<T, String> function) {
      return (args, sender, command) -> values.get().stream().map(function).collect(Collectors.toList());
   }
}
