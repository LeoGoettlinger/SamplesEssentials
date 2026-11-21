package vrx.economy.vRXClans.libs.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import vrx.economy.vRXClans.libs.cloud.services.type.ConsumerService;

@API(
   status = API.Status.STABLE
)
public interface CommandPostprocessor<C> extends ConsumerService<CommandPostprocessingContext<C>> {
}
