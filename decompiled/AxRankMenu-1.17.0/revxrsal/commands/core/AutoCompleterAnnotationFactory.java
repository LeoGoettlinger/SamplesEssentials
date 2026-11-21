package revxrsal.commands.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.Strings;

class AutoCompleterAnnotationFactory implements SuggestionProviderFactory {
   private final Map<String, SuggestionProvider> tabProviders;

   public AutoCompleterAnnotationFactory(Map<String, SuggestionProvider> tabProviders) {
      this.tabProviders = tabProviders;
   }

   @Nullable
   @Override
   public SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter) {
      AutoComplete ann = parameter.getDeclaringCommand().getAnnotation(AutoComplete.class);
      return ann != null ? this.parseTabAnnotation(ann, parameter.getCommandIndex()) : null;
   }

   private SuggestionProvider parseTabAnnotation(@NotNull AutoComplete annotation, int commandIndex) {
      if (annotation.value().isEmpty()) {
         return SuggestionProvider.EMPTY;
      } else {
         String[] values = Strings.SPACE.split(annotation.value());

         try {
            String providerV = values[commandIndex];
            if (providerV.equals("*")) {
               return null;
            } else if (providerV.startsWith("@")) {
               SuggestionProvider provider = this.tabProviders.get(providerV.substring(1));
               if (provider == null) {
                  throw new IllegalStateException("No such tab suggestion provider: " + providerV.substring(1));
               } else {
                  return provider;
               }
            } else {
               List<String> suggestions = Arrays.asList(Strings.VERTICAL_BAR.split(providerV));
               return SuggestionProvider.of(suggestions);
            }
         } catch (IndexOutOfBoundsException var6) {
            return null;
         }
      }
   }
}
