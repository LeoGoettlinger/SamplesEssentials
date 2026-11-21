package vrx.economy.vRXClans.libs.cloud.execution.preprocessor;

import org.apiguardian.api.API;
import vrx.economy.vRXClans.libs.cloud.services.type.ConsumerService;

@API(
   status = API.Status.STABLE
)
public interface CommandPreprocessor<C> extends ConsumerService<CommandPreprocessingContext<C>> {
}
