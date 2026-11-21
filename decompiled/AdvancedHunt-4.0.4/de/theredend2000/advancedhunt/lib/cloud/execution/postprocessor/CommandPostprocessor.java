package de.theredend2000.advancedhunt.lib.cloud.execution.postprocessor;

import de.theredend2000.advancedhunt.lib.cloud.services.type.ConsumerService;
import org.apiguardian.api.API;

@API(
   status = API.Status.STABLE
)
public interface CommandPostprocessor<C> extends ConsumerService<CommandPostprocessingContext<C>> {
}
