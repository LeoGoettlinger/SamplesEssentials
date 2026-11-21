package vrx.economy.vRXClans.libs.cloud.injection;

import org.apiguardian.api.API;
import vrx.economy.vRXClans.libs.cloud.services.type.Service;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface InjectionService<C> extends Service<InjectionRequest<C>, Object> {
}
