package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.annotation.AutoComplete;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProviderFactory;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.util.Strings;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
