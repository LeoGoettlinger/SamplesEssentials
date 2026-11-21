package revxrsal.commands.core;

import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.Either;

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
