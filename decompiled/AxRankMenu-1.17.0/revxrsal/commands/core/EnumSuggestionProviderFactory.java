package revxrsal.commands.core;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.command.CommandParameter;

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
