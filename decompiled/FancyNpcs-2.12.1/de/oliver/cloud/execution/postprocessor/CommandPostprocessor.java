package de.oliver.cloud.execution.postprocessor;

import de.oliver.cloud.services.type.ConsumerService;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(
   status = Status.STABLE
)
public interface CommandPostprocessor<C> extends ConsumerService<CommandPostprocessingContext<C>> {
}
