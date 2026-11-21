package de.oliver.cloud.injection;

import de.oliver.cloud.services.type.Service;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@FunctionalInterface
@API(
   status = Status.STABLE
)
public interface InjectionService<C> extends Service<InjectionRequest<C>, Object> {
}
