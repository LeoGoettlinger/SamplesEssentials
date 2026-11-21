package de.oliver.cloud.annotations.descriptor;

import de.oliver.cloud.component.DefaultValue;
import de.oliver.cloud.description.Description;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
)
@Immutable
public interface ArgumentDescriptor extends Descriptor {
   @NonNull
   static ImmutableArgumentDescriptor.Builder builder() {
      return ImmutableArgumentDescriptor.builder();
   }

   @NonNull
   Parameter parameter();

   @NonNull
   @Override
   String name();

   @Nullable
   String parserName();

   @Nullable
   String suggestions();

   @Nullable
   DefaultValue<?, ?> defaultValue();

   @Nullable
   Description description();
}
