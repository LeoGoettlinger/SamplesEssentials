package de.oliver.cloud.annotations;

import de.oliver.cloud.component.DefaultValue;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.STABLE
)
public interface DefaultValueFactory<C, T> {
   @NonNull
   static <C, T> DefaultValueFactory<C, T> constant(@NonNull final DefaultValue<C, T> defaultValue) {
      return parameter -> defaultValue;
   }

   @NonNull
   DefaultValue<C, T> create(@NonNull Parameter parameter);
}
