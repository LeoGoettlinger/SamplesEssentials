package de.oliver.cloud.bean;

import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
)
@Immutable
public interface CommandProperties {
   @NonNull
   static CommandProperties of(@NonNull final String name, @NonNull final String... aliases) {
      return CommandPropertiesImpl.of(name, Arrays.asList(aliases));
   }

   @NonNull
   static CommandProperties commandProperties(@NonNull final String name, @NonNull final String... aliases) {
      return of(name, aliases);
   }

   @NonNull
   String name();

   @NonNull
   Collection<String> aliases();
}
