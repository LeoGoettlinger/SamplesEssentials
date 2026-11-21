package de.oliver.cloud.annotations.descriptor;

import de.oliver.cloud.description.Description;
import de.oliver.cloud.permission.Permission;
import java.lang.reflect.Parameter;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
)
@Immutable
public interface FlagDescriptor extends Descriptor {
   @NonNull
   static ImmutableFlagDescriptor.Builder builder() {
      return ImmutableFlagDescriptor.builder();
   }

   @NonNull
   Parameter parameter();

   @NonNull
   @Override
   String name();

   @NonNull
   Collection<String> aliases();

   @Nullable
   String parserName();

   @Nullable
   String suggestions();

   @Nullable
   Permission permission();

   @Nullable
   Description description();

   boolean repeatable();
}
