package me.byteful.plugin.leveltools.libs.lamp.core;

import java.lang.reflect.Type;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProvider;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProviderFactory;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.util.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EitherSuggestionProviderFactory implements SuggestionProviderFactory {
   INSTANCE;

   @Nullable
   @Override
   public SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter) {
      Class<?> rawType = parameter.getType();
      if (!Either.class.isAssignableFrom(rawType)) {
         return null;
      } else {
         Type[] types = EitherParameter.getTypes(parameter);
         EitherParameter first = new EitherParameter(parameter, types[0]);
         EitherParameter second = new EitherParameter(parameter, types[1]);
         return first.getSuggestionProvider().compose(second.getSuggestionProvider());
      }
   }
}
