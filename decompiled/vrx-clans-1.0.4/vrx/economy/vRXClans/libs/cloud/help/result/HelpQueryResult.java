package vrx.economy.vRXClans.libs.cloud.help.result;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.help.HelpQuery;
import vrx.economy.vRXClans.libs.cloud.help.HelpRenderer;

@API(
   status = API.Status.STABLE
)
public interface HelpQueryResult<C> {
   @NonNull
   HelpQuery<C> query();

   default void render(@NonNull final HelpRenderer<C> renderer) {
      renderer.render(this);
   }
}
