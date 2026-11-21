package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProviderFactory;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.util.Either;
import java.lang.reflect.Type;
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
