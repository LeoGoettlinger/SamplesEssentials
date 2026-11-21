package com.artillexstudios.axtrade.libs.lamp.autocomplete;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
