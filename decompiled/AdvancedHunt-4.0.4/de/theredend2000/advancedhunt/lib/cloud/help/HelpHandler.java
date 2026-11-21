package de.theredend2000.advancedhunt.lib.cloud.help;

import de.theredend2000.advancedhunt.lib.cloud.help.result.HelpQueryResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.IndexCommandResult;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface HelpHandler<C> {
   @NonNull
   HelpQueryResult<C> query(@NonNull HelpQuery<C> query);

   @NonNull
   default IndexCommandResult<C> queryRootIndex(@NonNull final C sender) {
      return (IndexCommandResult<C>)this.query(HelpQuery.of(sender, ""));
   }
}
