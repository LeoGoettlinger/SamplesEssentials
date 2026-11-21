package vrx.economy.vRXClans.libs.cloud.help;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.help.result.HelpQueryResult;

@API(
   status = API.Status.STABLE
)
public interface HelpRenderer<C> {
   void render(@NonNull HelpQueryResult<C> result);
}
