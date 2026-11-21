package vrx.economy.vRXClans.libs.cloud.component;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.component.preprocessor.ComponentPreprocessor;
import vrx.economy.vRXClans.libs.cloud.description.Description;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;
import vrx.economy.vRXClans.libs.cloud.key.CloudKeyHolder;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;

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
