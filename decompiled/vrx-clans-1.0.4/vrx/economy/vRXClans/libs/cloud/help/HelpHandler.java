package vrx.economy.vRXClans.libs.cloud.help;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.help.result.HelpQueryResult;
import vrx.economy.vRXClans.libs.cloud.help.result.IndexCommandResult;

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
