package de.oliver.cloud.annotations.method;

import de.oliver.cloud.annotations.descriptor.Descriptor;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.INTERNAL
)
@Immutable
public interface ParameterValue {
   @NonNull
   static ParameterValue of(@NonNull final Parameter parameter, @Nullable final Object value) {
      return of(parameter, value, null);
   }

   @NonNull
   static ParameterValue of(@NonNull final Parameter parameter, @Nullable final Object value, @Nullable final Descriptor descriptor) {
      return ParameterValueImpl.of(parameter, value, descriptor);
   }

   @NonNull
   Parameter parameter();

   @Nullable
   Object value();

   @Nullable
   Descriptor descriptor();
}
