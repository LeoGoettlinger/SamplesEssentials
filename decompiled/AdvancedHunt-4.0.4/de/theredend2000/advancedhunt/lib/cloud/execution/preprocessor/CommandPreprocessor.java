package de.theredend2000.advancedhunt.lib.cloud.execution.preprocessor;

import de.theredend2000.advancedhunt.lib.cloud.services.type.ConsumerService;
import org.apiguardian.api.API;

@API(
   status = API.Status.STABLE
)
public interface CommandPreprocessor<C> extends ConsumerService<CommandPreprocessingContext<C>> {
}
