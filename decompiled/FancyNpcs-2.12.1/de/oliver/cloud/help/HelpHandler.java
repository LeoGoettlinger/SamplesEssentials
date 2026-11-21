package de.oliver.cloud.help;

import de.oliver.cloud.help.result.HelpQueryResult;
import de.oliver.cloud.help.result.IndexCommandResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface HelpHandler<C> {
   @NonNull
   HelpQueryResult<C> query(@NonNull HelpQuery<C> query);

   @NonNull
   default IndexCommandResult<C> queryRootIndex(@NonNull final C sender) {
      return (IndexCommandResult<C>)this.query(HelpQuery.of(sender, ""));
   }
}
