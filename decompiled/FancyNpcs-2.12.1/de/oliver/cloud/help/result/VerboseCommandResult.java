package de.oliver.cloud.help.result;

import de.oliver.cloud.help.HelpQuery;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
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
