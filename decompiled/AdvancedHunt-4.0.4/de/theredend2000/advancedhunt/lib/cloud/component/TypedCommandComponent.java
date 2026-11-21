package de.theredend2000.advancedhunt.lib.cloud.component;

import de.theredend2000.advancedhunt.lib.cloud.component.preprocessor.ComponentPreprocessor;
import de.theredend2000.advancedhunt.lib.cloud.description.Description;
import de.theredend2000.advancedhunt.lib.cloud.key.CloudKey;
import de.theredend2000.advancedhunt.lib.cloud.key.CloudKeyHolder;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public final class TypedCommandComponent<C, T> extends CommandComponent<C> implements CloudKeyHolder<T> {
   TypedCommandComponent(
      @NonNull final String name,
      @NonNull final ArgumentParser<C, ?> parser,
      @NonNull final TypeToken<?> valueType,
      @NonNull final Description description,
      @NonNull final CommandComponent.ComponentType componentType,
      @Nullable final DefaultValue<C, ?> defaultValue,
      @NonNull final SuggestionProvider<C> suggestionProvider,
      @NonNull final Collection<ComponentPreprocessor<C>> componentPreprocessors
   ) {
      super(name, parser, valueType, description, componentType, defaultValue, suggestionProvider, componentPreprocessors);
   }

   @NonNull
   @Override
   public TypeToken<T> valueType() {
      return (TypeToken<T>)super.valueType();
   }

   @NonNull
   @Override
   public ArgumentParser<C, T> parser() {
      return (ArgumentParser<C, T>)super.parser();
   }

   @Nullable
   @Override
   public DefaultValue<C, T> defaultValue() {
      return (DefaultValue<C, T>)super.defaultValue();
   }

   @NonNull
   @Override
   public CloudKey<T> key() {
      return CloudKey.of(this.name(), this.valueType());
   }
}
