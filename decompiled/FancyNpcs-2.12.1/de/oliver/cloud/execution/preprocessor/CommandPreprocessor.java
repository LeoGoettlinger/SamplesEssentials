package de.oliver.cloud.execution.preprocessor;

import de.oliver.cloud.services.type.ConsumerService;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(
   status = Status.STABLE
)
public interface CommandPreprocessor<C> extends ConsumerService<CommandPreprocessingContext<C>> {
}
