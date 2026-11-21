package de.theredend2000.advancedhunt.lib.cloud.injection;

import de.theredend2000.advancedhunt.lib.cloud.services.type.Service;
import org.apiguardian.api.API;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface InjectionService<C> extends Service<InjectionRequest<C>, Object> {
}
