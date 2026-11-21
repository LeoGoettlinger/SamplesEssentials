package de.theredend2000.advancedhunt.lib.cloud.help.result;

import de.theredend2000.advancedhunt.lib.cloud.help.HelpQuery;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface VerboseCommandResult<C> extends HelpQueryResult<C> {
   @NonNull
   static <C> VerboseCommandResult<C> of(@NonNull final HelpQuery<C> query, @NonNull final CommandEntry<C> entry) {
      return VerboseCommandResultImpl.of(query, entry);
   }

   @NonNull
   @Override
   HelpQuery<C> query();

   @NonNull
   CommandEntry<C> entry();
}
