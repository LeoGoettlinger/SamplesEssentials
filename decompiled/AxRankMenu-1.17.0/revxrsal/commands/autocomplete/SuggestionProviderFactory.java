package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;

public interface SuggestionProviderFactory {
   @Nullable
   SuggestionProvider createSuggestionProvider(@NotNull CommandParameter var1);

   static SuggestionProviderFactory forType(Class<?> type, SuggestionProvider provider) {
      return parameter -> parameter.getType() == type ? provider : null;
   }

   static SuggestionProviderFactory forHierarchyType(Class<?> type, SuggestionProvider provider) {
      return parameter -> parameter.getType() != type && !parameter.getType().isAssignableFrom(type) ? null : provider;
   }
}
