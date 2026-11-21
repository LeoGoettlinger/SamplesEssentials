package vrx.economy.vRXClans.libs.cloud.context;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface CommandContextFactory<C> {
   @API(
      status = API.Status.STABLE
   )
   @NonNull
   CommandContext<C> create(boolean suggestions, @NonNull C sender);
}
