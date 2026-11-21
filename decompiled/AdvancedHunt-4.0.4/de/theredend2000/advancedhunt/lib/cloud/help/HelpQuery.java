package de.theredend2000.advancedhunt.lib.cloud.help;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface HelpQuery<C> {
   @NonNull
   static <C> HelpQuery<C> of(@NonNull final C sender, @NonNull final String query) {
      return HelpQueryImpl.of(sender, query);
   }

   @NonNull
   C sender();

   @NonNull
   String query();
}
