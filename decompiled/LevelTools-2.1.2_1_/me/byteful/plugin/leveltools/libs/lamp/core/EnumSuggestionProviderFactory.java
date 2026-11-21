package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.ArrayList;
import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProvider;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProviderFactory;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum EnumSuggestionProviderFactory implements SuggestionProviderFactory {
   INSTANCE;

   private static final int MAX_ENUMS_SIZE = 64;

   @Nullable
   @Override
   public SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter) {
      if (!parameter.getType().isEnum()) {
         return null;
      } else {
         Class<? extends Enum> enumType = parameter.getType().asSubclass(Enum.class);
         Enum<?>[] enums = (Enum<?>[])enumType.getEnumConstants();
         List<String> suggestions = new ArrayList<>();

         for (int i = 0; i < enums.length && i < 63; i++) {
            suggestions.add(enums[i].name().toLowerCase());
         }

         return SuggestionProvider.of(suggestions);
      }
   }
}
