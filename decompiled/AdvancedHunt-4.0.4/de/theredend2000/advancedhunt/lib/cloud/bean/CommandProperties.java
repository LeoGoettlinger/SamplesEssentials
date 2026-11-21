package de.theredend2000.advancedhunt.lib.cloud.bean;

import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
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
